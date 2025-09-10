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

    public void reply()
    {
        synchronized (this.events)
        {
            INetworkEvent prev = null;
            for (int i = 0; i < this.events.size(); i++)
            {
                INetworkEvent e = this.events.get(i);

                if (e instanceof IStackableEvent && ((IStackableEvent) e).isStackable())
                    this.stackedEvents.put(IStackableEvent.f(NetworkEventMap.get(e.getClass()) + IStackableEvent.f(((IStackableEvent) e).getIdentifier())), (IStackableEvent) e);
                else
                {
                    if (prev != null)
                        this.sendEvent(prev,false);

                    prev = e;
                }
            }

            long time = System.currentTimeMillis() * Game.networkRate / 1000;
            if (time != lastStackedEventSend)
            {
                lastStackedEventSend = time;
                int size = this.stackedEvents.size();

                if (prev != null)
                    this.sendEvent(prev, size == 0);

                for (IStackableEvent e: this.stackedEvents.values())
                {
                    size--;
                    this.sendEvent(e, size <= 0);
                }
                this.stackedEvents.clear();
            }
            else if (prev != null)
                this.sendEvent(prev, true);

            if (steamID == null)
                this.ctx.flush();

            this.events.clear();
        }
    }

    public synchronized void sendEvent(INetworkEvent e)
    {
        this.sendEvent(e, true);
    }

    public HashMap<String, Integer> eventFrequencies = new HashMap<>();

    public synchronized void sendEvent(INetworkEvent e, boolean flush)
    {
        eventFrequencies.putIfAbsent(e.getClass().getSimpleName(), 0);
        eventFrequencies.put(e.getClass().getSimpleName(), eventFrequencies.get(e.getClass().getSimpleName()) + 1);

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
