package tanks.network;

import com.codedisaster.steamworks.*;
import io.netty.buffer.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import tanks.*;
import tanks.gui.screen.*;
import tanks.network.event.*;
import tanks.network.event.online.EventSendOnlineClientDetails;

import java.util.UUID;

public class ClientHandler extends NetworkHandler
{
	public long lastPingSent;
	public long lastLatency;

	public boolean online;

	public UUID connectionID;

	public ClientHandler(boolean online, UUID connectionID)
	{
		this.online = online;
		this.connectionID = connectionID;
        this.events = Game.eventsOut;
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

        ScreenPartyLobby.muted = false;
		ScreenPartyLobby.isClient = true;
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

    public synchronized void sendEventAndClose(INetworkEvent e)
	{
		if (steamID != null)
			Game.steamNetworkHandler.send(steamID.getAccountID(), e, SteamNetworking.P2PSend.Reliable);
		else
			this.sendEvent(e, true);

		ScreenPartyLobby.isClient = false;

		if (ctx != null)
			close();

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
    public void channelRead(ChannelHandlerContext ctx, Object msg)
	{
		this.ctx = ctx;
		ByteBuf buffy = (ByteBuf) msg;
		this.reader.queueMessage(this, buffy, null);
		ReferenceCountUtil.release(msg);
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e)
    {
		System.err.println("A network exception has occurred: " + e);
		Game.logger.println("A network exception has occurred: " + e);
		e.printStackTrace();
		e.printStackTrace(Game.logger);

		EventKick ev = new EventKick("A network exception has occurred: " + e);
		ev.clientID = null;
		Game.eventsIn.add(ev);

		ScreenPartyLobby.isClient = false;
        ctx.close();
    }
}
