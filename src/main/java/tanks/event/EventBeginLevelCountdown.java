package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.gui.screen.ScreenGame;

public class EventBeginLevelCountdown extends PersonalEvent
{	
	public EventBeginLevelCountdown()
	{
		
	}

	@Override
	public void execute() 
	{
		if (Game.screen instanceof ScreenGame && this.clientID == null)
		{
			((ScreenGame) Game.screen).shopScreen = false;
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
