package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Crusade;
import tanks.Game;
import tanks.Level;
import tanks.Movable;
import tanks.gui.screen.ScreenFailedToLoadLevel;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.item.Item;
import tanks.minigames.Minigame;
import tanks.network.NetworkUtils;
import tanks.tank.TankAIControlled;
import tanks.tank.TankPlayer;
import tanks.tank.TankRemote;

import java.util.ArrayList;

public class EventLoadLevel extends PersonalEvent
{
	public String level;

	public double startTime;
	public boolean disableFriendlyFire;

	public EventLoadLevel()
	{
		
	}
	
	public EventLoadLevel(Level l)
	{
		this.level = l.levelString;

		if (Crusade.crusadeMode)
		{
			StringBuilder s = new StringBuilder("tanks\n");

			for (TankAIControlled t : l.customTanks)
			{
				s.append(t.toString()).append("\n");
			}

			s.append("shop\n");

			for (Item.ShopItem i: Crusade.currentCrusade.getShop())
			{
				s.append(i.toString()).append("\n");
			}

			s.append("builds\n");

			for (TankPlayer.ShopTankBuild i: Crusade.currentCrusade.getBuildsShop())
			{
				s.append(i.toString()).append("\n");
			}

			s.append("level\n");

			this.level = s + level;
		}

		this.startTime = l.startTime;
		this.disableFriendlyFire = l.disableFriendlyFire;

		if (l instanceof Minigame)
			this.level = "minigame=" + ((Minigame) l).name;
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
			ScreenPartyLobby.readyPlayers.clear();
			ScreenPartyLobby.includedPlayers.clear();
			Game.cleanUp();

			if (level.startsWith("minigame="))
				Game.currentLevel = Game.registryMinigame.minigames.get(level.substring(level.indexOf("=") + 1)).getConstructor().newInstance();
			else
				Game.currentLevel = new Level(level, new ArrayList<>(), true, disableFriendlyFire);

			Game.currentLevel.startTime = startTime;
			Game.currentLevel.loadLevel();
		}
		catch (Exception e)
		{
			Game.screen = new ScreenFailedToLoadLevel("Level is remote!", level, e, new ScreenPartyLobby());
		}
	}
}
