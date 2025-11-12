package tanks.network.event;

import tanks.Game;
import tanks.gui.screen.*;
import tanks.network.Client;

public class EventKick extends PersonalEvent
{	
	public String reason;
	
	public EventKick()
	{
		
	}
	
	public EventKick(String reason)
	{
		this.reason = reason;
	}

	@Override
	public void execute() 
	{
		if (this.clientID == null)
		{
			Game.cleanUp();
			ScreenPartyLobby.isClient = false;

			Client.handler.close();

			if (!(Game.screen instanceof ScreenKicked))
				Game.screen = new ScreenKicked(reason);
		}
	}
}
