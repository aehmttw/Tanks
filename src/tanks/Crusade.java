package tanks;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Crusade 
{
	public static Crusade currentCrusade = null;
	public static boolean crusadeMode = false;
	
	public boolean win = false;
	public boolean lose = false;

	public int currentLevel = 0;
	public ArrayList<String> levels = new ArrayList<String>();
	public int remainingLives = 3;
	public int bonusLifeFrequency = 3;
	
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
		String[] args = levelArray.get(0).split("-");
		
		this.remainingLives = Integer.parseInt(args[0].split(",")[0]);
		this.bonusLifeFrequency = Integer.parseInt(args[0].split(",")[1]);
		
		for (int i = 1; i < levelArray.size(); i++)
		{
			this.levels.add(levelArray.get(i));
		}
		
		this.name = name;
	}
	
	public void levelFinished(boolean win)
	{
		if (!win)
		{
			remainingLives--;
			
			if (remainingLives <= 0)
				this.lose = true;
		}
		else
		{
			currentLevel++;
			
			if (this.currentLevel % 3 == 0)
				remainingLives++;
			
			if (this.currentLevel >= levels.size())
			{
				this.win = true;
			}
		}
	}
	
	public void loadLevel()
	{
		Level level = new Level(this.levels.get(currentLevel));
		level.loadLevel();
	}
}
