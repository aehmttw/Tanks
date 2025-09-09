package tanks.network;

import com.codedisaster.steamworks.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import tanks.*;
import tanks.network.event.*;

import java.util.*;

public abstract class NetworkHandler extends ChannelInboundHandlerAdapter
{
    public ChannelHandlerContext ctx;
    public SteamID steamID;

    protected SynchronizedList<INetworkEvent> events = new SynchronizedList<>();
    protected Int2ObjectOpenHashMap<IStackableEvent> stackedEvents = new Int2ObjectOpenHashMap<>();

    public boolean joined = false;
    public boolean closed = false;

    public MessageReader reader = new MessageReader();

    protected long lastStackedEventSend = 0;
    protected HashMap<Class<? extends INetworkEvent>, EventStackedGroup> groupedEvents = new HashMap<>();

    public synchronized void sendEventAndClose(INetworkEvent e)
    {
        this.closed = true;
        if (steamID != null)
            Game.steamNetworkHandler.send(steamID.getAccountID(), e, SteamNetworking.P2PSend.Reliable);
        else
            this.sendEvent(e, true);

        if (ctx != null)
            ctx.close();

        if (steamID != null)
            Game.steamNetworkHandler.queueClose(steamID.getAccountID());
    }

    public synchronized void sendEvent(INetworkEvent e)
    {
        sendEvent(e, true);
    }

    public synchronized void sendEvent(INetworkEvent e, boolean flush)
    {
        if (steamID != null)
            Game.steamNetworkHandler.send(steamID.getAccountID(), e, flush ?
                SteamNetworking.P2PSend.Reliable : SteamNetworking.P2PSend.ReliableWithBuffering);

        ByteBuf b = ctx.channel().alloc().buffer();

        int eventID = NetworkEventMap.get(e.getClass());
        if (eventID == -1)
            throw new RuntimeException("The network event " + e.getClass() + " has not been registered!");

        b.writeInt(eventID);
        e.write(b);

        ByteBuf b2 = ctx.channel().alloc().buffer();
        b2.writeInt(b.readableBytes());
        MessageReader.upstreamBytes += b.readableBytes() + 4;
        MessageReader.updateLastMessageTime();
        b2.writeBytes(b);

        if (flush)
            ctx.channel().writeAndFlush(b2);
        else
            ctx.channel().write(b2);

        ReferenceCountUtil.release(b);
    }

    public synchronized void stackEvent(INetworkEvent e)
    {
        groupedEvents.computeIfAbsent(e.getClass(),
            k -> new EventStackedGroup(e.getClass())).events.add(e);
    }

    public synchronized void flushEvents()
    {
        int i = 0;
        for (EventStackedGroup e : this.groupedEvents.values())
            sendEvent(e, i++ >= this.groupedEvents.size() - 1);
        groupedEvents.clear();
    }

    public void reply()
    {
        synchronized (this.events)
        {
            INetworkEvent prev = null;
            for (INetworkEvent e : this.events)
            {
                if (e instanceof IStackableEvent && ((IStackableEvent) e).isStackable())
                    this.stackedEvents.put(IStackableEvent.f(NetworkEventMap.get(e.getClass()) + IStackableEvent.f(((IStackableEvent) e).getIdentifier())), (IStackableEvent) e);
                else
                {
                    if (prev != null)
                        this.stackEvent(e);

                    prev = e;
                }
            }

            long time = System.currentTimeMillis() * Game.networkRate / 1000;
            if (time != lastStackedEventSend)
            {
                lastStackedEventSend = time;

                if (prev != null)
                    this.stackEvent(prev);

                for (IStackableEvent e : this.stackedEvents.values())
                    this.stackEvent(e);

                this.stackedEvents.clear();
                flushEvents();
            }
            else if (prev != null)
                this.stackEvent(prev);

            if (steamID == null)
                this.ctx.flush();

            this.events.clear();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
    {
        cause.printStackTrace();
        ctx.close();
    }
}
