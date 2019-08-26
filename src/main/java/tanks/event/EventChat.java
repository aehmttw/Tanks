package tanks.event;

import tanks.ChatMessage;
import tanks.Game;
import tanks.gui.screen.ScreenPartyHost;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.network.ServerHandler;

public class EventChat extends PersonalEvent
{
	public String message;

	public EventChat(String s)
	{
		this.message = s;
	}

	@Override
	public String getNetworkString()
	{
		return this.message;
	}

	@Override
	public void execute() 
	{
		//synchronized(ScreenPartyHost.server.connections)
		{
			if (this.clientID == null)
				ScreenPartyLobby.chat.add(0, new ChatMessage(this.message.replace("&", "\u00A7")));
			else
			{
				for (int i = 0; i < ScreenPartyHost.server.connections.size(); i++)
				{
					ServerHandler s = ScreenPartyHost.server.connections.get(i);

					if (s.clientID != null && s.clientID.equals(this.clientID))
					{
						ScreenPartyHost.chat.add(0, new ChatMessage(s.rawUsername, this.message.replace("&", "\u00A7")));
						Game.events.add(new EventPlayerChat(s.rawUsername, this.message));
					}
				}

			}
		}
	}
}
