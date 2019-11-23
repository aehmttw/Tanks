package tanks;

import tanks.hotbar.Coins;
import tanks.hotbar.Item;
import tanks.hotbar.ItemBar;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

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
	public int remainingLives = 3;
	public int bonusLifeFrequency = 3;
	
	public ArrayList<Item> crusadeItems = new ArrayList<Item>();
	
	public ItemBar itemBar = new ItemBar(Panel.panel.hotbar);
	public Coins coins = new Coins();
	
	public String name = "";

	public Crusade(ArrayList<String> levelArray, String name)
	{		
		this.initialize(levelArray, name);
	}
	
	public Crusade(File f, String name)
	{
		try 
		{
			Scanner in = new Scanner(f);
			ArrayList<String> list = new ArrayList<String>();
			
			while (in.hasNextLine())
			{
				String s = in.nextLine();
				
				if (!s.equals(""))
					list.add(s);
			}
			
			this.initialize(list, name);
			
			in.close();		
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
						this.crusadeItems.add(Item.parseItem(s));
					}
					else if (parsing == 2)
					{
						this.remainingLives = Integer.parseInt(s.split(",")[0]);
						this.bonusLifeFrequency = Integer.parseInt(s.split(",")[1]);
					}
					break;
			}
			
			i++;
		}
		
		this.name = name;

		if (this.levels.size() <= 0)
			Game.exitToCrash(new RuntimeException("The crusade " + name + " has no levels!"));
	}
	
	public void levelFinished(boolean win)
	{
		this.lifeGained = false;

		if (!win)
		{
			remainingLives--;
			
			if (remainingLives <= 0)
				this.lose = true;
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
				remainingLives++;
			}
		}
	}
	
	public void loadLevel()
	{
		Panel.panel.hotbar.enabledItemBar = true;
		Panel.panel.hotbar.currentItemBar = this.itemBar;
		Panel.panel.hotbar.enabledCoins = true;
		Panel.panel.hotbar.currentCoins = coins;
		Level level = new Level(this.levels.get(currentLevel));
		level.loadLevel();
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
