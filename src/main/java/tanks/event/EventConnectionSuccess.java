package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.gui.screen.ScreenPartyLobby;

public class EventConnectionSuccess extends PersonalEvent
{	
	public EventConnectionSuccess()
	{
		
	}

	@Override
	public void execute() 
	{
		if (this.clientID == null)
			Game.screen = new ScreenPartyLobby();
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
