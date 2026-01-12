package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.network.ConnectedPlayer;
import tanks.network.NetworkUtils;

import java.util.ArrayList;

public class EventUpdateEliminatedPlayers extends PersonalEvent
{
	public String eliminatedPlayers;

	public EventUpdateEliminatedPlayers()
	{

	}

	public EventUpdateEliminatedPlayers(ArrayList<ConnectedPlayer> players)
	{
		StringBuilder s = new StringBuilder();
		for (ConnectedPlayer p: players)
			s.append(p.clientId).append(",");

		if (players.size() == 0)
			eliminatedPlayers = "";
		else
			eliminatedPlayers = s.substring(0, s.length() - 1);
	}

	@Override
	public void execute()
	{
		if (this.clientID == null && Game.screen instanceof ScreenGame)
		{
			((ScreenGame) Game.screen).eliminatedPlayers.clear();

			String[] players = eliminatedPlayers.split(",");
			for (String p: players)
			{
				for (ConnectedPlayer c: ScreenPartyLobby.connections)
				{
					if (c.clientId.toString().equals(p))
						((ScreenGame) Game.screen).eliminatedPlayers.add(c);
				}
			}
		}
	}

	@Override
	public void write(ByteBuf b) 
	{
		NetworkUtils.writeString(b, this.eliminatedPlayers);
	}

	@Override
	public void read(ByteBuf b) 
	{
		this.eliminatedPlayers = NetworkUtils.readString(b);
	}

}
