package tanks.compression;

import com.github.luben.zstd.Zstd;

/** Java 8-compatible Zstandard implementation. Instances are immutable and safe to share. */
public final class ZstdCompression implements CompressionCodec
{
    private final int level;

    public ZstdCompression(int level)
    {
        if (level < 1 || level > 22)
            throw new IllegalArgumentException("Unsupported Zstandard compression level: " + level);
        this.level = level;
    }

    @Override
    public byte[] compress(byte[] input)
    {
        return Zstd.compress(input, level);
    }

    @Override
    public byte[] decompress(byte[] input, int expectedSize, int maxSize)
    {
        if (expectedSize < 0 || expectedSize > maxSize)
            throw new IllegalArgumentException("Invalid decompressed size: " + expectedSize);

        byte[] output = new byte[expectedSize];
        long result = Zstd.decompress(output, input);
        if (Zstd.isError(result) || result != expectedSize)
            throw new IllegalArgumentException("Invalid compressed payload");
        return output;
    }
}
