package tanks;

import lwjglwindow.LWJGLWindow;

import tanks.bullet.Bullet;
import tanks.network.*;
import tanks.event.*;
import tanks.tank.*;
import tanks.obstacle.*;
import tanks.registry.*;
import tanks.gui.*;
import tanks.gui.screen.*;
import tanks.hotbar.*;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.UUID;

public class Game 
{
	public static final double tank_size = 50;
	
	public static final UUID clientID = UUID.randomUUID();
	
	public static ArrayList<Movable> movables = new ArrayList<Movable>();
	public static ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();
	public static ArrayList<Effect> effects = new ArrayList<Effect>();
	public static ArrayList<Effect> belowEffects = new ArrayList<Effect>();

	public static ArrayList<Movable> removeMovables = new ArrayList<Movable>();
	public static ArrayList<Obstacle> removeObstacles = new ArrayList<Obstacle>();
	public static ArrayList<Effect> removeEffects = new ArrayList<Effect>();
	public static ArrayList<Effect> removeBelowEffects = new ArrayList<Effect>();

	public static ArrayList<Effect> recycleEffects = new ArrayList<Effect>();

	public static final SynchronizedList<INetworkEvent> eventsOut = new SynchronizedList<INetworkEvent>();
	public static final SynchronizedList<INetworkEvent> eventsIn = new SynchronizedList<INetworkEvent>();

	//public static Team playerTeam = new Team(new Color(0, 0, 255));
	//public static Team enemyTeam = new Team(new Color(255, 0, 0));
	
	public static Team playerTeam = new Team("ally");
	public static Team enemyTeam = new Team("enemy");

	public static int currentSizeX = 28;
	public static int currentSizeY = 18;
	public static double bgResMultiplier = 1;	
	
	public static double[][] tilesR = new double[28][18];
	public static double[][] tilesG = new double[28][18];
	public static double[][] tilesB = new double[28][18];
	
	public static Obstacle[][] tileDrawables = new Obstacle[28][18];
	
	public static double[][] tilesDepth = new double[28][18];

	public static final int network_protocol = 8;
	public static final String version = "Tanks 0.7.5";

	public static int port = 8080;

	public static String lastParty = "";

	public static double levelSize = 1;
	
	public static Tank player;
	
	public static boolean bulletLocked = false;
		
	public static boolean vsync = true;
	
	public static boolean enable3d = true;

	public static boolean enableChatFilter = true;
	
	public static String crashMessage = "Yay! The game hasn't crashed yet!";
	
	public static Screen screen;

	public static String username = "";
	
	public static String ip = "";
	
	public static boolean fancyGraphics = true;

	public static boolean autostart = true;
	public static double startTime = 400;

	public static boolean enableCustomTankRegistry = false;
	public static boolean enableCustomObstacleRegistry = false;

	public static RegistryTank registryTank = new RegistryTank();
	public static RegistryObstacle registryObstacle = new RegistryObstacle();

	public LWJGLWindow window;
	
	public static Level currentLevel = null;	
	public static String currentLevelString = "";	
	
	public static ChatFilter chatFilter = new ChatFilter();
	
	public static PrintStream logger = System.err;
	
	public static final String directoryPath = "/.tanks";
	public static final String logPath = directoryPath + "/logfile.txt";
	public static final String tankRegistryPath = directoryPath + "/tank-registry.txt";
	public static final String obstacleRegistryPath = directoryPath + "/obstacle-registry.txt";
	public static final String optionsPath = directoryPath + "/options.txt";
	public static final String tutorialPath = directoryPath + "/tutorial.txt";

	public static String homedir;
	
	public static ArrayList<RegistryTank.DefaultTankEntry> defaultTanks = new ArrayList<RegistryTank.DefaultTankEntry>();
	public static ArrayList<RegistryObstacle.DefaultObstacleEntry> defaultObstacles = new ArrayList<RegistryObstacle.DefaultObstacleEntry>();

	public static Game game = new Game();
	
	private Game() {}
	
