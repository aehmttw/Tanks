package tanks.network;

import tanks.compression.CompressionCodec;
import tanks.compression.ZstdCompression;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.Test;

import static org.junit.Assert.*;

/** Exercises framing without registering game events or initializing a transport. */
public class NetworkFrameCodecTest
{
    @Test
    public void frameRoundTripPreservesInputOwnershipAndReaderIndex()
    {
        NetworkFrameCodec codec = new NetworkFrameCodec(new ZstdCompression(4), 256, 4096);
        ByteBuf body = Unpooled.buffer().writeInt(999).writeInt(7).writeZero(2000);
        body.skipBytes(4);
        ByteBuf frame = codec.encode(body, UnpooledByteBufAllocator.DEFAULT, true);
        try
        {
            assertEquals(4, body.readerIndex());
            assertEquals(1, body.refCnt());
            assertEquals(1, frame.getUnsignedByte(0) & 1);
            ByteBuf decoded = codec.tryDecode(frame);
            try
            {
                assertEquals(body, decoded);
                assertFalse(frame.isReadable());
            }
            finally
            {
                decoded.release();
            }
            assertEquals(1, frame.refCnt());
        }
        finally
        {
            body.release();
            frame.release();
        }
    }

    @Test
    public void incompleteFrameDoesNotConsumeInput()
    {
        NetworkFrameCodec codec = new NetworkFrameCodec(new ZstdCompression(4), 256, 64);
        ByteBuf queue = Unpooled.buffer().writeByte(16).writeInt(7);
        try
        {
            assertNull(codec.tryDecode(queue));
            assertEquals(0, queue.readerIndex());
            queue.writeInt(123);
            ByteBuf decoded = codec.tryDecode(queue);
            try
            {
                assertEquals(7, decoded.readInt());
                assertEquals(123, decoded.readInt());
                assertFalse(queue.isReadable());
            }
            finally
            {
                decoded.release();
            }
        }
        finally
        {
            queue.release();
        }
    }

    @Test
    public void splitHeaderAndMalformedLengthsDoNotConsumeInput()
    {
        NetworkFrameCodec codec = new NetworkFrameCodec(new ZstdCompression(4), 256, 4096);
        ByteBuf queue = Unpooled.buffer().writeByte(0x80);
        try
        {
            assertNull(codec.tryDecode(queue));
            assertEquals(0, queue.readerIndex());
            queue.writeByte(0x02); // 128-byte uncompressed body
            assertNull(codec.tryDecode(queue));
            assertEquals(0, queue.readerIndex());
            queue.clear().writeByte(0x80).writeByte(0);
            assertThrows(IllegalArgumentException.class, () -> codec.tryDecode(queue));
            queue.clear().writeByte(0xFF).writeByte(0xFF).writeByte(0xFF).writeByte(0xFF).writeByte(0x08);
            assertThrows(IllegalArgumentException.class, () -> codec.tryDecode(queue));
        }
        finally
        {
            queue.release();
        }
    }

    @Test
    public void policySkipsCompressionAndAccountsForEnvelopeOverhead()
    {
        CountingCompression compression = new CountingCompression();
        NetworkFrameCodec codec = new NetworkFrameCodec(compression, 256, 4096);
        ByteBuf body = Unpooled.buffer().writeInt(7).writeZero(252);
        try
        {
            ByteBuf frame = codec.encode(body, UnpooledByteBufAllocator.DEFAULT, false);
            frame.release();
            assertEquals(0, compression.calls);

            body.writerIndex(255);
            frame = codec.encode(body, UnpooledByteBufAllocator.DEFAULT, true);
            frame.release();
            assertEquals(0, compression.calls);

            body.writerIndex(256);
            frame = codec.encode(body, UnpooledByteBufAllocator.DEFAULT, true);
            try
            {
                assertEquals(1, compression.calls);
                assertEquals(0, frame.getUnsignedByte(0) & 1); // savings equal overhead: keep the original
                assertEquals(258, frame.readableBytes());
            }
            finally
            {
                frame.release();
            }
        }
        finally
        {
            body.release();
        }
    }

    private static class CountingCompression implements CompressionCodec
    {
        int calls;

        @Override
        public byte[] compress(byte[] input)
        {
            calls++;
            return new byte[input.length - 2];
        }

        @Override
        public byte[] decompress(byte[] input, int expectedSize, int maxSize)
        {
            throw new AssertionError("No compressed frames should be produced by this test");
        }
    }
}
