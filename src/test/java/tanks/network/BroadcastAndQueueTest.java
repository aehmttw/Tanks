package tanks.network;

import tanks.Game;
import tanks.network.event.*;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class BroadcastAndQueueTest
{
    @BeforeClass
    public static void setup()
    {
        Game.debug = false;
        if (NetworkEventMap.get(EventChat.class) < 0)
            NetworkEventMap.register(EventChat.class);
    }

    @Test
    public void pendingPlayersReceiveExactlyTheChatEvents()
    {
        EventChat first = new EventChat("first");
        EventChat second = new EventChat("second");
        // A non-chat event first: the old check tested events.get(0) on every iteration
        for (ArrayList<INetworkEvent> out: Arrays.asList(
            new ArrayList<INetworkEvent>(Arrays.asList(new EventPing(true), first, new EventPing(false), second)),
            new ArrayList<INetworkEvent>(Arrays.asList(first, new EventPing(true), second, new EventPing(false)))))
        {
            ServerHandler handler = new ServerHandler(null);
            handler.addEvents(out, true);
            assertEquals(Arrays.asList(first, second), new ArrayList<>(handler.events));
        }
    }

    @Test
    public void broadcastFramesOutliveTheBroadcastAndAreReleasedByTheirOwners()
    {
        EventChat event = new EventChat("hello everyone");
        ByteBuf unshared = NetworkMessage.encode(event, UnpooledByteBufAllocator.DEFAULT);

        ByteBuf first;
        ByteBuf second;
        NetworkMessage.beginBroadcast();
        try
        {
            first = NetworkMessage.encode(event, UnpooledByteBufAllocator.DEFAULT);
            second = NetworkMessage.encode(event, UnpooledByteBufAllocator.DEFAULT);
        }
        finally
        {
            NetworkMessage.endBroadcast();
        }

        // Like frames still queued in a channel when the tick's broadcast has ended
        assertEquals(2, first.refCnt());
        assertEquals(unshared, first);
        assertEquals(unshared, second);
        for (ByteBuf frame: Arrays.asList(first, second))
        {
            ByteBuf body = NetworkMessage.tryDecode(frame);
            try
            {
                assertEquals("hello everyone", ((EventChat) EventObjectBuffer.decode(body)).message);
            }
            finally
            {
                body.release();
            }
            frame.release();
        }
        assertEquals(0, first.refCnt());
        unshared.release();

        // Outside a broadcast nothing is reused
        ByteBuf a = NetworkMessage.encode(event, UnpooledByteBufAllocator.DEFAULT);
        ByteBuf b = NetworkMessage.encode(event, UnpooledByteBufAllocator.DEFAULT);
        assertEquals(1, a.refCnt());
        assertEquals(1, b.refCnt());
        a.release();
        b.release();
    }
}
