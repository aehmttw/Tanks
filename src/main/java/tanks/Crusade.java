package tanks;

import basewindow.BaseFile;
import tanks.event.EventBeginCrusade;
import tanks.event.EventLoadCrusadeHotbar;
import tanks.hotbar.Coins;
import tanks.hotbar.Item;
import tanks.hotbar.ItemBar;
import tanks.tank.TankPlayer;
import tanks.tank.TankPlayerRemote;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class Crusade 
{
	public static Crusade currentCrusade = null;
	public static boolean crusadeMode = false;

	public boolean replay = false;

	public boolean win = false;
	public boolean lose = false;

	public boolean lifeGained = false;

	public int currentLevel = 0;

	public ArrayList<String> levels = new ArrayList<String>();
	public int bonusLifeFrequency = 3;
	public int startingLives = 3;

	public ArrayList<Item> crusadeItems = new ArrayList<Item>();

	public String name = "";

	public Crusade(ArrayList<String> levelArray, String name)
	{		
		this.initialize(levelArray, name);
	}
	
	public Crusade(BaseFile f, String name)
	{
		try 
		{
			f.startReading();
			ArrayList<String> list = new ArrayList<String>();
			
			while (f.hasNextLine())
			{
				String s = f.nextLine();
				
				if (!s.equals(""))
					list.add(s);
			}
			
			this.initialize(list, name);
			
			f.stopReading();
		}
		catch (FileNotFoundException e) 
		{
			Game.exitToCrash(e);
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
					}
					else if (parsing == 1)
					{
						this.crusadeItems.add(Item.parseItem(null, s));
					}
					else if (parsing == 2)
					{
						this.startingLives = Integer.parseInt(s.split(",")[0]);
						this.bonusLifeFrequency = Integer.parseInt(s.split(",")[1]);
					}
					break;
			}
			
			i++;
		}
		
		this.name = name;

		if (this.levels.size() <= 0)
			Game.exitToCrash(new RuntimeException("The crusade " + name + " has no levels!"));

		for (int j = 0; j < Game.players.size(); j++)
		{
			Game.players.get(j).remainingLives = this.startingLives;
		}
	}

	public void begin()
	{
		for (int i = 0; i < Game.players.size(); i++)
		{
			Game.players.get(i).crusadeItemBar = new ItemBar(Game.players.get(i));
			Game.players.get(i).coins = new Coins();
		}

		Game.player.crusadeItemBar.hotbar = Panel.panel.hotbar;

		Game.eventsOut.add(new EventBeginCrusade());

		this.loadLevel();
	}

	public void loadLevel()
	{
		Level l = new Level(this.levels.get(this.currentLevel));

		Panel.panel.hotbar.enabledItemBar = true;
		Panel.panel.hotbar.currentItemBar = Game.player.crusadeItemBar;
		Panel.panel.hotbar.enabledCoins = true;
		Panel.panel.hotbar.currentCoins = Game.player.coins;

		for (Player player: Game.players)
		{
			if (player.remainingLives > 0)
				l.includedPlayers.add(player);
		}

		l.loadLevel();
		Game.eventsOut.add(new EventLoadCrusadeHotbar("Battle " + (this.currentLevel + 1)));
	}
	
	public void levelFinished(boolean win)
	{
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
			else if ((this.currentLevel + 1) % this.bonusLifeFrequency == 0 && !replay)
			{
				this.lifeGained = true;

				for (Player player : Game.players)
				{
					player.remainingLives++;
				}
			}
		}
	}

	public boolean finalLife()
	{
		for (int i = 0; i < Game.movables.size(); i++)
		{
			if (Game.movables.get(i) instanceof TankPlayer && !Game.movables.get(i).destroy && ((TankPlayer) Game.movables.get(i)).player.remainingLives > 1)
				return false;
			else if (Game.movables.get(i) instanceof TankPlayerRemote && !Game.movables.get(i).destroy && ((TankPlayerRemote) Game.movables.get(i)).player.remainingLives > 1)
				return false;
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
}
