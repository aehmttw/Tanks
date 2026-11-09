package tanks.network;

import tanks.Game;
import tanks.compression.ZstdCompression;
import tanks.gui.screen.ScreenArcadeBonuses;
import tanks.network.event.*;
import tanks.network.event.online.EventUploadLevel;

import io.netty.buffer.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Run with ./gradlew benchmarkNetworkWire. Counts complete application frames on the same event instances. */
public final class NetworkWireBenchmark
{
    private static final ByteBufAllocator ALLOCATOR = UnpooledByteBufAllocator.DEFAULT;
    private static final ZstdCompression ZSTD = new ZstdCompression(4);

    private NetworkWireBenchmark()
    {
    }

    public static void main(String[] args) throws Exception
    {
        Game.debug = false;
        Game.registerEvents();

        List<Scenario> scenarios = scenarios();
        long[] totals = new long[4];
        File reportPath = new File("build/reports/network-wire.csv");
        if (!reportPath.getParentFile().exists() && !reportPath.getParentFile().mkdirs())
            throw new IOException("Unable to create benchmark report directory");
        System.out.println("Java " + System.getProperty("java.version"));
        System.out.println("Full frames include their length prefix; sizes exclude TCP/IP and Steam transport overhead.");
        try (PrintWriter report = new PrintWriter(new OutputStreamWriter(new FileOutputStream(reportPath), StandardCharsets.UTF_8)))
        {
            writeRow(report, "scenario,events,legacy_bytes,legacy_zstd4_bytes,objectbuffer_bytes,objectbuffer_zstd4_bytes,change_vs_legacy_pct,change_vs_legacy_zstd_pct");
            for (Scenario scenario: scenarios)
            {
                long[] sizes = measure(scenario.events);
                for (int i = 0; i < totals.length; i++)
                    totals[i] += sizes[i];

                writeRow(report, String.format(Locale.ROOT, "%s,%d,%d,%d,%d,%d,%.1f,%.1f", scenario.name,
                    scenario.events.size(), sizes[0], sizes[1], sizes[2], sizes[3],
                    change(sizes[0], sizes[3]), change(sizes[1], sizes[3])));
            }
            writeRow(report, String.format(Locale.ROOT, "TOTAL,%d,%d,%d,%d,%d,%.1f,%.1f", count(scenarios),
                totals[0], totals[1], totals[2], totals[3], change(totals[0], totals[3]), change(totals[1], totals[3])));
        }
        System.out.println("Report: " + reportPath.getPath());

        // Timing is measured separately from byte counts. It is indicative and varies by machine.
        List<INetworkEvent> all = new ArrayList<>();
        for (Scenario scenario: scenarios)
            all.addAll(scenario.events);
        for (int mode = 0; mode < 4; mode++)
        {
            run(all, mode);
            long start = System.nanoTime();
            for (int pass = 0; pass < 3; pass++)
                run(all, mode);
            System.out.printf(Locale.ROOT, "encode_time_ms,%s,%.2f%n", modeName(mode),
                (System.nanoTime() - start) / 3e6);
        }
    }

    private static void writeRow(PrintWriter report, String row)
    {
        report.println(row);
        System.out.println(row);
    }

    private static long[] measure(List<INetworkEvent> events)
    {
        long[] sizes = new long[4];
        for (INetworkEvent event: events)
        {
            for (int mode = 0; mode < 4; mode++)
            {
                ByteBuf frame = encode(event, mode);
                try
                {
                    sizes[mode] += frame.readableBytes();
                }
                finally
                {
                    frame.release();
                }
            }
        }
        return sizes;
    }

    private static void run(List<INetworkEvent> events, int mode)
    {
        for (INetworkEvent event: events)
            encode(event, mode).release();
    }

