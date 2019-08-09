package tanks.event;

import tanks.Game;
import tanks.ScreenGame;

public class EventEnterLevel implements INetworkEvent
{	
	public EventEnterLevel()
	{
		
	}
	
	public EventEnterLevel(String l)
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
		Game.screen = new ScreenGame();
	}
}
