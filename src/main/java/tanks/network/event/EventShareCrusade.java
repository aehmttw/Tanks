package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Crusade;
import tanks.Drawing;
import tanks.Game;
import tanks.Player;
import tanks.gui.ChatMessage;
import tanks.gui.screen.ScreenPartyHost;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.network.NetworkUtils;
import tanks.network.ServerHandler;

public class EventShareCrusade extends PersonalEvent
{
	public String crusade;
	public String name;
	public String username = "";

	public EventShareCrusade()
	{

	}

	public EventShareCrusade(Crusade c, String name)
	{
		this.crusade = c.contents;
		this.name = name;
	}

	@Override
	public void execute() 
	{		
		if (this.clientID != null)
		{
			Player p = null;

			for (Player pl: Game.players)
			{
				if (pl.clientID.equals(this.clientID))
				{
					p = pl;
					break;
				}
			}

			this.username = p.username;

			if (!EventChat.isStringValid(name))
			{
				for (int i = 0; i < ScreenPartyHost.server.connections.size(); i++)
				{
					ServerHandler s = ScreenPartyHost.server.connections.get(i);

					if (s.clientID != null && s.clientID.equals(this.clientID))
						s.sendEventAndClose(new EventKick("Invalid crusade name received!"));
				}
			}
			else
			{
				ScreenPartyHost.activeScreen.sharedCrusades.add(new ScreenPartyHost.SharedCrusade(this.crusade, this.name, this.username));
				Game.eventsOut.add(this);

				String s = "\u00A7200000200255" + p.username + " has shared the crusade " + this.name.replace("_", " ") + "\u00A7000000000255";

				Drawing.drawing.playGlobalSound("join.ogg", 1.5f);
				ScreenPartyHost.chat.add(0, new ChatMessage(s));
				Game.eventsOut.add(new EventChat(s));
			}
		}
		else
		{
			ScreenPartyLobby.sharedCrusades.add(new ScreenPartyHost.SharedCrusade(this.crusade, this.name, this.username));
		}
	}

	@Override
	public void write(ByteBuf b)
	{
		NetworkUtils.writeString(b, this.crusade);
		NetworkUtils.writeString(b, this.name);
		NetworkUtils.writeString(b, this.username);
	}

	@Override
	public void read(ByteBuf b)
	{
		this.crusade = NetworkUtils.readString(b);
		this.name = NetworkUtils.readString(b);
		this.username = NetworkUtils.readString(b);
	}
}
