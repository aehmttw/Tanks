package tanks.network.event;

import tanks.*;
import tanks.gui.screen.*;

public class EventLevelExit extends PersonalEvent
{	
	public String winningTeam;

	public EventLevelExit()
	{
		
	}
	
	public EventLevelExit(String winner)
	{
		this.winningTeam = winner;
	}

	@Override
	public void execute() 
	{
		if (this.clientID != null)
			return;

		if (Game.clientID.toString().equals(winningTeam) || (Game.playerTank != null && Game.playerTank.team != null && Game.playerTank.team.name.equals(this.winningTeam)))
		{
			Panel.win = true;
			Panel.winlose = "Victory!";
		}
		else
		{
			Panel.win = false;
			Panel.winlose = "You were destroyed!";
		}

		Game.silentCleanUp();
		Game.screen = new ScreenPartyInterlevel();

		ScreenPartyLobby.readyPlayers.clear();
		ScreenPartyLobby.includedPlayers.clear();

		System.gc();
	}
}
