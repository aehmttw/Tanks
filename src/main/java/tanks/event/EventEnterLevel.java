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
		{
			ScreenGame s = new ScreenGame();
			Game.screen = s;

			if (Game.autoReady)
				Game.eventsOut.add(new EventPlayerAutoReady());
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
