package tanks.network;

import tanks.compression.CompressionCodec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;

import java.nio.ByteBuffer;
import java.util.Arrays;

/** Length-prefixed event frames, independent of events, transports and bandwidth counters. */
public final class NetworkFrameCodec
{
    private final CompressionCodec compression;
    private final int compressionThreshold;
    private final int maxFrameSize;

    public NetworkFrameCodec(CompressionCodec compression, int compressionThreshold, int maxFrameSize)
    {
        if (compression == null || compressionThreshold < 0 || maxFrameSize < 1 || maxFrameSize > Integer.MAX_VALUE / 2)
            throw new IllegalArgumentException("Invalid frame codec configuration");
        this.compression = compression;
        this.compressionThreshold = compressionThreshold;
        this.maxFrameSize = maxFrameSize;
    }

    /** Does not consume or retain body. The caller owns the returned frame. */
    public ByteBuf encode(ByteBuf body, ByteBufAllocator allocator, boolean allowCompression)
    {
        int size = body.readableBytes();
        if (body.hasArray())
            return encode(body.array(), body.arrayOffset() + body.readerIndex(), size, allocator, allowCompression);

        byte[] raw = new byte[size];
        body.getBytes(body.readerIndex(), raw);
        return encode(raw, 0, size, allocator, allowCompression);
    }

    /** Does not consume or retain body, which must be array-backed. The caller owns the returned frame. */
    public ByteBuf encode(ByteBuffer body, ByteBufAllocator allocator, boolean allowCompression)
    {
        return encode(body.array(), body.arrayOffset() + body.position(), body.remaining(), allocator, allowCompression);
    }

    private ByteBuf encode(byte[] array, int offset, int size, ByteBufAllocator allocator, boolean allowCompression)
    {
        checkSize(size);
        if (allowCompression && size >= compressionThreshold)
        {
            byte[] raw = offset == 0 && array.length == size ? array : Arrays.copyOfRange(array, offset, offset + size);
            byte[] compressed = compression.compress(raw);
            long payloadLength = (long) compressed.length + varIntSize(size);
            if (compressed.length > 0 && payloadLength <= maxFrameSize &&
                payloadLength + varIntSize((int) payloadLength * 2 + 1) < size + varIntSize(size * 2))
            {
                int payloadSize = (int) payloadLength;
                ByteBuf frame = allocator.buffer(payloadSize + varIntSize(payloadSize * 2 + 1));
                writeVarInt(frame, payloadSize * 2 + 1);
                writeVarInt(frame, size);
                frame.writeBytes(compressed);
                return frame;
            }
        }

        ByteBuf frame = allocator.buffer(size + varIntSize(size * 2));
        writeVarInt(frame, size * 2);
        frame.writeBytes(array, offset, size);
        return frame;
    }

    /** Returns null without consuming input until a complete frame is available. Release the result. */
    public ByteBuf tryDecode(ByteBuf queue)
    {
        int header = readVarInt(queue, queue.readerIndex(), queue.writerIndex());
        if (header < 0)
            return null;

        int headerSize = varIntSize(header);
        int size = header >>> 1;
        checkSize(size);
        if (queue.readableBytes() - headerSize < size)
            return null;

        queue.skipBytes(headerSize);
        ByteBuf body = queue.readRetainedSlice(size);
        if ((header & 1) == 0)
            return body;

        try
        {
            int decodedSize = readVarInt(body, body.readerIndex(), body.writerIndex());
            if (decodedSize < 0)
                throw new IllegalArgumentException("Truncated compressed network frame");
            checkSize(decodedSize);
            body.skipBytes(varIntSize(decodedSize));
            if (!body.isReadable())
                throw new IllegalArgumentException("Missing compressed network data");
            byte[] compressed = new byte[body.readableBytes()];
            body.readBytes(compressed);
            return Unpooled.wrappedBuffer(compression.decompress(compressed, decodedSize, maxFrameSize));
        }
        finally
        {
            body.release();
        }
    }

    /** Unsigned varint, with the low bit of the frame header reserved for compression. */
    static void writeVarInt(ByteBuf output, int value)
    {
        while ((value & ~0x7F) != 0)
        {
            output.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        output.writeByte(value);
    }

    private static int readVarInt(ByteBuf input, int index, int end)
    {
        int result = 0;
        for (int i = 0; i < 5; i++)
        {
            if (index + i == end)
                return -1;
            int next = input.getUnsignedByte(index + i);
            if (i == 4 && (next & 0xF8) != 0)
                throw new IllegalArgumentException("Network frame length overflows an int");
            result |= (next & 0x7F) << (7 * i);
            if ((next & 0x80) == 0)
            {
                if (varIntSize(result) != i + 1)
                    throw new IllegalArgumentException("Noncanonical network frame length");
                return result;
            }
        }
        throw new IllegalArgumentException("Network frame length exceeds five bytes");
    }

    private static int varIntSize(int value)
    {
        int length = 1;
        while ((value >>>= 7) != 0)
            length++;
        return length;
    }

    private void checkSize(int size)
    {
        if (size < 1 || size > maxFrameSize)
            throw new IllegalArgumentException("Invalid network message size: " + size);
    }
}
