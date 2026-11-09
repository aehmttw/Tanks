package tanks.compression;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class ZstdCompressionTest
{
    @Test
    public void codecWorksWithoutNetworkOrFileState()
    {
        for (int level: new int[]{3, 4})
        {
            CompressionCodec codec = new ZstdCompression(level);
            for (byte[] input: new byte[][]{new byte[0], "Unicode 猫 🐢".getBytes(StandardCharsets.UTF_8), new byte[4096]})
                assertArrayEquals(input, codec.decompress(codec.compress(input), input.length, 4096));
        }
    }

    @Test
    public void validatesSizeBeforeDecompressionAndRejectsLengthMismatch()
    {
        CompressionCodec codec = new ZstdCompression(4);
        assertThrows(IllegalArgumentException.class, () -> codec.decompress(new byte[0], Integer.MAX_VALUE, 1024));
        assertThrows(IllegalArgumentException.class, () -> codec.decompress(new byte[0], -1, 1024));
        byte[] compressed = codec.compress(new byte[10]);
        assertThrows(RuntimeException.class, () -> codec.decompress(compressed, 9, 1024));
        assertThrows(RuntimeException.class, () -> codec.decompress(compressed, 11, 1024));
    }
}
