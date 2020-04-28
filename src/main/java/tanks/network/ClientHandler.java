package tanks.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import tanks.Game;
import tanks.Panel;
import tanks.event.EventKeepConnectionAlive;
import tanks.event.EventSendClientDetails;
import tanks.event.INetworkEvent;
import tanks.event.online.EventSendOnlineClientDetails;
import tanks.gui.screen.ScreenKicked;
import tanks.gui.screen.ScreenOverlayOnline;
import tanks.gui.screen.ScreenPartyLobby;

public class ClientHandler extends ChannelInboundHandlerAdapter 
{	
	public String message = "";
	public MessageReader reader = new MessageReader();
	
	public ChannelHandlerContext ctx;

	public static long lastMessage = -1;
	public static long latency = 0;

	public static long latencySum = 0;
	public static int latencyCount = 1;
	public static long lastLatencyTime = 0;
	public static long lastLatencyAverage = 0;

	public boolean online;

	public ClientHandler(boolean online)
	{
		this.online = online;
	}

	@Override
    public void channelActive(ChannelHandlerContext ctx)
    {
    	if (this.online)
		{
			Game.connectedToOnline = true;
			Panel.panel.onlineOverlay = new ScreenOverlayOnline();
		}

		this.reader.queue = ctx.channel().alloc().buffer();
		this.ctx = ctx;

		if (online)
			this.sendEvent(new EventSendOnlineClientDetails(Game.network_protocol, Game.clientID, Game.player.username, Game.computerID));
		else
			this.sendEvent(new EventSendClientDetails(Game.network_protocol, Game.clientID, Game.player.username));

		ScreenPartyLobby.isClient = true;

		this.sendEvent(new EventKeepConnectionAlive());
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
		this.sendEvent(e);
		ScreenPartyLobby.isClient = false;
		ctx.close();
	}

	
	@Override
    public void channelInactive(ChannelHandlerContext ctx)
    {
    	if (ScreenPartyLobby.isClient)
		{
			Game.screen = new ScreenKicked("You have lost connection");
		}

		ScreenPartyLobby.isClient = false;
    	Game.connectedToOnline = false;
		ReferenceCountUtil.release(this.reader.queue);
    }
	
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
    {
    	this.ctx = ctx;
		ByteBuf buffy = (ByteBuf) msg;
		boolean reply = this.reader.queueMessage(buffy, null);
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

			reply();
		}	
    }

    public void reply()
	{
		synchronized (Game.eventsOut)
		{
			EventKeepConnectionAlive k = new EventKeepConnectionAlive();
			Game.eventsOut.add(k);

			for (int i = 0; i < Game.eventsOut.size(); i++)
			{
				INetworkEvent e = Game.eventsOut.get(i);
				this.sendEvent(e);
			}

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
		Game.screen = new ScreenKicked("A network exception has occurred: " + e.toString());
		ScreenPartyLobby.isClient = false;
        ctx.close();
    }
}