package tanks;
import basewindow.BaseFile;
import tanks.event.*;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyHost;
import tanks.hotbar.ItemBar;
import tanks.hotbar.item.Item;
import tanks.tank.TankPlayer;
import tanks.tank.TankPlayerRemote;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Crusade 
{
	public static Crusade currentCrusade = null;
	public static boolean crusadeMode = false;

	public boolean replay = false;

	public boolean win = false;
	public boolean lose = false;

	public boolean lifeGained = false;

	public int currentLevel = 0;
	public int saveLevel = 0;

	public double timePassed = 0;

	public ArrayList<String> levels = new ArrayList<String>();
	public ArrayList<String> levelNames = new ArrayList<String>();

	public int bonusLifeFrequency = 3;
	public int startingLives = 3;
	public boolean showNames = false;

	public ArrayList<Item> crusadeItems = new ArrayList<Item>();

	public String name = "";
	public String fileName = "";

	public boolean internal = false;
	public boolean readOnly = false;

	public boolean started = false;

	public LinkedHashMap<Player, CrusadePlayer> crusadePlayers = new LinkedHashMap<>();

	public ArrayList<CrusadePlayer> disconnectedPlayers = new ArrayList<>();

	public String contents = "";
	public Exception error = null;

	public ArrayList<LevelPerformance> performances = new ArrayList<>();

	public Crusade(ArrayList<String> levelArray, String name, String file)
	{
		internal = true;
		this.fileName = file;
		this.initialize(levelArray, name);

		StringBuilder c = new StringBuilder();
		for (String s: levelArray)
			c.append(s).append("\n");

		contents = c.substring(0, c.length() - 1);
	}

	public Crusade(String s, String name)
	{
		this.contents = s;
		this.initialize(new ArrayList<>(Arrays.asList(s.split("\n"))), name);
	}
	
	public Crusade(BaseFile f, String name)
	{
		try 
		{
			this.fileName = f.path;
			f.startReading();
			ArrayList<String> list = new ArrayList<String>();

			StringBuilder c = new StringBuilder();

			while (f.hasNextLine())
			{
				String s = f.nextLine();
				
				if (!s.equals(""))
					list.add(s);

				c.append(s).append("\n");
			}

			this.contents = c.toString();
			
			this.initialize(list, name);
			
			f.stopReading();
		}
		catch (Exception e)
		{
			this.error = e;
		}
	}
	
	public void initialize(ArrayList<String> levelArray, String name)
	{
		int parsing = -1;
		
		int i = 0;
		
		while (i < levelArray.size())
		{
			String s = levelArray.get(i);
			switch (s.toLowerCase())
			{
				case "levels":
					parsing = 0;
					break;
				case "items":
					parsing = 1;
					break;
				case "properties":
					parsing = 2;
					break;
				default:
					if (parsing == 0)
					{
						this.levels.add(levelArray.get(i));

						if (levelArray.get(i).contains("name="))
							this.levelNames.add(levelArray.get(i).substring(levelArray.get(i).indexOf("name=") + 5));
						else
							this.levelNames.add("Battle " + (levelNames.size() + 1));
					}
					else if (parsing == 1)
					{
						this.crusadeItems.add(Item.parseItem(null, s));
					}
					else if (parsing == 2)
					{
						String[] z = s.split(",");

						this.startingLives = Integer.parseInt(z[0]);
						this.bonusLifeFrequency = Integer.parseInt(z[1]);

						if (z.length > 2)
							this.showNames = Boolean.parseBoolean(z[2]);
					}
					break;
			}

			i++;
		}
		
		this.name = name;

		//if (this.levels.size() <= 0)
		//	Game.exitToCrash(new RuntimeException("The crusade " + name + " has no levels!"));

		for (int j = 0; j < Game.players.size(); j++)
		{
			Game.players.get(j).remainingLives = this.startingLives;
		}
	}

	public void begin()
	{
		for (int i = 0; i < Game.players.size(); i++)
		{
			Game.players.get(i).hotbar.itemBar = new ItemBar(Game.players.get(i));
			Game.players.get(i).hotbar.coins = 0;
			Game.players.get(i).remainingLives = startingLives;
		}

		currentLevel = 0;
		saveLevel = 0;

		Game.eventsOut.add(new EventBeginCrusade());

		this.timePassed = 0;
		this.started = true;
		this.crusadePlayers.clear();

        this.crusadePlayers.put(Game.player, new CrusadePlayer(Game.player));

        this.loadLevel();
	}

	public void loadLevel()
	{
		Level l = new Level(this.levels.get(this.currentLevel));

		Game.player.hotbar.enabledItemBar = true;
		Game.player.hotbar.enabledCoins = true;

		for (Player player : Game.players)
		{
			if (crusadePlayers.get(player) == null)
			{
				boolean found = false;

				for (CrusadePlayer cp: disconnectedPlayers)
				{
					if (cp.player.clientID.equals(player.clientID))
					{
						player.remainingLives = cp.player.remainingLives;
						cp.player = player;
						crusadePlayers.put(player, cp);
						found = true;
						break;
					}
				}

				if (!found)
					crusadePlayers.put(player, new CrusadePlayer(player));
			}

			if (player.remainingLives > 0)
				l.includedPlayers.add(player);

			player.hotbar.enabledItemBar = true;
		}

		l.loadLevel();

		for (Player player : Game.players)
		{
			player.hotbar.coins = crusadePlayers.get(player).coins;

			ItemBar i = crusadePlayers.get(player).itemBar;

			if (i == null)
				player.hotbar.itemBar = new ItemBar(player);
			else
				player.hotbar.itemBar = i;

			if (player != Game.player)
			{
				Game.eventsOut.add(new EventUpdateCoins(player));

				for (int in = 0; in < player.hotbar.itemBar.slots.length; in++)
					Game.eventsOut.add(new EventSetItem(player, in, player.hotbar.itemBar.slots[in]));

				Game.eventsOut.add(new EventLoadItemBarSlot(player.clientID, player.hotbar.itemBar.selected));
			}
		}

		this.disconnectedPlayers.clear();

		String sub = "";

		if (Crusade.currentCrusade.showNames)
			sub = Crusade.currentCrusade.levelNames.get(Crusade.currentCrusade.currentLevel).replace("_", " ");

		Game.eventsOut.add(new EventLoadCrusadeHotbar("Battle " + (this.currentLevel + 1), sub));
	}
	
	public void levelFinished(boolean win)
	{
		this.recordPerformance(ScreenGame.lastTimePassed, win);

		this.lifeGained = false;

		if (!win)
		{
			this.lose = true;

			for (Player player : Game.players)
			{
				if (player.remainingLives > 0)
				{
					this.lose = false;
					break;
				}
			}
		}
		else
		{
			if (this.currentLevel >= levels.size() - 1)
			{
				this.win = true;
			}
			else if (!replay)
			{
				this.saveLevel++;

				if ((this.currentLevel + 1) % this.bonusLifeFrequency == 0)
				{
					this.lifeGained = true;

					for (Player player : Game.players)
					{
						player.remainingLives++;
					}
				}
			}
		}

		try
		{
			if (!ScreenPartyHost.isServer)
				this.crusadePlayers.get(Game.player).saveCrusade(win);
		}
		catch (Exception e)
		{
			Game.exitToCrash(e);
		}
	}

	public boolean finalLife()
	{
		for (Player p: Game.players)
		{
			if (p.remainingLives > 1)
				return false;
			else if (p.remainingLives == 1)
			{
				boolean found = false;
				for (Movable m: Game.movables)
				{
					if (m instanceof TankPlayer && ((TankPlayer) m).player == p && m.destroy)
						return false;
					else if (m instanceof TankPlayerRemote && ((TankPlayerRemote) m).player == p && m.destroy)
						return false;

					if ((m instanceof TankPlayer && ((TankPlayer) m).player == p) || (m instanceof TankPlayerRemote && ((TankPlayerRemote) m).player == p))
						found = true;
				}

				if (!found)
					return false;
			}
		}

		return true;
	}

	public ArrayList<Item> getShop() 
	{
		ArrayList<Item> shop = new ArrayList<Item>();
		
		for (int i = 0; i < this.crusadeItems.size(); i++)
		{
			Item item = this.crusadeItems.get(i);
			if (item.levelUnlock <= this.currentLevel)
				shop.add(item);
		}

		return shop;
	}

	public void saveHotbars()
	{
		for (Player p: Game.players)
		{
			CrusadePlayer cp = this.crusadePlayers.get(p);

			if (cp == null)
			{
				cp = new CrusadePlayer(p);
				this.crusadePlayers.put(p, cp);
			}

			cp.itemBar = p.hotbar.itemBar;
			cp.coins = p.hotbar.coins;
		}
	}

	public CrusadePlayer getCrusadePlayer(Player p)
	{
		CrusadePlayer cp = Crusade.currentCrusade.crusadePlayers.get(p);

		if (cp == null)
		{
			for (CrusadePlayer dp: Crusade.currentCrusade.disconnectedPlayers)
			{
				if (dp.player == p)
					cp = dp;
			}
		}

		return cp;
	}

	public static class LevelPerformance
	{
		public int index;
		public int attempts = 0;
		public double bestTime = Double.MAX_VALUE;
		public double totalTime = 0;

		public LevelPerformance(int index)
		{
			this.index = index;
		}

		public void recordAttempt(double time, boolean win)
		{
			if (win)
				this.bestTime = Math.min(this.bestTime, time);

			this.totalTime += time;
			this.attempts++;
		}

		@Override
		public String toString()
		{
			return index + "/" + attempts + "/" + bestTime + "/" + totalTime;
		}
	}

	public void recordPerformance(double time, boolean win)
	{
		for (int i = performances.size(); i < currentLevel; i++)
			performances.add(new LevelPerformance(i));

		if (performances.size() <= currentLevel)
			performances.add(new LevelPerformance(currentLevel));

		performances.get(currentLevel).recordAttempt(time, win);
	}
}
