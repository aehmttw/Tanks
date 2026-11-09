package tanks.tankson;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/** Compact-save container format; independent of paths, game settings and the desktop file manager. */
public final class TanksSaveCodec
{
    private static final byte[] MAGIC = {'T', 'K', 'O', 'B'};
    private static final int VERSION = 1;
    private static final int HEADER_SIZE = 6;
    // Header byte 5 selects compression. Only 0 (none) is supported; 1 is reserved for Zstandard.
    private static final int NO_COMPRESSION = 0;

    private TanksSaveCodec()
    {
    }

    /** Uses the smaller representation. Encoding does not modify existing files. */
    public static byte[] encode(String text, boolean useObjectBuffers)
    {
        byte[] plain = text.getBytes(StandardCharsets.UTF_8);
        if (!useObjectBuffers)
            return plain;

        byte[] compact = TanksONDocument.encode(text);
        if (compact == null || compact.length + HEADER_SIZE >= plain.length)
            return plain;

        byte[] bytes = Arrays.copyOf(MAGIC, HEADER_SIZE + compact.length);
        bytes[4] = VERSION;
        bytes[5] = NO_COMPRESSION;
        System.arraycopy(compact, 0, bytes, HEADER_SIZE, compact.length);
        return bytes;
    }

    /** Takes ownership of source. Closing the returned reader closes any remaining input stream. */
    public static BufferedReader openReader(InputStream source) throws IOException
    {
        BufferedInputStream input = new BufferedInputStream(source);
        try
        {
            input.mark(HEADER_SIZE);
            boolean binary = true;
            for (byte b: MAGIC)
                binary &= input.read() == b;

            if (!binary)
            {
                input.reset();
                return new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
            }

            if (input.read() != VERSION)
                throw new IOException("Unsupported ObjectBuffer save version");
            if (input.read() != NO_COMPRESSION)
                throw new IOException("Unsupported ObjectBuffer save compression");

            ByteArrayOutputStream body = new ByteArrayOutputStream();
            byte[] block = new byte[8192];
            int count;
            while ((count = input.read(block)) != -1)
                body.write(block, 0, count);

            input.close();
            return new BufferedReader(new StringReader(TanksONDocument.decode(body.toByteArray())));
        }
        catch (IOException | RuntimeException e)
        {
            try
            {
                input.close();
            }
            catch (IOException closeError)
            {
                e.addSuppressed(closeError);
            }
            throw e;
        }
    }
}
