package tanks.network;

import com.codedisaster.steamworks.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import it.unimi.dsi.fastutil.ints.*;
import tanks.*;
import tanks.network.event.*;

import java.util.*;

public abstract class NetworkHandler extends ChannelInboundHandlerAdapter
{
    public ChannelHandlerContext ctx;
    public SteamID steamID;

    protected SynchronizedList<INetworkEvent> events = new SynchronizedList<>();
    protected Int2IntLinkedOpenHashMap stackedEvents = new Int2IntLinkedOpenHashMap();

    public boolean joined = false, closed = false;

    public MessageReader reader = new MessageReader();

    protected long lastStackedEventSend = 0;

    public void reply()
    {
        long time = System.currentTimeMillis() * Game.networkRate / 1000;
        if (time == lastStackedEventSend)
            return;

        lastStackedEventSend = time;

        synchronized (this.events)
        {
            for (int i = 0; i < this.events.size(); i++)
            {
                INetworkEvent e = this.events.get(i);
                if (e instanceof IStackableEvent && ((IStackableEvent) e).isStackable())
                    stackedEvents.put(IStackableEvent.key((IStackableEvent) e), i);
            }
            for (int i = 0; i < this.events.size(); i++)
            {
                INetworkEvent e = this.events.get(i);
                if (e instanceof IStackableEvent && ((IStackableEvent) e).isStackable()
                    && stackedEvents.get(IStackableEvent.key((IStackableEvent) e)) != i)
                    continue;

                this.sendEvent(e, i == this.events.size() - 1);
            }

            if (steamID == null)
                this.ctx.flush();

            this.events.clear();
        }
    }

    public synchronized void sendEvent(INetworkEvent e)
    {
        this.sendEvent(e, true);
    }

    public synchronized void sendEvent(INetworkEvent e, boolean flush)
    {
        if (steamID != null)
        {
            SteamNetworking.P2PSend sendType = SteamNetworking.P2PSend.ReliableWithBuffering;

            if (flush)
                sendType = SteamNetworking.P2PSend.Reliable;

            Game.steamNetworkHandler.send(steamID.getAccountID(), e, sendType);
            return;
        }

        ByteBuf b = ctx.channel().alloc().buffer();

        int i = NetworkEventMap.get(e.getClass());
        if (i == -1)
            throw new RuntimeException("The network event " + e.getClass() + " has not been registered!");

        b.writeShort(i);
        e.write(b);

        int rb = b.readableBytes();
        ByteBuf b2 = ctx.channel().alloc().buffer();
        b2.writeInt(rb);
        MessageReader.upstreamBytes += rb + 4;
        if (Game.recordEventData)
            MessageReader.eventBytes.addTo(i, rb + 4);
        MessageReader.updateLastMessageTime();
        b2.writeBytes(b);

        if (flush)
            ctx.channel().writeAndFlush(b2);
        else
            ctx.channel().write(b2);

        ReferenceCountUtil.release(b);
    }

    public synchronized void sendEventAndClose(INetworkEvent e)
    {
        this.closed = true;
        if (steamID != null)
            Game.steamNetworkHandler.send(steamID.getAccountID(), e, SteamNetworking.P2PSend.Reliable);
        else
            this.sendEvent(e);

        if (ctx != null)
            ctx.close();

        if (steamID != null)
            Game.steamNetworkHandler.queueClose(steamID.getAccountID());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
    {
        cause.printStackTrace();

        ctx.close();
    }
}
