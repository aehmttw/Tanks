package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.*;
import tanks.gui.ChatMessage;
import tanks.gui.screen.ScreenPartyHost;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.network.NetworkUtils;

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

			ScreenPartyHost.activeScreen.sharedCrusades.add(new ScreenPartyHost.SharedCrusade(this.crusade, this.name, this.username));

			Game.eventsOut.add(this);

			String s = "\u00A7200000200255" + p.username + " has shared the crusade " + this.name.replace("_", " ") + "\u00A7000000000255";

			Drawing.drawing.playGlobalSound("join.ogg", 1.5f);
			ScreenPartyHost.chat.add(0, new ChatMessage(s));
			Game.eventsOut.add(new EventChat(s));
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
