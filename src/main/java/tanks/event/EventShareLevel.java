package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.*;
import tanks.gui.ChatMessage;
import tanks.gui.screen.ScreenPartyHost;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.network.NetworkUtils;

public class EventShareLevel extends PersonalEvent
{
	public String level;
	public String name;
	public String username = "";

	public EventShareLevel()
	{

	}

	public EventShareLevel(Level l, String name)
	{
		this.level = l.levelString;
		this.name = name;
	}

	@Override
	public void execute() 
	{		
		if (this.clientID != null)
		{
			for (Player p: Game.players)
			{
				if (p.clientID.equals(this.clientID))
				{
					this.username = p.username;
					break;
				}
			}

			ScreenPartyHost.activeScreen.sharedLevels.add(new ScreenPartyHost.SharedLevel(this.level, this.name, this.username));

			Game.eventsOut.add(this);

			String s = "\u00A7200000200255" + this.username + " has shared the level " + this.name.replace("_", " ") + Colors.black;

			Drawing.drawing.playGlobalSound("join.ogg", 1.5f);
			ScreenPartyHost.chat.add(0, new ChatMessage(s));
			Game.eventsOut.add(new EventChat(s));
		}
		else
		{
			ScreenPartyLobby.sharedLevels.add(new ScreenPartyHost.SharedLevel(this.level, this.name, this.username));
		}
	}

	@Override
	public void write(ByteBuf b)
	{
		NetworkUtils.writeString(b, this.level);
		NetworkUtils.writeString(b, this.name);
		NetworkUtils.writeString(b, this.username);
	}

	@Override
	public void read(ByteBuf b)
	{
		this.level = NetworkUtils.readString(b);
		this.name = NetworkUtils.readString(b);
		this.username = NetworkUtils.readString(b);
	}
}
