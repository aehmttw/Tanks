package tanks;

import basewindow.BaseFile;
import basewindow.BaseFileManager;
import basewindow.BaseWindow;
import tanks.bullet.Bullet;
import tanks.event.*;
import tanks.event.online.*;
import tanks.gui.ChatFilter;
import tanks.gui.screen.*;
import tanks.hotbar.Coins;
import tanks.hotbar.ItemBar;
import tanks.network.Client;
import tanks.network.NetworkEventMap;
import tanks.network.SynchronizedList;
import tanks.obstacle.*;
import tanks.registry.RegistryObstacle;
import tanks.registry.RegistryTank;
import tanks.tank.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

public class Game 
{
	public enum Framework {lwjgl, swing, libgdx}
	public static Framework framework;

	public static final double tank_size = 50;

	public static UUID computerID;
	public static final UUID clientID = UUID.randomUUID();

	public static final int absoluteDepthBase = 1000;
	
	public static ArrayList<Movable> movables = new ArrayList<Movable>();
	public static ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();
	public static ArrayList<Effect> effects = new ArrayList<Effect>();
	public static ArrayList<Effect> belowEffects = new ArrayList<Effect>();
	public static SynchronizedList<Player> players = new SynchronizedList<Player>();
	public static Player player;

	public static ArrayList<Movable> removeMovables = new ArrayList<Movable>();
	public static ArrayList<Obstacle> removeObstacles = new ArrayList<Obstacle>();
	public static ArrayList<Effect> removeEffects = new ArrayList<Effect>();
	public static ArrayList<Effect> removeBelowEffects = new ArrayList<Effect>();

	public static ArrayList<Effect> recycleEffects = new ArrayList<Effect>();

	public static final SynchronizedList<INetworkEvent> eventsOut = new SynchronizedList<INetworkEvent>();
	public static final SynchronizedList<INetworkEvent> eventsIn = new SynchronizedList<INetworkEvent>();

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

	//Remember to change the version in android's build.gradle and ios's robovm.properties
	public static final String version = "Tanks 0.8.h";
	public static final int network_protocol = 12;
	public static boolean debug = false;

	public static int port = 8080;

	public static String lastParty = "";
	public static String lastOnlineServer = "";

	public static double levelSize = 1;
	
	public static Tank playerTank;
	
	public static boolean bulletLocked = false;
		
	public static boolean vsync = true;

	public static boolean enable3d = true;
	public static boolean enable3dBg = true;
	public static boolean angledView = false;

	public static boolean enableVibrations = true;

	public static boolean enableChatFilter = true;
	
	public static String crashMessage = "Yay! The game hasn't crashed yet!";
	public static long crashTime = 0;

	public static Screen screen;

	public static String ip = "";
	
	public static boolean fancyGraphics = true;

	public static boolean autostart = true;
	public static double startTime = 400;

	public static Screen lastOfflineScreen = null;

	public static boolean enableCustomTankRegistry = false;
	public static boolean enableCustomObstacleRegistry = false;

	public static RegistryTank registryTank = new RegistryTank();
	public static RegistryObstacle registryObstacle = new RegistryObstacle();

	public BaseWindow window;

	public BaseFileManager fileManager;

	public static Level currentLevel = null;	
	public static String currentLevelString = "";	
	
	public static ChatFilter chatFilter = new ChatFilter();
	
	public static PrintStream logger = System.err;
	
	public static String directoryPath = "/.tanks";
	public static final String logPath = directoryPath + "/logfile.txt";
	public static final String crashesPath = directoryPath + "/crashes/";
	public static final String tankRegistryPath = directoryPath + "/tank-registry.txt";
	public static final String obstacleRegistryPath = directoryPath + "/obstacle-registry.txt";
	public static final String optionsPath = directoryPath + "/options.txt";
	public static final String tutorialPath = directoryPath + "/tutorial.txt";
	public static final String uuidPath = directoryPath + "/uuid";

	public static String homedir;
	
	public static ArrayList<RegistryTank.DefaultTankEntry> defaultTanks = new ArrayList<RegistryTank.DefaultTankEntry>();
	public static ArrayList<RegistryObstacle.DefaultObstacleEntry> defaultObstacles = new ArrayList<RegistryObstacle.DefaultObstacleEntry>();

	public static Game game = new Game();

	public static boolean isOnlineServer;
	public static boolean connectedToOnline = false;

	private Game() {}

