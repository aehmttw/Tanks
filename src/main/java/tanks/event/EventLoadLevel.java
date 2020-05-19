package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Level;
import tanks.gui.screen.ScreenFailedToLoadLevel;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.network.NetworkUtils;

public class EventLoadLevel extends PersonalEvent
{
	public String level;

	public EventLoadLevel()
	{
		
	}
	
	public EventLoadLevel(Level l)
	{
		this.level = l.levelString;
	}

	@Override
	public void execute() 
	{		
		if (this.clientID != null)
			return;

		if (Game.playerTank != null)
			Game.playerTank.team = null;
			
		try
		{
			ScreenPartyLobby.readyPlayers = 0;
			ScreenPartyLobby.includedPlayers.clear();
			Game.cleanUp();
			Game.currentLevel = new Level(level);
			Game.currentLevel.loadLevel(true);
		}
		catch (Exception e)
		{
			Game.screen = new ScreenFailedToLoadLevel("Level is remote!", level, e, new ScreenPartyLobby());
		}
	}

	@Override
	public void write(ByteBuf b)
	{
		NetworkUtils.writeString(b, this.level);
	}

	@Override
	public void read(ByteBuf b)
	{
		this.level = NetworkUtils.readString(b);
	}
}
