package tanks.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import tanks.Game;
import tanks.event.*;
import tanks.gui.ChatMessage;
import tanks.gui.screen.ScreenPartyHost;

import java.util.UUID;

public class ServerHandler extends ChannelInboundHandlerAdapter
{
	public MessageReader reader = new MessageReader();
	public SynchronizedList<INetworkEvent> events = new SynchronizedList<INetworkEvent>();

	public ChannelHandlerContext ctx;

	public Server server;

	public UUID clientID;
	public String rawUsername;
	public String username;

	public long lastMessage = -1;
	public long latency = 0;

	public long latencySum = 0;
	public int latencyCount = 1;
	public long lastLatencyTime = 0;
	public long lastLatencyAverage = 0;

	public boolean closed = false;

	public ServerHandler(Server s)
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

		if (ScreenPartyHost.isServer)
		{
			ScreenPartyHost.includedPlayers.remove(this.clientID);
			ScreenPartyHost.readyPlayers.remove(this.clientID);
			ScreenPartyHost.disconnectedPlayers.add(this.clientID);

			if (this.clientID != null)
			{
				Game.eventsOut.add(new EventUpdateReadyCount(ScreenPartyHost.readyPlayers.size()));
				Game.eventsOut.add(new EventAnnounceConnection(new ConnectedPlayer(this.clientID, this.rawUsername), false));
				Game.eventsOut.add(new EventChat("\u00A7000127255255" + this.username + " has left the party\u00A7000000000255"));

				ScreenPartyHost.chat.add(0, new ChatMessage("\u00A7000127255255" + this.username + " has left the party\u00A7000000000255"));
			}
		}
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
			this.reader.frame++;

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

			this.reply();
		}
	}

	public void reply()
	{
		synchronized (this.events)
		{
			EventKeepConnectionAlive k = new EventKeepConnectionAlive();
			this.events.add(k);

			for (int i = 0; i < this.events.size(); i++)
			{
				INetworkEvent e = this.events.get(i);
				this.sendEvent(e);
			}

			this.events.clear();
		}
	}

	public synchronized void sendEvent(INetworkEvent e)
	{
		ByteBuf b = ctx.channel().alloc().buffer();
		b.writeInt(NetworkEventMap.get(e.getClass()));
		e.write(b);

		ByteBuf b2 = ctx.channel().alloc().buffer();
		b2.writeInt(b.readableBytes());
		b2.writeBytes(b);
		ctx.channel().writeAndFlush(b2);

		ReferenceCountUtil.release(b);
	}

	public synchronized void sendEventAndClose(INetworkEvent e)
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
}