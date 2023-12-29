package tanksonline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import tanks.network.event.INetworkEvent;
import tanks.network.event.online.EventAddMenuButton;
import tanks.network.event.online.EventRemoveMenuButton;
import tanks.gui.Button;
import tanks.gui.screen.ScreenOverlayOnline;
import tanks.network.NetworkEventMap;
import tanks.network.SynchronizedList;
import tanksonline.screen.ScreenLayout;

import java.util.UUID;

public class TanksOnlineServerHandler extends ChannelInboundHandlerAdapter
{
    public TanksOnlineMessageReader reader = new TanksOnlineMessageReader();
    public SynchronizedList<INetworkEvent> events = new SynchronizedList<>();

    public ChannelHandlerContext ctx;

    public TanksOnlineServer server;

    public UUID clientID;
    public UUID computerID;

    public String rawUsername;
    public String username;

    public long lastMessage = -1;
    public long latency = 0;

    public long latencySum = 0;
    public int latencyCount = 1;
    public long lastLatencyTime = 0;
    public long lastLatencyAverage = 0;

    public boolean closed = false;

    public Button[] menuButtons = new Button[ScreenOverlayOnline.max_button_count];

    public ScreenLayout screen;

    public TanksOnlineServerHandler(TanksOnlineServer s)
    {
        this.server = s;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx)
    {
        this.ctx = ctx;
        this.reader.queue = ctx.channel().alloc().buffer();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx)
    {
        ReferenceCountUtil.release(this.reader.queue);
        server.connections.remove(this);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
    {
        if (closed)
            return;

        this.ctx = ctx;
        ByteBuf buffy = (ByteBuf) msg;
        boolean reply = this.reader.queueMessage(this, buffy, this.clientID);
        ReferenceCountUtil.release(msg);

        if (reply)
        {
            if (lastMessage < 0)
                lastMessage = System.currentTimeMillis();

            long time = System.currentTimeMillis();
            latency = time - lastMessage;
            lastMessage = time;

            latencyCount++;
            latencySum += latency;

            if (time / 1000 > lastLatencyTime)
            {
                lastLatencyTime = time / 1000;
                lastLatencyAverage = latencySum / latencyCount;

                latencySum = 0;
                latencyCount = 0;
            }

            synchronized (this.events)
            {
                for (int i = 0; i < this.events.size(); i++)
                {
                    INetworkEvent e = this.events.get(i);
                    this.sendEvent(e);
                }

                this.events.clear();
            }
        }
    }

    public void sendEvent(INetworkEvent e)
    {
        ByteBuf b = ctx.channel().alloc().buffer();

        int i = NetworkEventMap.get(e.getClass());

        if (i == -1)
            throw new RuntimeException("The network event " + e.getClass() + " has not been registered!");

        b.writeInt(i);
        e.write(b);

        ByteBuf b2 = ctx.channel().alloc().buffer();
        b2.writeInt(b.readableBytes());
        b2.writeBytes(b);
        ctx.channel().writeAndFlush(b2);

        ReferenceCountUtil.release(b);
    }

    public void sendEventAndClose(INetworkEvent e)
    {
        this.closed = true;
        this.sendEvent(e);
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
    {
        cause.printStackTrace();

        ctx.close();
    }

    public void setMenuButton(int id, Button b, boolean unpause)
    {
        if (b == null)
        {
            this.sendEvent(new EventRemoveMenuButton(id));
            this.menuButtons[id] = null;
        }
        else
        {
            this.sendEvent(new EventAddMenuButton(id, b, unpause));
            this.menuButtons[id] = b;
        }
    }
}