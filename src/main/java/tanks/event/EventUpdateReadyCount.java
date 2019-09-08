package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.gui.screen.ScreenPartyLobby;

public class EventUpdateReadyCount implements INetworkEvent
{
	public int readyPlayers;
	
	public EventUpdateReadyCount()
	{
		
	}
	
	public EventUpdateReadyCount(int players)
	{
		this.readyPlayers = players;
	}

	@Override
	public void execute() 
	{
		ScreenPartyLobby.readyPlayers = readyPlayers;
	}

	@Override
	public void write(ByteBuf b) 
	{
		b.writeInt(this.readyPlayers);
	}

	@Override
	public void read(ByteBuf b) 
	{
		this.readyPlayers = b.readInt();
	}

}