	public static void initScript()
	{		
		Drawing.initialize();
		Panel.initialize();
		Game.exitToTitle();
		
		/* 0 */ NetworkEventMap.register(EventSendClientDetails.class);
		/* 1 */ NetworkEventMap.register(EventKeepConnectionAlive.class);
		/* 2 */ NetworkEventMap.register(EventConnectionSuccess.class);
		/* 3 */ NetworkEventMap.register(EventKick.class);
		/* 4 */ NetworkEventMap.register(EventAnnounceConnection.class);
		/* 5 */ NetworkEventMap.register(EventChat.class);
		/* 6 */ NetworkEventMap.register(EventPlayerChat.class);
		/* 7 */ NetworkEventMap.register(EventLoadLevel.class);
		/* 8 */ NetworkEventMap.register(EventEnterLevel.class);
		/* 9 */ NetworkEventMap.register(EventLevelEnd.class);
		/* 10*/ NetworkEventMap.register(EventReturnToLobby.class);
		/* 11*/ NetworkEventMap.register(EventPlayerReady.class);
		/* 12*/ NetworkEventMap.register(EventUpdateReadyCount.class);
		/* 13*/ NetworkEventMap.register(EventBeginLevelCountdown.class);
		/* 14*/ NetworkEventMap.register(EventTankUpdate.class);
		/* 15*/ NetworkEventMap.register(EventTankControllerUpdateS.class);
		/* 16*/ NetworkEventMap.register(EventTankControllerUpdateC.class);
		/* 17*/ NetworkEventMap.register(EventTankControllerUpdateAmmunition.class);
		/* 18*/ NetworkEventMap.register(EventCreatePlayer.class);
		/* 19*/ NetworkEventMap.register(EventCreateTank.class);
		/* 20*/ NetworkEventMap.register(EventCreateCustomTank.class);
		/* 21*/ NetworkEventMap.register(EventTankUpdateHealth.class);
		/* 22*/ NetworkEventMap.register(EventShootBullet.class);
		/* 23*/ NetworkEventMap.register(EventBulletUpdate.class);
		/* 24*/ NetworkEventMap.register(EventBulletDestroyed.class);
		/* 25*/ NetworkEventMap.register(EventBulletInstantWaypoint.class);
		/* 26*/ NetworkEventMap.register(EventBulletAddAttributeModifier.class);
		/* 27*/ NetworkEventMap.register(EventBulletElectricStunEffect.class);
		/* 28*/ NetworkEventMap.register(EventLayMine.class);
		/* 29*/ NetworkEventMap.register(EventMineExplode.class);
		/* 30*/ NetworkEventMap.register(EventTankTeleport.class);
		/* 31*/ NetworkEventMap.register(EventTankUpdateVisibility.class);
		/* 32*/ NetworkEventMap.register(EventTankRedUpdateCharge.class);
		/* 33*/ NetworkEventMap.register(EventTankAddAttributeModifier.class);

		defaultObstacles.add(new RegistryObstacle.DefaultObstacleEntry(Obstacle.class, "normal"));
		defaultObstacles.add(new RegistryObstacle.DefaultObstacleEntry(ObstacleIndestructible.class, "hard"));
		defaultObstacles.add(new RegistryObstacle.DefaultObstacleEntry(ObstacleHole.class, "hole"));
		defaultObstacles.add(new RegistryObstacle.DefaultObstacleEntry(ObstacleBouncy.class, "bouncy"));
		defaultObstacles.add(new RegistryObstacle.DefaultObstacleEntry(ObstacleShrubbery.class, "shrub"));
		defaultObstacles.add(new RegistryObstacle.DefaultObstacleEntry(ObstacleTeleporter.class, "teleporter"));

		defaultTanks.add(new RegistryTank.DefaultTankEntry(TankBrown.class, "brown", 1));
		defaultTanks.add(new RegistryTank.DefaultTankEntry(TankGray.class, "gray", 1));
		defaultTanks.add(new RegistryTank.DefaultTankEntry(TankMint.class, "mint", 1.0 / 2));
		defaultTanks.add(new RegistryTank.DefaultTankEntry(TankYellow.class, "yellow", 1.0 / 2));
		defaultTanks.add(new RegistryTank.DefaultTankEntry(TankMagenta.class, "magenta", 1.0 / 3));
		defaultTanks.add(new RegistryTank.DefaultTankEntry(TankRed.class, "red", 1.0 / 3));
		defaultTanks.add(new RegistryTank.DefaultTankEntry(TankGreen.class, "green", 1.0 / 4));
		defaultTanks.add(new RegistryTank.DefaultTankEntry(TankPurple.class, "purple", 1.0 / 4));
		defaultTanks.add(new RegistryTank.DefaultTankEntry(TankBlue.class, "blue", 1.0 / 4));
		defaultTanks.add(new RegistryTank.DefaultTankEntry(TankWhite.class, "white", 1.0 / 4));
		defaultTanks.add(new RegistryTank.DefaultTankEntry(TankCyan.class, "cyan", 1.0 / 5));
		defaultTanks.add(new RegistryTank.DefaultTankEntry(TankOrange.class, "orange", 1.0 / 6));
		defaultTanks.add(new RegistryTank.DefaultTankEntry(TankMaroon.class, "maroon", 1.0 / 7));
		defaultTanks.add(new RegistryTank.DefaultTankEntry(TankMedic.class, "medic", 1.0 / 8));
		defaultTanks.add(new RegistryTank.DefaultTankEntry(TankDarkGreen.class, "darkgreen", 1.0 / 9));
		defaultTanks.add(new RegistryTank.DefaultTankEntry(TankBlack.class, "black", 1.0 / 10));
		defaultTanks.add(new RegistryTank.DefaultTankEntry(TankPink.class, "pink", 1.0 / 15));
		defaultTanks.add(new RegistryTank.DefaultTankEntry(TankBoss.class, "boss", 1.0 / 25));
		
		homedir = System.getProperty("user.home");
		
		if (Files.exists(Paths.get(homedir + "/.tanks.d")) && !Files.exists(Paths.get(homedir + directoryPath)))
		{
			try
			{
				Files.move(Paths.get(homedir + "/.tanks.d"), Paths.get(homedir + directoryPath));
			}
			catch (Exception e)
			{
				e.printStackTrace();
				System.exit(1);
			}
			
		}
		
		if (!Files.exists(Paths.get(directoryPath))) 
		{
			new File(homedir + directoryPath).mkdir();
			new File(homedir + directoryPath + ScreenSavedLevels.levelDir).mkdir();

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
		
		if (!Files.exists(Paths.get(homedir + tutorialPath))) 
		{
			Game.silentCleanUp();
			Tutorial.loadTutorial(true);
		}
		
		if (!Files.exists(Paths.get(homedir + tankRegistryPath)))
		{
			RegistryTank.initRegistry(homedir);
		}
		
		if (!Files.exists(Paths.get(homedir + obstacleRegistryPath)))
		{
			RegistryObstacle.initRegistry(homedir);
		}
		
		if (!Files.exists(Paths.get(homedir + optionsPath)))
		{
			ScreenOptions.initOptions(homedir);
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
		
		ScreenOptions.loadOptions(homedir);
		RegistryTank.loadRegistry(homedir);
		RegistryObstacle.loadRegistry(homedir);		
		
		if (Game.usernameInvalid(Game.username))
			Game.screen = new ScreenUsernameInvalid();
	}
	
	public static boolean usernameInvalid(String username)
	{
		if (username.length() > 18)
			return true;
			
		for (int i = 0; i < Game.username.length(); i++)
		{
			if (!"abcdefghijklmnopqrstuvwxyz1234567890_".contains(Game.username.toLowerCase().substring(i, i+1)))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public static void main(String[] args)
	{		
		Game.initScript();
		Game.game.window = new LWJGLWindow("Tanks", 1400, 900 + Drawing.drawing.statsHeight, 1000, new GameUpdater(), new GameDrawer(), new GameWindowHandler(), Game.vsync, !Panel.showMouseTarget);
		Game.game.window.run();
	}
	
	public static void reset()
	{
		Tank.currentID = 0;
		Tank.freeIDs.clear();
		Tank.idMap.clear();
		obstacles.clear();
		belowEffects.clear();
		movables.clear();
		effects.clear();
		recycleEffects.clear();
		removeEffects.clear();
		removeBelowEffects.clear();

		System.gc();
		start();
	}
	
	public static void exit()
	{
		screen = new ScreenInterlevel();
		Tank.currentID = 0;
		Tank.freeIDs.clear();
		Tank.idMap.clear();
		obstacles.clear();
		belowEffects.clear();
		movables.clear();
		effects.clear();
		recycleEffects.clear();
		removeEffects.clear();
		removeBelowEffects.clear();
		
		System.gc();
	}
	
	public static void exit(String name)
	{
		obstacles.clear();
		belowEffects.clear();
		movables.clear();
		effects.clear();
		recycleEffects.clear();
		removeEffects.clear();
		removeBelowEffects.clear();
		
		System.gc();
		
		ScreenLevelBuilder s = new ScreenLevelBuilder(name);
		Game.loadLevel(new File(Game.homedir + ScreenSavedLevels.levelDir + "/" + name), s);
		Game.screen = s;	
	}
	
	public static void exitToCrash(Exception e)
	{
		e.printStackTrace();

		if (ScreenPartyHost.isServer && ScreenPartyHost.server != null)
			ScreenPartyHost.server.close("The party has ended because the host crashed");
		
		if (ScreenPartyLobby.isClient)
			Client.handler.ctx.close();
		
		ScreenPartyHost.isServer = false;
		ScreenPartyLobby.isClient = false;

		obstacles.clear();
		belowEffects.clear();
		movables.clear();
		effects.clear();
		Game.crashMessage = e.toString();
		Game.logger.println(new Date().toString() + " (syserr) the game has crashed! below is a crash report, good luck:");
		e.printStackTrace(Game.logger);
		screen = new ScreenCrashed();
		recycleEffects.clear();
		removeEffects.clear();
		removeBelowEffects.clear();
		
		System.gc();
	}
	
	public static void resetTiles()
	{
		Game.tilesR = new double[28][18];
		Game.tilesG = new double[28][18];
		Game.tilesB = new double[28][18];
		Game.tilesDepth = new double[28][18];
		Game.tileDrawables = new Obstacle[28][18];

		for (int i = 0; i < 28; i++)
		{
			for (int j = 0; j < 18; j++)
			{
				Game.tilesR[i][j] = (255 - Math.random() * 20);
				Game.tilesG[i][j] = (227 - Math.random() * 20);
				Game.tilesB[i][j] = (186 - Math.random() * 20);
				Game.tilesDepth[i][j] = Math.random() * 10;
			}
		}
		
		Level.currentColorR = 235; 
		Level.currentColorG = 207;
		Level.currentColorB = 166;
	}
	
	public static void exitToTitle()
	{
		cleanUp();
		screen = new ScreenTitle();
		System.gc();
	}
	
	public static void cleanUp()
	{
		resetTiles();

		silentCleanUp();
	}

	public static void silentCleanUp()
    {
        Drawing.drawing.setScreenBounds(Game.tank_size * 28, Game.tank_size * 18);
        obstacles.clear();
        belowEffects.clear();
        movables.clear();
        effects.clear();
        recycleEffects.clear();
        removeEffects.clear();
        removeBelowEffects.clear();

        Tank.currentID = 0;
        Tank.idMap.clear();
        Tank.freeIDs.clear();

		Bullet.currentID = 0;
		Bullet.idMap.clear();
		Bullet.freeIDs.clear();

        Panel.panel.hotbar.currentCoins = new Coins();
        Panel.panel.hotbar.enabledCoins = false;
        Panel.panel.hotbar.currentItemBar = new ItemBar(Panel.panel.hotbar);
        Panel.panel.hotbar.enabledItemBar = false;
    }
	
	public static void loadLevel(File f)
	{
		Game.loadLevel(f, null);
	}
	
	public static void loadLevel(File f, ScreenLevelBuilder s)
	{
		Scanner in = null;
		try
		{
			in = new Scanner(f);
			
			while (in.hasNextLine()) 
			{
				String line = in.nextLine();
				Level l = new Level(line);
				l.loadLevel(s);
			}
		}
		catch (FileNotFoundException e)
		{
			Game.exitToCrash(e);
		}

		if (in != null)
			in.close();
	}
	
	public static void start()
	{		
		//Level level = new Level("{28,18|4...11-6,11-0...5,17...27-6,16-3...6,0...10-11,11-11...14,16...23-11,16-12...17|3-15-player,7-3-purple2-2,20-14-green,22-3-green-2,8-8.5-brown,19-8.5-mint-2,13.5-5-yellow-1}");
		
		//System.out.println(LevelGenerator.generateLevelString());		
		//Game.currentLevel = "{28,18|0-17,1-16,2-15,3-14,4-13,5-12,6-11,7-10,10-7,12-5,15-2,16-1,17-0,27-0,26-1,25-2,24-3,23-4,22-5,21-6,20-7,17-10,15-12,12-15,11-16,10-17,27-17,26-16,25-15,24-14,23-13,22-12,21-11,20-10,17-7,15-5,12-2,11-1,10-0,0-0,1-1,3-3,2-2,4-4,5-5,6-6,7-7,10-10,12-12,15-15,16-16,17-17,11-11,16-11,16-6,11-6|0-8-player-0,13-8-magenta-1,14-9-magenta-3,12-10-yellow-0,15-7-yellow-2,13-0-mint-1,14-17-mint-3,27-8-mint-2,27-9-mint-2}";///LevelGenerator.generateLevelString();
		Level level = new Level(LevelGenerator.generateLevelString());
		//Level level = new Level("{28,18|3...6-3...4,3...4-5...6,10...19-13...14,18...19-4...12|22-14-player,14-10-brown}");
		//Level level = new Level("{28,18|0...27-9,0...27-7|2-8-player,26-8-purple2-2}");
		level.loadLevel();
	}

	
}
