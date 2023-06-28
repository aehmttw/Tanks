package tanks.network;

import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamNetworking;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import tanks.Crusade;
import tanks.Game;
import tanks.Panel;
import tanks.network.event.*;
import tanks.network.event.online.EventSendOnlineClientDetails;
import tanks.gui.screen.ScreenOverlayOnline;
import tanks.gui.screen.ScreenPartyLobby;

import java.util.HashMap;
import java.util.UUID;

public class ClientHandler extends ChannelInboundHandlerAdapter 
{	
	public String message = "";
	public MessageReader reader = new MessageReader();
	protected HashMap<Integer, IStackableEvent> stackedEvents = new HashMap<>();
	protected long lastStackedEventSend = 0;

	public ChannelHandlerContext ctx;
	public SteamID steamID;

	public static long lastMessage = -1;
	public static long latency = 0;

	public static long latencySum = 0;
	public static int latencyCount = 1;
	public static long lastLatencyTime = 0;
	public static long lastLatencyAverage = 0;

	public boolean online;

	public UUID connectionID;

	public ClientHandler(boolean online, UUID connectionID)
	{
		this.online = online;
		this.connectionID = connectionID;
	}

	@Override
    public void channelActive(ChannelHandlerContext ctx)
    {
    	if (this.connectionID != Client.connectionID)
		{
			ScreenPartyLobby.isClient = false;

			if (ctx != null)
				ctx.close();

			return;
		}

    	if (this.online)
		{
			Game.connectedToOnline = true;
			Panel.panel.onlineOverlay = new ScreenOverlayOnline();
		}

		if (ctx != null)
			this.reader.queue = ctx.channel().alloc().buffer();
		else
			this.reader.queue = Unpooled.buffer();

		this.ctx = ctx;

		if (this.steamID == null)
		{
			if (online)
				this.sendEvent(new EventSendOnlineClientDetails(Game.network_protocol, Game.clientID, Game.player.username, Game.computerID));
			else
				this.sendEvent(new EventSendClientDetails(Game.network_protocol, Game.clientID, Game.player.username));
		}

		ScreenPartyLobby.isClient = true;

		this.sendEvent(new EventPing());
    }

    public void close()
	{
		if (Client.handler.ctx != null)
			Client.handler.ctx.close();
		else if (Client.handler.steamID != null)
		{
			Game.steamNetworkHandler.queueClose(Client.handler.steamID.getAccountID());
			Client.handler.channelInactive(null);
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
		if (steamID != null)
			Game.steamNetworkHandler.send(steamID.getAccountID(), e, SteamNetworking.P2PSend.Reliable);
		else
			this.sendEvent(e);

		ScreenPartyLobby.isClient = false;

		if (ctx != null)
			ctx.close();

		if (steamID != null)
			Game.steamNetworkHandler.queueClose(steamID.getAccountID());
	}

	@Override
    public void channelInactive(ChannelHandlerContext ctx)
    {
		if (ScreenPartyLobby.isClient)
		{
			EventKick e = new EventKick("You have lost connection");
			e.clientID = null;
			Game.eventsIn.add(e);
		}

		ScreenPartyLobby.isClient = false;
    	Game.connectedToOnline = false;

		Crusade.crusadeMode = false;
		Crusade.currentCrusade = null;

		if (steamID == null)
			ReferenceCountUtil.release(this.reader.queue);

		Client.connectionID = null;
    }
	
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws InterruptedException
	{
		this.ctx = ctx;
		ByteBuf buffy = (ByteBuf) msg;
		boolean reply = this.reader.queueMessage(buffy, null);
		ReferenceCountUtil.release(msg);

		//Thread.sleep(150);

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

			this.sendEvent(new EventPing());
		}
    }

    public void reply()
	{
		synchronized (Game.eventsOut)
		{
			INetworkEvent prev = null;
			for (int i = 0; i < Game.eventsOut.size(); i++)
			{
				INetworkEvent e = Game.eventsOut.get(i);

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
				int size = this.stackedEvents.values().size();

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

			Game.eventsOut.clear();
		}
	}
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e)
    {
		System.err.println("A network exception has occurred: " + e.toString());
		Game.logger.println("A network exception has occurred: " + e.toString());
		e.printStackTrace();
		e.printStackTrace(Game.logger);

		EventKick ev = new EventKick("A network exception has occurred: " + e.toString());
		ev.clientID = null;
		Game.eventsIn.add(ev);

		ScreenPartyLobby.isClient = false;
        ctx.close();
    }
}