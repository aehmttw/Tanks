package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Player;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.network.ConnectedPlayer;
import tanks.network.NetworkUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class EventUpdateReadyPlayers extends PersonalEvent
{
	public String readyPlayers;

	public EventUpdateReadyPlayers()
	{
		
	}
	
	public EventUpdateReadyPlayers(ArrayList<Player> players)
	{
		StringBuilder s = new StringBuilder();
		for (Player p: players)
			s.append(p.clientID).append(",");

		if (players.size() == 0)
			readyPlayers = "";
		else
			readyPlayers = s.substring(0, s.length() - 1);
	}

	@Override
	public void execute()
	{
		if (this.clientID == null)
		{
			ScreenPartyLobby.readyPlayers.clear();

			String[] players = readyPlayers.split(",");
			for (String p: players)
			{
				for (ConnectedPlayer c: ScreenPartyLobby.connections)
				{
					if (c.clientId.toString().equals(p))
						ScreenPartyLobby.readyPlayers.add(c);
				}
			}
		}
	}

	@Override
	public void write(ByteBuf b) 
	{
		NetworkUtils.writeString(b, this.readyPlayers);
	}

	@Override
	public void read(ByteBuf b) 
	{
		this.readyPlayers = NetworkUtils.readString(b);
	}

}