	public static void registerEvents()
	{
		NetworkEventMap.register(EventSendClientDetails.class);
		NetworkEventMap.register(EventKeepConnectionAlive.class);
		NetworkEventMap.register(EventConnectionSuccess.class);
		NetworkEventMap.register(EventKick.class);
		NetworkEventMap.register(EventAnnounceConnection.class);
		NetworkEventMap.register(EventChat.class);
		NetworkEventMap.register(EventPlayerChat.class);
		NetworkEventMap.register(EventLoadLevel.class);
		NetworkEventMap.register(EventEnterLevel.class);
		NetworkEventMap.register(EventLevelEnd.class);
		NetworkEventMap.register(EventReturnToLobby.class);
		NetworkEventMap.register(EventBeginCrusade.class);
		NetworkEventMap.register(EventReturnToCrusade.class);
		NetworkEventMap.register(EventLoadCrusadeHotbar.class);
		NetworkEventMap.register(EventAddShopItem.class);
		NetworkEventMap.register(EventSortShopButtons.class);
		NetworkEventMap.register(EventPurchaseItem.class);
		NetworkEventMap.register(EventSetItem.class);
		NetworkEventMap.register(EventSetItemBarSlot.class);
		NetworkEventMap.register(EventUpdateCoins.class);
		NetworkEventMap.register(EventPlayerReady.class);
		NetworkEventMap.register(EventUpdateReadyCount.class);
		NetworkEventMap.register(EventUpdateRemainingLives.class);
		NetworkEventMap.register(EventBeginLevelCountdown.class);
		NetworkEventMap.register(EventTankUpdate.class);
		NetworkEventMap.register(EventTankControllerUpdateS.class);
		NetworkEventMap.register(EventTankControllerUpdateC.class);
		NetworkEventMap.register(EventTankControllerUpdateAmmunition.class);
		NetworkEventMap.register(EventCreatePlayer.class);
		NetworkEventMap.register(EventCreateTank.class);
		NetworkEventMap.register(EventCreateCustomTank.class);
		NetworkEventMap.register(EventTankUpdateHealth.class);
		NetworkEventMap.register(EventShootBullet.class);
		NetworkEventMap.register(EventBulletUpdate.class);
		NetworkEventMap.register(EventBulletDestroyed.class);
		NetworkEventMap.register(EventBulletInstantWaypoint.class);
		NetworkEventMap.register(EventBulletAddAttributeModifier.class);
		NetworkEventMap.register(EventBulletElectricStunEffect.class);
		NetworkEventMap.register(EventLayMine.class);
		NetworkEventMap.register(EventMineExplode.class);
		NetworkEventMap.register(EventTankTeleport.class);
		NetworkEventMap.register(EventTankUpdateVisibility.class);
		NetworkEventMap.register(EventTankRedUpdateCharge.class);
		NetworkEventMap.register(EventTankAddAttributeModifier.class);
		NetworkEventMap.register(EventCreateFreezeEffect.class);
		NetworkEventMap.register(EventObstacleShrubberyBurn.class);
		NetworkEventMap.register(EventPlaySound.class);

		NetworkEventMap.register(EventSendOnlineClientDetails.class);
		NetworkEventMap.register(EventSilentDisconnect.class);
		NetworkEventMap.register(EventNewScreen.class);
		NetworkEventMap.register(EventSetScreen.class);
		NetworkEventMap.register(EventAddShape.class);
		NetworkEventMap.register(EventAddText.class);
		NetworkEventMap.register(EventAddButton.class);
		NetworkEventMap.register(EventAddTextBox.class);
		NetworkEventMap.register(EventAddMenuButton.class);
		NetworkEventMap.register(EventAddUUIDTextBox.class);
		NetworkEventMap.register(EventRemoveShape.class);
		NetworkEventMap.register(EventRemoveText.class);
		NetworkEventMap.register(EventRemoveButton.class);
		NetworkEventMap.register(EventRemoveTextBox.class);
		NetworkEventMap.register(EventRemoveMenuButton.class);
		NetworkEventMap.register(EventSetPauseScreenTitle.class);
		NetworkEventMap.register(EventPressedButton.class);
		NetworkEventMap.register(EventSetTextBox.class);
		NetworkEventMap.register(EventUploadLevel.class);
		NetworkEventMap.register(EventSendLevelToDownload.class);
		NetworkEventMap.register(EventCleanUp.class);
	}

