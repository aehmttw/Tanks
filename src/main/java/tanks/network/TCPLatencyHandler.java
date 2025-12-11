package tanks.network;

import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;

import java.nio.channels.ClosedChannelException;
import java.util.*;
import java.util.concurrent.*;

/**
 * TCP-only, preserves order even with jitter by monotonic due-times.
 * Written by AI
 */
public final class TCPLatencyHandler extends ChannelDuplexHandler
{
    private final int oneWayMs;
    private final int jitterMs;

    public TCPLatencyHandler(int oneWayMs, int jitterMs)
    {
        this.oneWayMs = Math.max(0, oneWayMs);
        this.jitterMs = Math.max(0, jitterMs);
    }

    // -------- Outbound (write/flush) --------
    private static final class OutItem
    {
        final Object msg;
        final ChannelPromise promise;
        final boolean isFlush;
        final long dueNs;

        OutItem(Object msg, ChannelPromise p, boolean isFlush, long dueNs)
        {
            this.msg = msg;
            this.promise = p;
            this.isFlush = isFlush;
            this.dueNs = dueNs;
        }
    }

    private final Deque<OutItem> outQ = new ArrayDeque<>();
    private boolean outScheduled = false;
    private long lastOutDue = 0L;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise p)
    {
        enqueueOutbound(ctx, new OutItem(msg, p, false, nextOutDue()));
        scheduleOutbound(ctx);
    }

    @Override
    public void flush(ChannelHandlerContext ctx)
    {
        enqueueOutbound(ctx, new OutItem(null, null, true, nextOutDue()));
        scheduleOutbound(ctx);
    }

    private long nextOutDue()
    {
        long now = System.nanoTime();
        long jitter = (jitterMs == 0) ? 0 : ThreadLocalRandom.current().nextInt(-jitterMs, jitterMs + 1);
        long due = now + TimeUnit.MILLISECONDS.toNanos(Math.max(0, oneWayMs + jitter));
        if (due < lastOutDue) due = lastOutDue; // enforce monotonicity => preserves order
        return lastOutDue = due;
    }

    private void enqueueOutbound(ChannelHandlerContext ctx, OutItem it)
    {
        outQ.addLast(it);
    }

    private void scheduleOutbound(ChannelHandlerContext ctx)
    {
        if (outScheduled) return;
        outScheduled = true;
        drainOutbound(ctx);
    }

    private void drainOutbound(ChannelHandlerContext ctx)
    {
        Runnable task = new Runnable()
        {
            @Override
            public void run()
            {
                outScheduled = false;
                long now = System.nanoTime();
                // Run all items that are due
                while (!outQ.isEmpty() && outQ.peekFirst().dueNs <= now)
                {
                    OutItem it = outQ.pollFirst();
                    if (it.isFlush)
                    {
                        ctx.flush();
                    }
                    else
                    {
                        ctx.write(it.msg, it.promise);
                    }
                }
                if (!outQ.isEmpty())
                {
                    long delayNs = Math.max(0, outQ.peekFirst().dueNs - System.nanoTime());
                    outScheduled = true;
                    ctx.executor().schedule(this, delayNs, TimeUnit.NANOSECONDS);
                }
            }
        };
        long delayNs = outQ.isEmpty() ? 0 : Math.max(0, outQ.peekFirst().dueNs - System.nanoTime());
        ctx.executor().schedule(task, delayNs, TimeUnit.NANOSECONDS);
    }

    // -------- Inbound (read) --------
    private static final class InItem
    {
        final Object msg;
        final long dueNs;

        InItem(Object msg, long dueNs)
        {
            this.msg = msg;
            this.dueNs = dueNs;
        }
    }

    private final Deque<InItem> inQ = new ArrayDeque<>();
    private boolean inScheduled = false;
    private long lastInDue = 0L;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
    {
        long now = System.nanoTime();
        long jitter = (jitterMs == 0) ? 0 : ThreadLocalRandom.current().nextInt(-jitterMs, jitterMs + 1);
        long due = now + TimeUnit.MILLISECONDS.toNanos(Math.max(0, oneWayMs + jitter));
        if (due < lastInDue) due = lastInDue; // preserve arrival order
        lastInDue = due;

        inQ.addLast(new InItem(msg, due));
        scheduleInbound(ctx);
    }

    private void scheduleInbound(ChannelHandlerContext ctx)
    {
        if (inScheduled) return;
        inScheduled = true;
        drainInbound(ctx);
    }

    private void drainInbound(ChannelHandlerContext ctx)
    {
        Runnable task = new Runnable()
        {
            @Override
            public void run()
            {
                inScheduled = false;
                long now = System.nanoTime();
                while (!inQ.isEmpty() && inQ.peekFirst().dueNs <= now)
                {
                    InItem it = inQ.pollFirst();
                    ctx.fireChannelRead(it.msg);
                }
                if (!inQ.isEmpty())
                {
                    long delayNs = Math.max(0, inQ.peekFirst().dueNs - System.nanoTime());
                    inScheduled = true;
                    ctx.executor().schedule(this, delayNs, TimeUnit.NANOSECONDS);
                }
            }
        };
        long delayNs = inQ.isEmpty() ? 0 : Math.max(0, inQ.peekFirst().dueNs - System.nanoTime());
        ctx.executor().schedule(task, delayNs, TimeUnit.NANOSECONDS);
    }

    // -------- Cleanup --------
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        // Release any buffered ByteBufs on close to avoid leaks
        while (!inQ.isEmpty()) ReferenceCountUtil.release(inQ.pollFirst().msg);
        while (!outQ.isEmpty())
        {
            OutItem it = outQ.pollFirst();
            if (it.msg != null) ReferenceCountUtil.release(it.msg);
            if (it.promise != null && !it.promise.isDone()) it.promise.setFailure(new ClosedChannelException());
        }
        super.channelInactive(ctx);
    }
}
