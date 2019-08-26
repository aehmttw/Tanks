package tanks.event;

import java.util.UUID;

import tanks.gui.screen.ScreenPartyLobby;
import tanks.network.ConnectedPlayer;

public class EventAnnounceConnection implements INetworkEvent
{
	public ConnectedPlayer player;
	public boolean joined;
	
	public EventAnnounceConnection(ConnectedPlayer p, boolean joined)
	{
		this.player = p;
		this.joined = joined;
	}
	
	public EventAnnounceConnection(String s)
	{
		String[] sp = s.split(",");
		this.joined = Boolean.parseBoolean(sp[0]);
		this.player = new ConnectedPlayer(UUID.fromString(sp[1]), sp[2]);
	}
	
	
	@Override
	public String getNetworkString() 
	{
		return this.joined + "," + this.player.clientId + "," + this.player.rawUsername;
	}

	@Override
	public void execute() 
	{
		if (this.joined)
		{
			ScreenPartyLobby.connections.add(this.player);
		}
		else
		{
			for (int i = 0; i < ScreenPartyLobby.connections.size(); i++)
			{
				if (ScreenPartyLobby.connections.get(i).clientId.equals(player.clientId))
				{
					ScreenPartyLobby.connections.remove(i);
					i--;
				}
			}
		}
	}

}
