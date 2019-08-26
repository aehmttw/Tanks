package tanks.event;

import tanks.Game;
import tanks.ScreenKicked;

public class EventKick implements INetworkEvent
{	
	public String reason;
	
	public EventKick(String reason)
	{
		this.reason = reason;
	}

	@Override
	public String getNetworkString() 
	{
		return this.reason;
	}

	@Override
	public void execute() 
	{
		Game.cleanUp();
		Game.screen = new ScreenKicked(reason);
	}
}
