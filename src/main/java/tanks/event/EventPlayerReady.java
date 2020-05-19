package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.gui.screen.ScreenGame;
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

		if (!ScreenPartyHost.readyPlayers.contains(this.clientID))
			ScreenPartyHost.readyPlayers.add(this.clientID);

		Game.eventsOut.add(new EventUpdateReadyCount(ScreenPartyHost.readyPlayers.size()));

		if (ScreenPartyHost.readyPlayers.size() >= ScreenPartyHost.includedPlayers.size() && Game.screen instanceof ScreenGame)
		{
			Game.eventsOut.add(new EventBeginLevelCountdown());
			((ScreenGame) Game.screen).cancelCountdown = false;
		}

	}

	@Override
	public void write(ByteBuf b)
	{
		
	}

	@Override
	public void read(ByteBuf b)
	{
		
	}

}
