package tanks.network;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import tanks.ChatMessage;
import tanks.Game;
import tanks.IPartyMenuScreen;
import tanks.event.EventAnnounceConnection;
import tanks.event.EventChat;
import tanks.event.EventConnectionSuccess;
import tanks.event.EventKick;
import tanks.event.EventUpdateReadyCount;
import tanks.event.INetworkEvent;
import tanks.gui.screen.ScreenPartyHost;

/**
 * Handles a server-side channel.
 */
public class ServerHandler extends ChannelInboundHandlerAdapter 
{
	public String message = "";
	public boolean reading = false;
	public MessageExecutor executor = new MessageExecutor();
	public ArrayList<INetworkEvent> events = new ArrayList<INetworkEvent>();

	public ChannelHandlerContext ctx;

	public Server server;

	public UUID clientID;
	public String rawUsername;
	public String username;

	public ServerHandler(Server s)
	{
		this.server = s;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) 
	{
		synchronized(server.connections)
		{
			server.connections.add(this);
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) 
	{
		synchronized(server.connections)
		{
			server.connections.remove(this);
		}

		ScreenPartyHost.readyPlayers.remove(this.clientID);
		Game.events.add(new EventUpdateReadyCount(ScreenPartyHost.readyPlayers.size()));
		Game.events.add(new EventAnnounceConnection(new ConnectedPlayer(this.clientID, this.rawUsername), false));

		Game.events.add(new EventChat("&000127255255" + this.username + " has left the party&000000000255"));
		ScreenPartyHost.chat.add(0, new ChatMessage("\u00A7000127255255" + this.username + " has left the party\u00A7000000000255"));
	}

	public void kick(ChannelHandlerContext ctx, String reason)
	{
		EventKick e = new EventKick(reason);
		String reply = "";
		reply += "<" + NetworkEventMap.get(EventKick.class) + "#" + e.getNetworkString() + ">";
		final ByteBuf buffy2 = Unpooled.wrappedBuffer((reply).getBytes());
		ctx.writeAndFlush(buffy2);
		ctx.close();
	}

	public void sendSuccess(ChannelHandlerContext ctx)
	{
		EventConnectionSuccess e = new EventConnectionSuccess();
		String reply = "";
		reply += "<" + NetworkEventMap.get(EventConnectionSuccess.class) + "#" + e.getNetworkString() + ">";
		final ByteBuf buffy2 = Unpooled.wrappedBuffer((reply).getBytes());
		ctx.writeAndFlush(buffy2);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) 
	{
		this.ctx = ctx;

		ByteBuf buffy = (ByteBuf) msg;

		String s = buffy.toString(Charset.defaultCharset());

		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);

			if (c == '<')
				reading = true;
			else if (c == '>')
			{
				reading = false;	

				if (message.startsWith("$"))
				{
					boolean success = true;

					if (!(Game.screen instanceof IPartyMenuScreen))
					{
						kick(ctx, "Please wait for party's current game to finish");
					}

					if (Game.network_protocol != Integer.parseInt(message.split("\\.")[0].substring(1)))
					{
						kick(ctx, "Incompatible Tanks version, please use " + Game.version);
						success = false;
					}

					this.clientID = UUID.fromString(message.split("\\.")[1]);
					this.rawUsername = message.split("\\.")[2];

					this.username = this.rawUsername;

					if (Game.enableChatFilter)
						this.username = Game.chatFilter.filterChat(this.username);

					//synchronized(server.connections)
					{
						for (int k = 0; k < server.connections.size(); k++)
						{
							ServerHandler connection = server.connections.get(k);

							if (connection != null && connection != this && connection.clientID.equals(this.clientID))
							{
								kick(ctx, "You are already connected!");
								success = false;
							}
						}


						if (success)
						{
							sendSuccess(ctx);
							Game.events.add(new EventAnnounceConnection(new ConnectedPlayer(this.clientID, this.rawUsername), true));

							this.events.add(new EventAnnounceConnection(new ConnectedPlayer(Game.clientID, Game.username), true));

							for (int k = 0; k < server.connections.size(); k++)
							{
								ServerHandler connection = server.connections.get(k);

								if (connection != this)
									this.events.add(new EventAnnounceConnection(new ConnectedPlayer(connection.clientID, connection.rawUsername), true));
							}

							Game.events.add(new EventChat("&000127255255" + this.username + " has joined the party&000000000255"));
							ScreenPartyHost.chat.add(0, new ChatMessage("\u00A7000127255255" + this.username + " has joined the party\u00A7000000000255"));
						}
					}
				}
				else
					executor.executeMessage(message, this.clientID);

				message = "";
			}
			else if (reading)
				message += c;
		}

		ReferenceCountUtil.release(msg);

		String reply = "<>";

		while (this.events.size() > 0)
		{
			INetworkEvent event = this.events.get(0);

			if (event != null)
			{
				reply += "<" + NetworkEventMap.get(event.getClass()) + "#" + event.getNetworkString() + ">";
			}

			this.events.remove(event);
		}

		final ByteBuf buffy2 = Unpooled.wrappedBuffer((reply).getBytes());
		ctx.writeAndFlush(buffy2);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) 
	{
		// Close the connection when an exception is raised.
		cause.printStackTrace();
		ctx.close();
	}
}