	public static void initScript()
	{
		player = new Player(clientID, "");
		Game.players.add(player);

		Drawing.initialize();
		Panel.initialize();
		Game.exitToTitle();

		registerEvents();

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

		if (Game.framework == Framework.libgdx)
			homedir = "";

		BaseFile directoryFile = game.fileManager.getFile(homedir + directoryPath);
		if (!directoryFile.exists() && Game.framework != Framework.libgdx)
		{
			directoryFile.mkdirs();
			game.fileManager.getFile(homedir + directoryPath + ScreenSavedLevels.levelDir).mkdirs();

			try 
			{
				game.fileManager.getFile(homedir + logPath).create();
				Game.logger = new PrintStream(new FileOutputStream(homedir + logPath, true));
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
				System.exit(1);
			}
		}

		BaseFile tankRegistryFile = game.fileManager.getFile(homedir + tankRegistryPath);
		if (!tankRegistryFile.exists())
		{
			RegistryTank.initRegistry(homedir);
		}

		BaseFile obstacleRegistryFile = game.fileManager.getFile(homedir + obstacleRegistryPath);
		if (!obstacleRegistryFile.exists())
		{
			RegistryObstacle.initRegistry(homedir);
		}

		BaseFile uuidFile = game.fileManager.getFile(homedir + uuidPath);
		if (!uuidFile.exists())
		{
			try
			{
				uuidFile.create();
				uuidFile.startWriting();
				uuidFile.println(UUID.randomUUID().toString());
				uuidFile.println("IMPORTANT: This file contains an ID unique to your computer.");
				uuidFile.println("The file can be used by online services. Deleting or modifying");
				uuidFile.println("the file or its contents can cause loss of online data.");
				uuidFile.stopWriting();
			}
			catch (IOException e)
			{
				e.printStackTrace();
				System.exit(1);
			}
		}

		try
		{
			uuidFile.startReading();
			Game.computerID = UUID.fromString(uuidFile.nextLine());
			uuidFile.stopReading();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(1);
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

		RegistryTank.loadRegistry(homedir);
		RegistryObstacle.loadRegistry(homedir);
	}
	
	public static boolean usernameInvalid(String username)
	{
		if (username.length() > 18)
			return true;
			
		for (int i = 0; i < username.length(); i++)
		{
			if (!"abcdefghijklmnopqrstuvwxyz1234567890_".contains(username.toLowerCase().substring(i, i+1)))
			{
				return true;
			}
		}
		
		return false;
	}

	public static void removePlayer(UUID id)
	{
		for (int i = 0; i < Game.players.size(); i++)
		{
			if (Game.players.get(i).clientID.equals(id))
			{
				Game.players.remove(i);
				i--;
			}
		}
	}

	public static String timeInterval(long time1, long time2)
	{
		long secs = (time2 - time1) / 1000;
		long mins = secs / 60;
		long hours = mins / 60;
		long days = hours / 24;

		if (days > 7)
			return days + "d";
		else if (days > 0)
			return days + "d " + hours % 24 + "h";
		else if (hours > 0)
			return hours % 24 + "h " + mins % 60 + "m";
		else if (mins > 0)
			return mins % 60 + "m";
		else
			return "less than 1m";
	}
	
	public static void reset()
	{
		resetNetworkIDs();

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

		resetNetworkIDs();

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
		Game.loadLevel(game.fileManager.getFile(Game.homedir + ScreenSavedLevels.levelDir + "/" + name), s);
		Game.screen = s;
	}
	
	public static void exitToCrash(Throwable e)
	{
		System.gc();

		e.printStackTrace();

		if (ScreenPartyHost.isServer && ScreenPartyHost.server != null)
			ScreenPartyHost.server.close("The party has ended because the host crashed");
		
		if (ScreenPartyLobby.isClient || Game.connectedToOnline)
			Client.handler.ctx.close();

		ScreenPartyHost.isServer = false;
		ScreenPartyLobby.isClient = false;

		obstacles.clear();
		belowEffects.clear();
		movables.clear();
		effects.clear();
		eventsIn.clear();
		eventsOut.clear();

		Game.crashMessage = e.toString();
		Game.crashTime = System.currentTimeMillis();
		Game.logger.println(new Date().toString() + " (syserr) the game has crashed! below is a crash report, good luck:");
		e.printStackTrace(Game.logger);

		if (!(Game.screen instanceof ScreenCrashed) && !(Game.screen instanceof ScreenOutOfMemory))
		{
			try
			{
				BaseFile dir = Game.game.fileManager.getFile(Game.homedir + Game.crashesPath);
				if (!dir.exists())
					dir.mkdirs();

				BaseFile f = Game.game.fileManager.getFile(Game.homedir + Game.crashesPath + Game.crashTime + ".crash");
				f.create();

				f.startWriting();
				f.println("Tanks crash report: " + Game.version + " - " + new Date().toString() + "\n");

				f.println(e.toString());
				for (StackTraceElement el: e.getStackTrace())
				{
					f.println("at " + el.toString());
				}

				f.println("\nSystem properties:");
				Properties p = System.getProperties();
				for (Object s: p.keySet())
					f.println(s + ": " + p.get(s));

				f.stopWriting();
			}
			catch (Exception ex) {ex.printStackTrace();}
		}

		if (e instanceof OutOfMemoryError)
			screen = new ScreenOutOfMemory();
		else
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
		Panel.panel.zoomTimer = 0;
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

        resetNetworkIDs();

        Panel.panel.hotbar.currentCoins = new Coins();
        Panel.panel.hotbar.enabledCoins = false;
        Panel.panel.hotbar.currentItemBar = new ItemBar(Game.player, Panel.panel.hotbar);
        Panel.panel.hotbar.enabledItemBar = false;
    }

    public static void resetNetworkIDs()
	{
		Tank.currentID = 0;
		Tank.idMap.clear();
		Tank.freeIDs.clear();

		Bullet.currentID = 0;
		Bullet.idMap.clear();
		Bullet.freeIDs.clear();

		Mine.currentID = 0;
		Mine.idMap.clear();
		Mine.freeIDs.clear();
	}
	
	public static void loadLevel(BaseFile f)
	{
		Game.loadLevel(f, null);
	}
	
	public static void loadLevel(BaseFile f, ILevelPreviewScreen s)
	{
		try
		{
			f.startReading();

			while (f.hasNextLine())
			{
				String line = f.nextLine();
				Level l = new Level(line);
				l.loadLevel(s);
			}

			f.stopReading();
		}
		catch (FileNotFoundException e)
		{
			Game.exitToCrash(e);
		}
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
