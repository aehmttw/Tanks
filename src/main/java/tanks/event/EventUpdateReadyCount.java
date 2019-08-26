package tanks.event;

import tanks.gui.screen.ScreenPartyLobby;

public class EventUpdateReadyCount implements INetworkEvent
{
	public int readyPlayers;
	
	public EventUpdateReadyCount(int players)
	{
		this.readyPlayers = players;
	}
	
	public EventUpdateReadyCount(String l)
	{
		this.readyPlayers = Integer.parseInt(l);
	}
	
	@Override
	public String getNetworkString() 
	{
		return this.readyPlayers + "";
	}

	@Override
	public void execute() 
	{
		ScreenPartyLobby.readyPlayers = readyPlayers;
	}

}
