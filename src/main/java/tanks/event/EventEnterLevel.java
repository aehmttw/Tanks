package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.gui.screen.ScreenGame;

public class EventEnterLevel implements INetworkEvent
{	
	public EventEnterLevel()
	{
		
	}

	@Override
	public void execute() 
	{
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
