package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Panel;
import tanks.gui.screen.ScreenPartyInterlevel;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.network.NetworkUtils;

public class EventLevelEnd extends PersonalEvent
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
