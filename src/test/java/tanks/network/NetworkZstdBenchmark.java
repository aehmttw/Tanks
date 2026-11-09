package tanks.network;

import tanks.Game;
import tanks.compression.ZstdCompression;
import tanks.network.event.INetworkEvent;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** Run with ./gradlew benchmarkNetworkZstd on Java 8. */
public final class NetworkZstdBenchmark
{
    private static final ByteBufAllocator ALLOCATOR = UnpooledByteBufAllocator.DEFAULT;
    private static final NetworkFrameCodec FRAMES = new NetworkFrameCodec(new ZstdCompression(4),
        NetworkMessage.COMPRESSION_THRESHOLD, MessageReader.max_event_size);
    private static volatile INetworkEvent decodedSink;
    private static volatile int decodedLengthSink;

    private NetworkZstdBenchmark()
    {
    }

    public static void main(String[] args) throws IOException
    {
        Game.debug = false;
        Game.registerEvents();
        List<NetworkWireBenchmark.Scenario> scenarios = NetworkWireBenchmark.scenarios();
        File reportPath = new File("build/reports/network-zstd.csv");
        if (!reportPath.getParentFile().exists() && !reportPath.getParentFile().mkdirs())
            throw new IOException("Unable to create benchmark report directory");
        System.out.println("Java " + System.getProperty("java.version"));
        List<INetworkEvent> all = new ArrayList<>();
        try (PrintWriter report = new PrintWriter(new OutputStreamWriter(new FileOutputStream(reportPath), StandardCharsets.UTF_8)))
        {
            row(report, "scenario,events,zstd_off_bytes,zstd_on_bytes,byte_reduction_pct,zstd_off_encode_ms,zstd_on_encode_ms,encode_extra_pct,zstd_off_decode_ms,zstd_on_decode_ms,decode_extra_pct");
            ArrayList<INetworkEvent> withoutLevels = new ArrayList<>();
            for (NetworkWireBenchmark.Scenario scenario: scenarios)
            {
                measure(report, scenario.name, scenario.events);
                all.addAll(scenario.events);
                if (!scenario.name.equals("bundled_levels_share_and_upload"))
                    withoutLevels.addAll(scenario.events);
            }
            measure(report, "without_levels", withoutLevels);
            measure(report, "TOTAL", all);
        }
        System.out.println("Report: " + reportPath.getPath());
        frameCost(all);
    }

    private static void measure(PrintWriter report, String name, List<INetworkEvent> events)
    {
        byte[][] raw = frames(events, false);
        byte[][] compressed = frames(events, true);
        long rawBytes = byteCount(raw);
        long compressedBytes = byteCount(compressed);
        for (int i = 0; i < 5; i++)
        {
            encode(events, false);
            encode(events, true);
            decode(raw);
            decode(compressed);
        }
        double[] encodeTimes = timedEncode(events);
        double[] decodeTimes = timedDecode(raw, compressed);
        row(report, String.format(Locale.ROOT, "%s,%d,%d,%d,%.1f,%.3f,%.3f,%.1f,%.3f,%.3f,%.1f",
            name, events.size(), rawBytes, compressedBytes,
            rawBytes == compressedBytes ? 0 : -percentage(rawBytes, compressedBytes),
            encodeTimes[0], encodeTimes[1], percentage(encodeTimes[0], encodeTimes[1]),
            decodeTimes[0], decodeTimes[1], percentage(decodeTimes[0], decodeTimes[1])));
    }

    private static double[] timedEncode(List<INetworkEvent> events)
    {
        double[][] results = new double[2][9];
        for (int sample = 0; sample < 9; sample++)
        {
            for (int order = 0; order < 2; order++)
            {
                int mode = (sample + order) % 2;
                long start = System.nanoTime();
                for (int repeat = 0; repeat < 5; repeat++)
                    encode(events, mode == 1);
                results[mode][sample] = (System.nanoTime() - start) / 5e6;
            }
        }
        return medians(results);
    }

    private static double[] timedDecode(byte[][] raw, byte[][] compressed)
    {
        double[][] results = new double[2][9];
        for (int sample = 0; sample < 9; sample++)
        {
            for (int order = 0; order < 2; order++)
            {
                int mode = (sample + order) % 2;
                long start = System.nanoTime();
                for (int repeat = 0; repeat < 5; repeat++)
                    decode(mode == 1 ? compressed : raw);
                results[mode][sample] = (System.nanoTime() - start) / 5e6;
            }
        }
        return medians(results);
    }

    private static double[] medians(double[][] samples)
    {
        double[] result = new double[2];
        for (int mode = 0; mode < 2; mode++)
        {
            Arrays.sort(samples[mode]);
            result[mode] = samples[mode][4];
        }
        return result;
    }

    private static byte[][] frames(List<INetworkEvent> events, boolean zstd)
    {
        byte[][] frames = new byte[events.size()][];
        for (int i = 0; i < events.size(); i++)
        {
            ByteBuf frame = NetworkMessage.encode(events.get(i), ALLOCATOR, zstd);
            try
            {
                frames[i] = new byte[frame.readableBytes()];
                frame.readBytes(frames[i]);
            }
            finally
            {
                frame.release();
            }
        }
        return frames;
    }

