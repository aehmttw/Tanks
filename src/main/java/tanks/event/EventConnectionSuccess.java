package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.gui.screen.ScreenPartyLobby;

public class EventConnectionSuccess implements INetworkEvent
{	
	public EventConnectionSuccess()
	{
		
	}

	@Override
	public void execute() 
	{
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
