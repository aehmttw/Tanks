package tanks.compression;

/** Byte compression independent of transport, serialization and file formats. */
public interface CompressionCodec
{
    byte[] compress(byte[] input);

    /** Reject sizes above maxSize before allocating the output, and require an exact size match. */
    byte[] decompress(byte[] input, int expectedSize, int maxSize);
}
