package tanks.network.event;

import tanks.Game;
import tanks.gui.screen.ScreenPartyLobby;

public class EventConnectionSuccess extends PersonalEvent
{	
	public EventConnectionSuccess()
	{
		
	}

	@Override
	public void execute() 
	{
		if (this.clientID == null)
		{
			Game.screen = new ScreenPartyLobby();

			Game.eventsOut.add(new EventSendTankColors(Game.player));
		}
	}
}
