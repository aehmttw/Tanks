package tanks.event;

import tanks.Game;
import tanks.gui.screen.ScreenPartyLobby;

public class EventConnectionSuccess implements INetworkEvent
{	
	public EventConnectionSuccess()
	{
		
	}
	
	public EventConnectionSuccess(String l)
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
		Game.screen = new ScreenPartyLobby();
	}
}
