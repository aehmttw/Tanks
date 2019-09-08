package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.gui.screen.ScreenPartyLobby;

public class EventReturnToLobby implements INetworkEvent
{		
	public EventReturnToLobby()
	{

	}

	@Override
	public void execute() 
	{
		Game.cleanUp();
		Game.screen = new ScreenPartyLobby();
		ScreenPartyLobby.readyPlayers = 0;
			
		System.gc();
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