    private static ByteBuf encode(INetworkEvent event, int mode)
    {
        if (mode == 3)
            return NetworkMessage.encode(event, ALLOCATOR);

        ByteBuf body = ALLOCATOR.buffer();
        try
        {
            if (mode <= 1)
            {
                body.writeInt(NetworkEventMap.get(event.getClass()));
                event.write(body); // The exact event writer used before ObjectBuffer networking.
            }
            else
                body.writeBytes(EventObjectBuffer.encode(event));

            if (mode == 0 || mode == 2)
            {
                ByteBuf frame = ALLOCATOR.buffer(body.readableBytes() + 5);
                if (mode == 0)
                    frame.writeInt(body.readableBytes());
                else
                    NetworkFrameCodec.writeVarInt(frame, body.readableBytes() * 2);
                frame.writeBytes(body);
                return frame;
            }
            byte[] raw = new byte[body.readableBytes()];
            body.getBytes(body.readerIndex(), raw);
            byte[] compressed = raw.length >= NetworkMessage.COMPRESSION_THRESHOLD ? ZSTD.compress(raw) : null;
            if (compressed != null && compressed.length < raw.length - 8)
            {
                ByteBuf frame = ALLOCATOR.buffer(compressed.length + 12);
                frame.writeInt(compressed.length + 8).writeInt(-1).writeInt(raw.length).writeBytes(compressed);
                return frame;
            }
            ByteBuf frame = ALLOCATOR.buffer(raw.length + 4);
            frame.writeInt(raw.length).writeBytes(raw);
            return frame;
        }
        finally
        {
            body.release();
        }
    }

    static List<Scenario> scenarios() throws IOException
    {
        List<Scenario> scenarios = new ArrayList<>();
        List<INetworkEvent> movement = new ArrayList<>();
        for (int i = 0; i < 1000; i++)
        {
            EventTankUpdate event = new EventTankUpdate();
            event.tank = i % 12;
            event.posX = 100 + i * 0.35;
            event.posY = 200 + i * 0.27;
            event.vX = (i % 3) * 0.1;
            event.vY = (i % 5) * 0.1;
            event.angle = i * 0.01;
            movement.add(event);
        }
        scenarios.add(new Scenario("tank_updates", movement));

        List<INetworkEvent> small = new ArrayList<>();
        for (int i = 0; i < 100; i++)
        {
            small.add(new EventPing(i % 2 == 0));
            small.add(new EventChat("hello team " + i));
        }
        scenarios.add(new Scenario("ping_and_chat", small));

        List<INetworkEvent> setup = new ArrayList<>();
        setup.add(new EventSendClientDetails(Game.network_protocol,
            UUID.fromString("12345678-1234-1234-1234-123456789abc"), "benchmark_player"));
        EventSetItem item = new EventSetItem();
        item.playerID = UUID.fromString("abcdef01-1234-1234-1234-123456789abc");
        item.slot = 2;
        item.itemStackString = "/*TANKSON v1.1*/{\"obj_type\":\"item_stack\",\"name\":\"Cannon\",\"stack_size\":12}";
        setup.add(item);
        EventTankTransformCustom transform = new EventTankTransformCustom();
        transform.tankMusic = new HashSet<>(Arrays.asList("tank/track1.ogg", "tank/track2.ogg"));
        setup.add(transform);
        setup.add(new EventArcadeBonuses(
            new ScreenArcadeBonuses.Bonus("one", 1, 255, 0, 0),
            new ScreenArcadeBonuses.Bonus("two", 2, 0, 255, 0),
            new ScreenArcadeBonuses.Bonus("three", 3, 0, 0, 255)));
        scenarios.add(new Scenario("setup_and_custom", setup));

        List<INetworkEvent> levels = new ArrayList<>();
        try (ZipFile archive = new ZipFile("Tests.zip"))
        {
            Enumeration<? extends ZipEntry> entries = archive.entries();
            while (entries.hasMoreElements())
            {
                ZipEntry entry = entries.nextElement();
                if (!entry.getName().endsWith(".tanks"))
                    continue;
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                try (InputStream input = archive.getInputStream(entry))
                {
                    byte[] block = new byte[8192];
                    int count;
                    while ((count = input.read(block)) != -1)
                        bytes.write(block, 0, count);
                }
                String level = new String(bytes.toByteArray(), StandardCharsets.UTF_8);
                EventShareLevel shared = new EventShareLevel();
                shared.level = level;
                shared.name = entry.getName();
                levels.add(shared);
                levels.add(new EventUploadLevel(entry.getName(), level));
            }
        }
        scenarios.add(new Scenario("bundled_levels_share_and_upload", levels));
        return scenarios;
    }

    private static int count(List<Scenario> scenarios)
    {
        int count = 0;
        for (Scenario scenario: scenarios)
            count += scenario.events.size();
        return count;
    }

    private static double change(long before, long after)
    {
        return 100.0 * (after - before) / before;
    }

    private static String modeName(int mode)
    {
        return new String[]{"legacy", "legacy_zstd4", "objectbuffer", "objectbuffer_zstd4"}[mode];
    }

    static class Scenario
    {
        final String name;
        final List<INetworkEvent> events;

        Scenario(String name, List<INetworkEvent> events)
        {
            this.name = name;
            this.events = events;
        }
    }
}
