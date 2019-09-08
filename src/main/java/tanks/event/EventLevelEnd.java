package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Panel;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.network.NetworkUtils;

public class EventLevelEnd implements INetworkEvent
{	
	public String winningTeam;
	
	public EventLevelEnd()
	{
		
	}
	
	public EventLevelEnd(String winner)
	{
		this.winningTeam = winner;
	}

	@Override
	public void execute() 
	{
		Game.cleanUp();
		Game.screen = new ScreenPartyLobby();
		
		if (Game.clientID.toString().equals(winningTeam) || (Game.player.team != null && Game.player.team.name.equals(this.winningTeam)))
		{
			Panel.win = true;
			Panel.winlose = "Victory!";
		}
		else
		{
			Panel.win = false;
			Panel.winlose = "You were destroyed!";
		}
		
		ScreenPartyLobby.readyPlayers = 0;
			
		System.gc();
	}

	@Override
	public void write(ByteBuf b) 
	{
		NetworkUtils.writeString(b, this.winningTeam);
	}

	@Override
	public void read(ByteBuf b)
	{
		this.winningTeam = NetworkUtils.readString(b);
	}
}
