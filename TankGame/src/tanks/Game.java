package tanks;

import java.awt.Color;
import java.awt.Toolkit;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

public class Game 
{
	public static final int tank_size = 50;
	
	public static ArrayList<Movable> movables = new ArrayList<Movable>();
	public static ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();
	public static ArrayList<Movable> effects = new ArrayList<Movable>();
	public static ArrayList<Movable> belowEffects = new ArrayList<Movable>();

	public static ArrayList<Movable> removeMovables = new ArrayList<Movable>();
	public static ArrayList<Obstacle> removeObstacles = new ArrayList<Obstacle>();
	public static ArrayList<Movable> removeEffects = new ArrayList<Movable>();
	public static ArrayList<Movable> removeBelowEffects = new ArrayList<Movable>();

	static int currentSizeX = 28;
	static int currentSizeY = 18;
	static double bgResMultiplier = 1;	
	public static Color[][] tiles = new Color[28][18];
	
	public static double levelSize = 1;
	
	public static PlayerTank player;
	
	public static boolean bulletLocked = false;
	
	public enum Menu {none, paused, title, options, interlevel}
	
	public static Menu menu = Menu.title;
	
	//public static boolean mainMenu = false;
	//public static boolean optionsMenu = true;
	//public static boolean pausedMenu = false;
	public static boolean paused = true;

	public static boolean graphicalEffects = true;

	public static boolean insanity = false;

	public static int coins = 0;
	public static Item[] items = new Item[5];
	
	static Screen gamescreen;
	
	static String currentLevel = "";	
	
	public static void main(String[] args)
	{		
		SwingUtilities.invokeLater
		(
			new Runnable()
			{
				@Override
				public void run() 
				{
					for (int i = 0; i < currentSizeX; i++)
					{
						for (int j = 0; j < currentSizeY; j++)
						{
							Game.tiles[i][j] = new Color((int)(255 - Math.random() * 20), (int)(227 - Math.random() * 20), (int)(186 - Math.random() * 20));
						}
					}
					
					gamescreen = new Screen();
					gamescreen.setTitle("Tanks");
					gamescreen.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("icon64.png")));
					
					
					//movables.add(new EnemyTankStationary(120, 600, tank_size));
					//movables.add(new EnemyTankStationary(900, 700, tank_size));

					//add things to movables

					Panel screen = new Panel();

					gamescreen.add(screen);
					screen.startTimer();
				}
			}
		);
	}
	
	public static void reset()
	{
		obstacles.clear();
		belowEffects.clear();
		movables.clear();
		effects.clear();
		System.gc();
		start();
	}
	
	public static void exit()
	{
		Game.paused = true;
		menu = Menu.interlevel;
		obstacles.clear();
		belowEffects.clear();
		movables.clear();
		effects.clear();
		System.gc();
	}
	
	public static void exitToTitle()
	{
		Game.tiles = new Color[28][18];
		for (int i = 0; i < 28; i++)
		{
			for (int j = 0; j < 18; j++)
			{
				Game.tiles[i][j] = new Color((int)(255 - Math.random() * 20), (int)(227 - Math.random() * 20), (int)(186 - Math.random() * 20));
			}
		}
		Level.currentColor = new Color(235, 207, 166);

		Game.gamescreen.setScreenBounds(Game.tank_size * 28, Game.tank_size * 18);
		Game.paused = true;
		menu = Menu.title;
		obstacles.clear();
		belowEffects.clear();
		movables.clear();
		effects.clear();
		System.gc();
	}
	
	public static void start()
	{
		//Level level = new Level("{28,18|4...11-6,11-0...5,17...27-6,16-3...6,0...10-11,11-11...14,16...23-11,16-12...17|3-15-player,7-3-purple2-2,20-14-green,22-3-green-2,8-8.5-brown,19-8.5-mint-2,13.5-5-yellow-1}");
		
		//System.out.println(LevelGenerator.generateLevelString());
		Game.currentLevel = LevelGenerator.generateLevelString();
		Level level = new Level(currentLevel);
		//Level level = new Level("{28,18|3...6-3...4,3...4-5...6,10...19-13...14,18...19-4...12|22-14-player,14-10-brown}");
		//Level level = new Level("{28,18|0...27-9,0...27-7|2-8-player,26-8-purple2-2}");
		level.loadLevel();
	}

	
}
