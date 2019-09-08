package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.gui.screen.ScreenGame;

public class EventBeginLevelCountdown implements INetworkEvent
{	
	public EventBeginLevelCountdown()
	{
		
	}

	@Override
	public void execute() 
	{
		if (Game.screen instanceof ScreenGame)
		{
			((ScreenGame)Game.screen).cancelCountdown = false;
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
