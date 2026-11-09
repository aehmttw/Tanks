package tanks.network;

import basewindow.ComputerFileManager;
import tanks.Game;
import tanks.tankson.TanksONDocument;

import com.github.luben.zstd.Zstd;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Run with ./gradlew benchmarkNetwork. Measures payload bytes, excluding message headers. */
public class NetworkCompressionBenchmark
{
    public static void main(String[] args) throws Exception
    {
        Game.game.fileManager = new ComputerFileManager();
        List<byte[]> payloads = new ArrayList<>();
        long[] totals = new long[6];
        System.out.println("Java " + System.getProperty("java.version"));
        System.out.println("file,text,objectbuffer,text_zstd3,text_zstd4,objectbuffer_zstd3,objectbuffer_zstd4");
        try (ZipFile archive = new ZipFile("Tests.zip"))
        {
            Enumeration<? extends ZipEntry> entries = archive.entries();
            while (entries.hasMoreElements())
            {
                ZipEntry entry = entries.nextElement();
                if (!entry.getName().endsWith(".tanks"))
                    continue;

                byte[] raw;
                try (InputStream input = archive.getInputStream(entry))
                {
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    byte[] block = new byte[8192];
                    int count;
                    while ((count = input.read(block)) != -1)
                        bytes.write(block, 0, count);
                    raw = bytes.toByteArray();
                }

                byte[] compact = TanksONDocument.encode(new String(raw, StandardCharsets.UTF_8));
                if (compact == null || compact.length >= raw.length)
                    compact = raw;
                else if (!Arrays.equals(compact, TanksONDocument.encode(TanksONDocument.decode(compact))))
                    throw new AssertionError("Document round trip failed: " + entry.getName());

                payloads.add(compact);
                long[] sizes = {raw.length, compact.length, Zstd.compress(raw, 3).length, Zstd.compress(raw, 4).length,
                    Zstd.compress(compact, 3).length, Zstd.compress(compact, 4).length};
                StringBuilder row = new StringBuilder(entry.getName());
                for (int i = 0; i < sizes.length; i++)
                {
                    row.append(',').append(sizes[i]);
                    totals[i] += sizes[i];
                }
                System.out.println(row);
            }
        }
        System.out.println("TOTAL " + Arrays.toString(totals));

        // Warm both levels before measuring; timings are indicative, not a pass/fail criterion.
        for (int pass = 0; pass < 10; pass++)
        {
            for (byte[] payload: payloads)
            {
                Zstd.compress(payload, 3);
                Zstd.compress(payload, 4);
            }
        }
        for (int level: new int[]{3, 4})
        {
            long start = System.nanoTime();
            for (int pass = 0; pass < 50; pass++)
            {
                for (byte[] payload: payloads)
                    Zstd.compress(payload, level);
            }
            System.out.printf(Locale.ROOT, "Level %d: %.3f ms per corpus pass%n", level, (System.nanoTime() - start) / 50e6);
        }
    }
}
