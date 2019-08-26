package tanks.event;

import tanks.Game;
import tanks.gui.Panel;
import tanks.gui.screen.ScreenPartyLobby;

public class EventLevelEnd implements INetworkEvent
{	
	public String winningTeam;
	
	public EventLevelEnd(String winner)
	{
		this.winningTeam = winner;
	}
	
	@Override
	public String getNetworkString() 
	{
		return winningTeam;
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
}
