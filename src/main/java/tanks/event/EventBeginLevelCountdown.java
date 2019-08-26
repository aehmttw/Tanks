package tanks.event;

import tanks.Game;
import tanks.gui.screen.ScreenGame;

public class EventBeginLevelCountdown implements INetworkEvent
{	
	public EventBeginLevelCountdown()
	{
		
	}
	
	public EventBeginLevelCountdown(String l)
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
		if (Game.screen instanceof ScreenGame)
		{
			((ScreenGame)Game.screen).cancelCountdown = false;
		}
	}
}
