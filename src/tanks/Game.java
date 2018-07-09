package tanks;

import java.awt.Color;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;

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
	
	public enum Menu {none, paused, title, options, interlevel, crashed}
	
	public static String crashMessage = "Yay! The game hasn't crashed yet!";
	
	public static Menu menu = Menu.title;
	
	//public static boolean mainMenu = false;
	//public static boolean optionsMenu = true;
	//public static boolean pausedMenu = false;
	public static boolean paused = true;

	public static boolean graphicalEffects = true;

	public static boolean insanity = false;

	public static int coins = 0;
	public static Item[] items = new Item[5];
	
	public static Registry registry = new Registry();
	
	static Screen gamescreen;
	
	static String currentLevel = "";	
	
	public static PrintStream logger = System.err;
	
	public static final String directoryPath = "/.tanks.d";
	public static final String logPath = directoryPath + "/logfile.txt";
	public static final String registryPath = directoryPath + "/tank-registry.txt";
	public static String homedir;
	
	public static ArrayList<Registry.DefaultTankEntry> defaultTanks = new ArrayList<Registry.DefaultTankEntry>();
	
	public static void initScript() 
	{
		defaultTanks.add(new Registry.DefaultTankEntry(EnemyTankBrown.class, "brown", 1));
		defaultTanks.add(new Registry.DefaultTankEntry(EnemyTankGray.class, "gray", 1));
		defaultTanks.add(new Registry.DefaultTankEntry(EnemyTankMint.class, "mint", 1.0 / 2));
		defaultTanks.add(new Registry.DefaultTankEntry(EnemyTankYellow.class, "yellow", 1.0 / 2));
		defaultTanks.add(new Registry.DefaultTankEntry(EnemyTankMagenta.class, "magenta", 1.0 / 3));
		defaultTanks.add(new Registry.DefaultTankEntry(EnemyTankRed.class, "red", 1.0 / 3));
		defaultTanks.add(new Registry.DefaultTankEntry(EnemyTankGreen.class, "green", 1.0 / 4));
		defaultTanks.add(new Registry.DefaultTankEntry(EnemyTankPurple.class, "purple", 1.0 / 4));
		defaultTanks.add(new Registry.DefaultTankEntry(EnemyTankWhite.class, "white", 1.0 / 4));
		defaultTanks.add(new Registry.DefaultTankEntry(EnemyTankOrange.class, "orange", 1.0 / 6));
		defaultTanks.add(new Registry.DefaultTankEntry(EnemyTankDarkGreen.class, "darkgreen", 1.0 / 9));
		defaultTanks.add(new Registry.DefaultTankEntry(EnemyTankBlack.class, "black", 1.0 / 10));
		defaultTanks.add(new Registry.DefaultTankEntry(EnemyTankPink.class, "pink", 1.0 / 15));
		
		homedir = System.getProperty("user.home");
		if (!Files.exists(Paths.get(homedir + directoryPath))) 
		{
			new File(homedir + directoryPath).mkdir();
			try 
			{
				new File(homedir + logPath).createNewFile();
				Game.logger = new PrintStream(new FileOutputStream (homedir + logPath, true));
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		if (!Files.exists(Paths.get(homedir + registryPath)))
		{
			Registry.initRegistry(homedir);
		}
		
		try 
		{
			Game.logger = new PrintStream(new FileOutputStream (homedir + logPath, true));
		} 
		catch (FileNotFoundException e) 
		{
			Game.logger = System.err;
			Game.logger.println(new Date().toString() + " (syswarn) logfile not found despite existence of tanks directory! using stderr instead.");
		}
		
		Registry.loadRegistry(homedir);
	}
	
	public static void main(String[] args)
	{		
		initScript();

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
	
	public static void exitToCrash()
	{
		Game.paused = true;
		obstacles.clear();
		belowEffects.clear();
		movables.clear();
		effects.clear();
		System.gc();
		menu = Menu.crashed;
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
