package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.gui.screen.ScreenPartyLobby;

public class EventReturnToLobby extends PersonalEvent
{		
	public EventReturnToLobby()
	{

	}

	@Override
	public void execute() 
	{
		if (this.clientID == null)
		{
			Game.cleanUp();
			Game.screen = new ScreenPartyLobby();
			ScreenPartyLobby.readyPlayers.clear();
			ScreenPartyLobby.includedPlayers.clear();

			System.gc();
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
