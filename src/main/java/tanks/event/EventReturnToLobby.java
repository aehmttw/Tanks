package tanks.event;

import tanks.Game;
import tanks.gui.screen.ScreenPartyLobby;

public class EventReturnToLobby implements INetworkEvent
{		
	public EventReturnToLobby()
	{

	}
	
	public EventReturnToLobby(String s)
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
		Game.cleanUp();
		Game.screen = new ScreenPartyLobby();
		ScreenPartyLobby.readyPlayers = 0;
			
		System.gc();
	}
}
