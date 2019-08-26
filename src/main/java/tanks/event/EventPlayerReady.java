package tanks.event;

import tanks.Game;
import tanks.ScreenGame;
import tanks.ScreenPartyHost;

public class EventPlayerReady extends PersonalEvent
{
	public EventPlayerReady()
	{

	}

	public EventPlayerReady(String l)
	{

	}

	@Override
	public String getNetworkString() 
	{
		return "x";
	}

	@Override
	public void execute() 
	{
		if (!ScreenPartyHost.readyPlayers.contains(this.clientID))
			ScreenPartyHost.readyPlayers.add(this.clientID);

		Game.events.add(new EventUpdateReadyCount(ScreenPartyHost.readyPlayers.size()));

		//synchronized(ScreenPartyHost.server.connections)
		{
			if (ScreenPartyHost.readyPlayers.size() >= ScreenPartyHost.server.connections.size() + 1 && Game.screen instanceof ScreenGame)
			{
				Game.events.add(new EventBeginLevelCountdown());
				((ScreenGame)Game.screen).cancelCountdown = false;
			}
		}
	}

}
