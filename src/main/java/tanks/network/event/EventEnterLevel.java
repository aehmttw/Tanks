package tanks.network.event;

import tanks.*;
import tanks.gui.screen.ScreenGame;

public class EventEnterLevel extends PersonalEvent
{	
	public EventEnterLevel()
	{
		
	}

	@Override
	public void execute()
	{
		if (this.clientID == null)
		{
			ScreenGame s = new ScreenGame();
			Game.screen = s;

			if (Game.autoReady)
				Game.eventsOut.add(new EventPlayerAutoReady());

			Crusade.currentCrusade = null;
		}
	}
}
