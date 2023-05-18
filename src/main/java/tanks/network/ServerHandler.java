package tanks.network;

import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamNetworking;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import tanks.Game;
import tanks.Player;
import tanks.gui.ChatMessage;
import tanks.gui.screen.ScreenPartyHost;
import tanks.network.event.*;

import java.util.HashMap;
import java.util.UUID;

public class ServerHandler extends ChannelInboundHandlerAdapter
{
	public MessageReader reader = new MessageReader();
	public SynchronizedList<INetworkEvent> events = new SynchronizedList<>();

	public ChannelHandlerContext ctx;
	public SteamID steamID;

	public Server server;

	public boolean initialized = false;

	public Player player;
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

		if (ctx != null)
			this.reader.queue = ctx.channel().alloc().buffer();
		else
			this.reader.queue = Unpooled.buffer();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx)
	{
		if (steamID == null)
			ReferenceCountUtil.release(this.reader.queue);

		server.connections.remove(this);

		if (ScreenPartyHost.isServer)
		{
			ScreenPartyHost.includedPlayers.remove(this.clientID);
			ScreenPartyHost.disconnectedPlayers.add(this.clientID);

			if (this.clientID != null)
			{
				Game.eventsOut.add(new EventUpdateReadyPlayers(ScreenPartyHost.readyPlayers));
				Game.eventsOut.add(new EventAnnounceConnection(new ConnectedPlayer(this.clientID, this.rawUsername), false));
				Game.eventsOut.add(new EventChat("\u00A7000127255255" + this.username + " has left the party\u00A7000000000255"));
				Game.eventsOut.add(new EventPlaySound("leave.ogg", 1.0f, 1.0f));

				Game.eventsIn.add(new EventPlaySound("leave.ogg", 1.0f, 1.0f));
				ScreenPartyHost.chat.add(0, new ChatMessage("\u00A7000127255255" + this.username + " has left the party\u00A7000000000255"));
			}
		}

		//System.out.println(eventFrequencies);

		for (String s: eventFrequencies.keySet())
		{
			System.out.println(s + ": " + eventFrequencies.get(s));
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

		if (steamID == null)
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

			//this.sendEvent(new EventPing());
		}
	}

	public void reply()
	{
		synchronized (this.events)
		{
			for (int i = 0; i < this.events.size(); i++)
			{
				INetworkEvent e = this.events.get(i);
				this.sendEvent(e, i >= this.events.size() - 1);
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

		b.writeInt(i);
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