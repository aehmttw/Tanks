package tanks.network;

import tanks.compression.ZstdCompression;
import tanks.network.event.INetworkEvent;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.util.IdentityHashMap;

/** Shared framing for TCP, Steam and Tanks Online. All sizes are bytes, not characters. */
public final class NetworkMessage
{
    public static final int COMPRESSION_LEVEL = 4;
    public static final int COMPRESSION_THRESHOLD = 256;

    private static final NetworkFrameCodec frames = new NetworkFrameCodec(
        new ZstdCompression(COMPRESSION_LEVEL), COMPRESSION_THRESHOLD, MessageReader.max_event_size);
    private static volatile boolean compressionEnabled = true;

    private NetworkMessage()
    {
    }

    /** The caller owns the returned buffer. Event fields are encoded before frame compression. */
    public static ByteBuf encode(INetworkEvent event, ByteBufAllocator allocator)
    {
        return encode(event, allocator, compressionEnabled);
    }

    /** A local send policy; all peers can decode both frame forms. */
    static ByteBuf encode(INetworkEvent event, ByteBufAllocator allocator, boolean allowCompression)
    {
        int id = NetworkEventMap.get(event.getClass());
        if (id < 0)
            throw new IllegalArgumentException("Unregistered network event: " + event.getClass());

        ByteBuf frame;
        Broadcast b = broadcast.get();
        if (b == null)
            frame = frames.encode(EventObjectBuffer.encodeToBuffer(event), allocator, allowCompression);
        else
        {
            IdentityHashMap<INetworkEvent, ByteBuf> cache = b.frames[allowCompression ? 1 : 0];
            ByteBuf cached = cache.get(event);
            if (cached == null)
            {
                cached = frames.encode(EventObjectBuffer.encodeToBuffer(event), allocator, allowCompression);
                cache.put(event, cached);
            }
            frame = cached.retainedDuplicate();
        }

        MessageReader.recordSent(id, frame.readableBytes());
        return frame;
    }

    /** Frames encoded during a broadcast, by event instance, without and with compression allowed. */
    private static final class Broadcast
    {
        @SuppressWarnings("unchecked")
        final IdentityHashMap<INetworkEvent, ByteBuf>[] frames = new IdentityHashMap[]{new IdentityHashMap<>(), new IdentityHashMap<>()};
    }

    private static final ThreadLocal<Broadcast> broadcast = new ThreadLocal<>();

    /**
     * Until {@link #endBroadcast}, encoding an event instance this thread has already encoded reuses its frame,
     * so sending the same events to every connection encodes each of them once. Events must not be changed
     * between beginBroadcast and endBroadcast. Always pair with endBroadcast in a finally block.
     */
    public static void beginBroadcast()
    {
        endBroadcast();
        broadcast.set(new Broadcast());
    }

    public static void endBroadcast()
    {
        Broadcast b = broadcast.get();
        if (b == null)
            return;

        broadcast.remove();
        for (IdentityHashMap<INetworkEvent, ByteBuf> cache: b.frames)
        {
            for (ByteBuf frame: cache.values())
                frame.release();
        }
    }

    public static boolean isCompressionEnabled()
    {
        return compressionEnabled;
    }

    public static void setCompressionEnabled(boolean enabled)
    {
        compressionEnabled = enabled;
    }

    /** Returns null until a complete frame is available. The caller must release the decoded body. */
    public static ByteBuf tryDecode(ByteBuf queue)
    {
        int start = queue.readerIndex();
        ByteBuf message = frames.tryDecode(queue);
        if (message != null)
            MessageReader.recordReceived(queue.readerIndex() - start);
        return message;
    }
}
