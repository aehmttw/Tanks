package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
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
			Game.screen = new ScreenGame();
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
