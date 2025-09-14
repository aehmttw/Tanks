package tanks.network.event;

import tanks.gui.screen.ScreenPartyLobby;
import tanks.network.ConnectedPlayer;

import java.util.UUID;

public class EventAnnounceConnection extends PersonalEvent
{
	public String name;
	public UUID clientIdTarget;
	public boolean joined;
	public boolean isBot;

	public EventAnnounceConnection()
	{
		
	}
	
	public EventAnnounceConnection(ConnectedPlayer p, boolean joined)
	{
		this.name = p.rawUsername;
		this.clientIdTarget = p.clientId;
		this.joined = joined;
		this.isBot = p.isBot;
	}

	@Override
	public void execute() 
	{
		if (this.clientID != null)
			return;

		if (this.joined)
		{
			ConnectedPlayer c = new ConnectedPlayer(this.clientIdTarget, this.name);
			if (isBot)
			{
				c.isBot = true;
				ScreenPartyLobby.connectedBots++;
				ScreenPartyLobby.connections.add(c);
			}
			else
				ScreenPartyLobby.connections.add(ScreenPartyLobby.connections.size() - ScreenPartyLobby.connectedBots, c);
		}
		else
		{
			ScreenPartyLobby.includedPlayers.remove(this.clientIdTarget);

			for (int i = 0; i < ScreenPartyLobby.connections.size(); i++)
			{
				if (ScreenPartyLobby.connections.get(i).clientId.equals(this.clientIdTarget))
				{
					ScreenPartyLobby.readyPlayers.remove(ScreenPartyLobby.connections.get(i));

					if (ScreenPartyLobby.connections.get(i).isBot)
						ScreenPartyLobby.connectedBots--;

					ScreenPartyLobby.connections.remove(i);
					i--;
				}
			}
		}
	}

}