    private static long byteCount(byte[][] frames)
    {
        long count = 0;
        for (byte[] frame: frames)
            count += frame.length;
        return count;
    }

    private static void encode(List<INetworkEvent> events, boolean zstd)
    {
        for (INetworkEvent event: events)
            NetworkMessage.encode(event, ALLOCATOR, zstd).release();
    }

    private static void decode(byte[][] frames)
    {
        for (byte[] bytes: frames)
        {
            ByteBuf frame = Unpooled.wrappedBuffer(bytes);
            try
            {
                ByteBuf body = NetworkMessage.tryDecode(frame);
                if (body == null)
                    throw new IllegalStateException("Incomplete benchmark frame");
                try
                {
                    decodedSink = EventObjectBuffer.decode(body);
                }
                finally
                {
                    body.release();
                }
            }
            finally
            {
                frame.release();
            }
        }
    }

    /** Isolates frame compression from ObjectBuffer conversion and event field reflection. */
    private static void frameCost(List<INetworkEvent> events) throws IOException
    {
        byte[][] bodies = new byte[events.size()][];
        for (int i = 0; i < bodies.length; i++)
            bodies[i] = EventObjectBuffer.encode(events.get(i));
        byte[][] raw = frameBytes(bodies, false);
        byte[][] compressed = frameBytes(bodies, true);
        for (int i = 0; i < 5; i++)
        {
            frameEncode(bodies, false);
            frameEncode(bodies, true);
            frameDecode(raw);
            frameDecode(compressed);
        }
        double[][] encode = new double[2][9];
        double[][] decode = new double[2][9];
        for (int sample = 0; sample < 9; sample++)
        {
            for (int order = 0; order < 2; order++)
            {
                int mode = (sample + order) % 2;
                long start = System.nanoTime();
                for (int repeat = 0; repeat < 10; repeat++)
                    frameEncode(bodies, mode == 1);
                encode[mode][sample] = (System.nanoTime() - start) / 1e7;

                start = System.nanoTime();
                for (int repeat = 0; repeat < 10; repeat++)
                    frameDecode(mode == 1 ? compressed : raw);
                decode[mode][sample] = (System.nanoTime() - start) / 1e7;
            }
        }
        double[] encodeMedian = medians(encode);
        double[] decodeMedian = medians(decode);
        File reportPath = new File("build/reports/network-zstd-framing.csv");
        try (PrintWriter report = new PrintWriter(new OutputStreamWriter(new FileOutputStream(reportPath), StandardCharsets.UTF_8)))
        {
            report.println("events,zstd_off_frame_encode_ms,zstd_on_frame_encode_ms,zstd_extra_frame_encode_ms,zstd_off_frame_decode_ms,zstd_on_frame_decode_ms,zstd_extra_frame_decode_ms");
            report.printf(Locale.ROOT, "%d,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f%n", events.size(),
                encodeMedian[0], encodeMedian[1], encodeMedian[1] - encodeMedian[0],
                decodeMedian[0], decodeMedian[1], decodeMedian[1] - decodeMedian[0]);
        }
        System.out.printf(Locale.ROOT, "frame_only_encode_ms,zstd_off=%.3f,zstd_on=%.3f,extra=%.3f%n",
            encodeMedian[0], encodeMedian[1], encodeMedian[1] - encodeMedian[0]);
        System.out.printf(Locale.ROOT, "frame_only_decode_ms,zstd_off=%.3f,zstd_on=%.3f,extra=%.3f%n",
            decodeMedian[0], decodeMedian[1], decodeMedian[1] - decodeMedian[0]);
        System.out.println("Report: " + reportPath.getPath());
    }

    private static byte[][] frameBytes(byte[][] bodies, boolean zstd)
    {
        byte[][] result = new byte[bodies.length][];
        for (int i = 0; i < bodies.length; i++)
        {
            ByteBuf body = Unpooled.wrappedBuffer(bodies[i]);
            try
            {
                ByteBuf frame = FRAMES.encode(body, ALLOCATOR, zstd);
                try
                {
                    result[i] = new byte[frame.readableBytes()];
                    frame.readBytes(result[i]);
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
        return result;
    }

    private static void frameEncode(byte[][] bodies, boolean zstd)
    {
        for (byte[] bytes: bodies)
        {
            ByteBuf body = Unpooled.wrappedBuffer(bytes);
            try
            {
                FRAMES.encode(body, ALLOCATOR, zstd).release();
            }
            finally
            {
                body.release();
            }
        }
    }

    private static void frameDecode(byte[][] frames)
    {
        for (byte[] bytes: frames)
        {
            ByteBuf frame = Unpooled.wrappedBuffer(bytes);
            try
            {
                ByteBuf body = FRAMES.tryDecode(frame);
                try
                {
                    decodedLengthSink = body.readableBytes();
                }
                finally
                {
                    body.release();
                }
            }
            finally
            {
                frame.release();
            }
        }
    }

    private static double percentage(double before, double after)
    {
        return 100 * (after - before) / before;
    }

    private static void row(PrintWriter report, String value)
    {
        report.println(value);
        System.out.println(value);
    }
}
