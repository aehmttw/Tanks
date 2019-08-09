package tanks.event;

import tanks.Game;
import tanks.Level;
import tanks.ScreenFailedToLoadLevel;
import tanks.ScreenPartyLobby;

public class EventLoadLevel extends PersonalEvent
{
	public Level level;

	public EventLoadLevel(Level l)
	{
		this.level = l;
	}

	public EventLoadLevel(String l)
	{
		this.level = new Level(l);
	}

	@Override
	public String getNetworkString() 
	{
		return this.level.levelString;
	}

	@Override
	public void execute() 
	{
		if (this.clientID != null)
			return;
			
		try
		{
			ScreenPartyLobby.readyPlayers = 0;
			Game.exit();
			Game.currentLevel = level;
			Game.currentLevel.loadLevel(true);
		}
		catch (Exception e)
		{
			Game.screen = new ScreenFailedToLoadLevel("Level is remote!", new ScreenPartyLobby());
		}
	}
}
