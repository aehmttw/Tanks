package tanks.network;

import basewindow.ComputerFileManager;
import tanks.Game;
import tanks.network.event.*;
import tanks.tankson.TanksON;
import tanks.tankson.ObjectBuffer;
import tanksonline.TanksOnlineServerHandler;

import io.netty.buffer.*;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class NetworkMessageTest
{
    @BeforeClass
    public static void setup()
    {
        Game.game.fileManager = new ComputerFileManager();
        Game.logger = System.out;
        Game.debug = false;
        NetworkEventMap.register(Payload.class);
        NetworkEventMap.register(EventSendClientDetails.class);
        NetworkEventMap.register(EventKick.class);
        NetworkEventMap.register(EventSetItem.class);
    }

    @Test
    public void compressesLargePayloadAndLeavesTinyAndRandomPayloadsRaw()
    {
        byte[] repeated = new byte[8192];
        Arrays.fill(repeated, (byte) 42);
        roundTrip(repeated, true);
        roundTrip(new byte[]{1, 2}, false);
        byte[] random = new byte[8192];
        new Random(123).nextBytes(random);
        roundTrip(random, false);
    }

    @Test
    public void compressionSettingChangesOnlyOutboundFrames()
    {
        byte[] repeated = new byte[8192];
        ByteBuf compressed = NetworkMessage.encode(new Payload(repeated), UnpooledByteBufAllocator.DEFAULT, true);
        boolean before = NetworkMessage.isCompressionEnabled();
        try
        {
            NetworkMessage.setCompressionEnabled(false);
            ByteBuf raw = NetworkMessage.encode(new Payload(repeated), UnpooledByteBufAllocator.DEFAULT);
            try
            {
                assertEquals(1, compressed.getUnsignedByte(0) & 1);
                assertEquals(0, raw.getUnsignedByte(0) & 1);
                assertTrue(raw.readableBytes() > compressed.readableBytes());
                for (ByteBuf frame: new ByteBuf[]{compressed, raw})
                {
                    ByteBuf body = NetworkMessage.tryDecode(frame);
                    try
                    {
                        assertArrayEquals(repeated, ((Payload) EventObjectBuffer.decode(body)).bytes);
                    }
                    finally
                    {
                        body.release();
                    }
                }
            }
            finally
            {
                raw.release();
            }
        }
        finally
        {
            NetworkMessage.setCompressionEnabled(before);
            compressed.release();
        }
    }

    private static void roundTrip(byte[] bytes, boolean compressed)
    {
        ByteBuf frame = NetworkMessage.encode(new Payload(bytes), UnpooledByteBufAllocator.DEFAULT);
        try
        {
            assertEquals(compressed, (frame.getUnsignedByte(0) & 1) != 0);
            assertTrue(frame.readableBytes() <= bytes.length + 128);
            ByteBuf body = NetworkMessage.tryDecode(frame);
            try
            {
                Payload copy = (Payload) EventObjectBuffer.decode(body);
                assertArrayEquals(bytes, copy.bytes);
                assertFalse(body.isReadable());
                assertFalse(frame.isReadable());
            }
            finally
            {
                body.release();
            }
        }
        finally
        {
            frame.release();
        }
    }

    @Test
    public void fragmentedAndCoalescedFramesReachTheMessageReaderExactlyOnce()
    {
        RecordingReader reader = new RecordingReader();
        reader.queue = Unpooled.buffer();
        ByteBuf first = NetworkMessage.encode(new Payload(new byte[3000]), UnpooledByteBufAllocator.DEFAULT);
        ByteBuf second = NetworkMessage.encode(new Payload(new byte[]{7}), UnpooledByteBufAllocator.DEFAULT);
        try
        {
            // Split both the header and the body, then combine the tail with another event.
            for (int i = 0; i < 6; i++)
            {
                ByteBuf fragment = first.readRetainedSlice(1);
                reader.queueMessage(null, null, fragment, null);
                fragment.release();
                assertTrue(reader.received.isEmpty());
            }
            ByteBuf tail = Unpooled.buffer();
            tail.writeBytes(first).writeBytes(second);
            reader.queueMessage(null, null, tail, null);
            tail.release();
            assertEquals(Arrays.asList(3000, 1), reader.received);
            assertFalse(reader.queue.isReadable());
        }
        finally
        {
            first.release();
            second.release();
            reader.queue.release();
        }
    }

    @Test
    public void tcpSendersUseTheSameFraming()
    {
        EmbeddedChannel channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter());
        ClientHandler client = new ClientHandler(false, null);
        client.ctx = channel.pipeline().firstContext();
        ServerHandler server = new ServerHandler(null);
        server.ctx = client.ctx;
        TanksOnlineServerHandler online = new TanksOnlineServerHandler(null);
        online.ctx = client.ctx;
        Payload event = new Payload(new byte[2048]);
        try
        {
            client.sendEvent(event);
            server.sendEvent(event);
            online.sendEvent(event);
            for (int i = 0; i < 3; i++)
            {
                ByteBuf frame = channel.readOutbound();
                assertNotNull(frame);
                ByteBuf body = NetworkMessage.tryDecode(frame);
                try
                {
                    Payload restored = (Payload) EventObjectBuffer.decode(body);
                    assertArrayEquals(event.bytes, restored.bytes);
                }
                finally
                {
                    body.release();
                    frame.release();
                }
            }
        }
        finally
        {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    public void structuredEventIsDecodedAndQueuedWithItsValuesIntact()
    {
        EventSetItem event = new EventSetItem();
        event.itemStackString = "/*TANKSON v1.1*/{\"obj_type\":\"item_stack\",\"name\":\"猫\",\"stack_size\":12}";
        event.playerID = UUID.randomUUID();
        event.slot = 3;
        MessageReader reader = new MessageReader();
        reader.queue = Unpooled.buffer();
        ByteBuf frame = NetworkMessage.encode(event, UnpooledByteBufAllocator.DEFAULT);
        int count = Game.eventsIn.size();
        try
        {
            reader.queueMessage(null, null, frame, null);
            assertEquals(count + 1, Game.eventsIn.size());
            EventSetItem restored = (EventSetItem) Game.eventsIn.get(count);
            assertEquals(event.playerID, restored.playerID);
            assertEquals(event.slot, restored.slot);
            assertEquals(TanksON.parseObject(event.itemStackString), TanksON.parseObject(restored.itemStackString));
        }
        finally
        {
            while (Game.eventsIn.size() > count)
                Game.eventsIn.remove(Game.eventsIn.size() - 1);
            frame.release();
            reader.queue.release();
        }
    }

    @Test
    public void rejectsInvalidFrameSizesBeforeAllocation()
    {
        for (byte[] invalid: new byte[][]{{0}, {1}, {(byte) 0x80, 0},
            {(byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x70}})
        {
            ByteBuf frame = Unpooled.wrappedBuffer(invalid);
            try
            {
                assertThrows(IllegalArgumentException.class, () -> NetworkMessage.tryDecode(frame));
            }
            finally
            {
                frame.release();
            }
        }

        ByteBuf bomb = Unpooled.buffer().writeByte(11).writeByte(0x80).writeByte(0x80)
            .writeByte(0x80).writeByte(0x40).writeByte(0);
        try
        {
            assertThrows(IllegalArgumentException.class, () -> NetworkMessage.tryDecode(bomb));
        }
        finally
        {
            bomb.release();
        }
    }

    @Test
    public void rejectsCorruptCompressionAndIncorrectDecodedLength()
    {
        ByteBuf corrupt = Unpooled.buffer().writeByte(11).writeByte(64).writeInt(0);
        try
        {
            assertThrows(RuntimeException.class, () -> NetworkMessage.tryDecode(corrupt));
        }
        finally
        {
            corrupt.release();
        }
        ByteBuf frame = NetworkMessage.encode(new Payload(new byte[4096]), UnpooledByteBufAllocator.DEFAULT);
        try
        {
            int decodedLengthIndex = 0;
            while ((frame.getUnsignedByte(decodedLengthIndex++) & 0x80) != 0)
            {
            }
            frame.setByte(decodedLengthIndex, frame.getUnsignedByte(decodedLengthIndex) + 1);
            assertThrows(RuntimeException.class, () -> NetworkMessage.tryDecode(frame));
        }
        finally
        {
            frame.release();
        }
    }

    @Test
    public void utf8StringsAndBinaryUuidsRoundTrip()
    {
        ByteBuf buffer = Unpooled.buffer();
        try
        {
            String text = "こんにちは 🐢 café §";
            NetworkUtils.writeString(buffer, text);
            assertEquals(text.getBytes(NetworkUtils.charset).length, buffer.getInt(0));
            assertEquals(text, NetworkUtils.readString(buffer));
            NetworkUtils.writeString(buffer, null);
            assertNull(NetworkUtils.readString(buffer));
            UUID id = UUID.randomUUID();
            INetworkEvent.writeUUID(buffer, id);
            assertEquals(17, buffer.readableBytes());
            assertEquals(id, INetworkEvent.readUUID(buffer));
            INetworkEvent.writeUUID(buffer, null);
            assertEquals(1, buffer.readableBytes());
            assertNull(INetworkEvent.readUUID(buffer));
            buffer.writeInt(1000).writeByte(0);
            assertThrows(IllegalArgumentException.class, () -> NetworkUtils.readString(buffer));
        }
        finally
        {
            buffer.release();
        }
    }

    @Test
    public void handshakesAndRejectionsAreObjectBuffers()
    {
        UUID id = UUID.randomUUID();
        ByteBuf frame = NetworkMessage.encode(new EventSendClientDetails(Game.network_protocol, id, "player"), UnpooledByteBufAllocator.DEFAULT);
        try
        {
            ByteBuf body = NetworkMessage.tryDecode(frame);
            try
            {
                EventSendClientDetails copy = (EventSendClientDetails) EventObjectBuffer.decode(body);
                assertEquals(Game.network_protocol, copy.version);
                assertEquals(id, copy.clientID);
                assertEquals("player", copy.username);
            }
            finally
            {
                body.release();
            }
        }
        finally
        {
            frame.release();
        }
        frame = NetworkMessage.encode(new EventKick("reason"), UnpooledByteBufAllocator.DEFAULT);
        try
        {
            ByteBuf body = NetworkMessage.tryDecode(frame);
            try
            {
                assertEquals("reason", ((EventKick) EventObjectBuffer.decode(body)).reason);
            }
            finally
            {
                body.release();
            }
        }
        finally
        {
            frame.release();
        }
    }

    @Test
    public void objectBufferEventHasTypedFields()
    {
        Payload payload = new Payload(new byte[]{1, 2, 3});
        List<?> values = (List<?>) ObjectBuffer.parseNetworkBytes(EventObjectBuffer.encode(payload));
        assertEquals(NetworkEventMap.get(Payload.class), values.get(0));
        assertArrayEquals(payload.bytes, (byte[]) values.get(1));
    }

    public static class Payload implements INetworkEvent
    {
        public byte[] bytes;

        public Payload()
        {
        }

        public Payload(byte[] bytes)
        {
            this.bytes = bytes;
        }

        @Override
        public void write(ByteBuf buffer)
        {
            buffer.writeInt(bytes.length).writeBytes(bytes);
        }

        @Override
        public void read(ByteBuf buffer)
        {
            bytes = new byte[buffer.readInt()];
            buffer.readBytes(bytes);
        }

        @Override
        public void execute()
        {
        }
    }

    private static class RecordingReader extends MessageReader
    {
        final List<Integer> received = new ArrayList<>();

        @Override
        public synchronized void readMessage(ServerHandler s, ClientHandler c, ByteBuf message, UUID clientID)
        {
            Payload event = (Payload) EventObjectBuffer.decode(message);
            received.add(event.bytes.length);
            assertFalse(message.isReadable());
        }
    }
}
