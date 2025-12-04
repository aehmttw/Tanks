package tanks.network.event;

import tanks.*;
import tanks.gui.screen.ScreenPartyHost;

public class EventPlayerReady extends PersonalEvent
{
	public EventPlayerReady()
	{

	}

	@Override
	public void execute() 
	{
		if (!ScreenPartyHost.includedPlayers.contains(this.clientID))
			return;

		Player pl = null;

		for (Player p: Game.players)
		{
			if (p.clientID.equals(this.clientID))
			{
				pl = p;
			}
		}

		if (pl != null)
		{
			if (!ScreenPartyHost.readyPlayers.contains(pl))
				ScreenPartyHost.readyPlayers.add(pl);

			Game.eventsOut.add(new EventUpdateReadyPlayers(ScreenPartyHost.readyPlayers));
		}
	}
}
