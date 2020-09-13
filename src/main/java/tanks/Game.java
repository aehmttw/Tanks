package tanks;

import basewindow.BaseFile;
import basewindow.BaseFileManager;
import basewindow.BaseWindow;
import tanks.bullet.Bullet;
import tanks.event.*;
import tanks.event.online.*;
import tanks.gui.Button;
import tanks.gui.ChatFilter;
import tanks.gui.input.InputBindingGroup;
import tanks.gui.input.InputBindings;
import tanks.gui.screen.*;
import tanks.hotbar.Hotbar;
import tanks.hotbar.ItemBar;
import tanks.hotbar.item.ItemBullet;
import tanks.hotbar.item.ItemShield;
import tanks.network.Client;
import tanks.network.NetworkEventMap;
import tanks.network.SynchronizedList;
import tanks.obstacle.*;
import tanks.registry.RegistryItem;
import tanks.registry.RegistryObstacle;
import tanks.registry.RegistryTank;
import tanks.tank.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

public class Game
{
	public enum Framework {lwjgl, swing, libgdx}
	public static Framework framework;

	public static final double tile_size = 50;

	public static UUID computerID;
	public static final UUID clientID = UUID.randomUUID();

	public static final int absoluteDepthBase = 1000;

	public static ArrayList<Face> horizontalFaces = new ArrayList<Face>();
	public static ArrayList<Face> verticalFaces = new ArrayList<Face>();

	public boolean[][] solidGrid;

	public static ArrayList<Movable> movables = new ArrayList<Movable>();
	public static ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();
	public static ArrayList<Effect> effects = new ArrayList<Effect>();
	public static ArrayList<Effect> tracks = new ArrayList<Effect>();
	public static SynchronizedList<Player> players = new SynchronizedList<Player>();
	public static Player player;

	public static ArrayList<Movable> removeMovables = new ArrayList<Movable>();
	public static ArrayList<Obstacle> removeObstacles = new ArrayList<Obstacle>();
	public static ArrayList<Effect> removeEffects = new ArrayList<Effect>();
	public static ArrayList<Effect> removeTracks = new ArrayList<Effect>();

	public static Queue<Effect> recycleEffects = new LinkedList<Effect>();

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
	public static final String version = "Tanks v0.9.0";
	public static final int network_protocol = 20;
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

	public static boolean soundsEnabled = true;
	public static boolean musicEnabled = true;

	public static boolean antialiasing = false;

	public static boolean enableVibrations = true;

	public static boolean enableChatFilter = true;

	public static String crashMessage = "Yay! The game hasn't crashed yet!";
	public static long crashTime = 0;

    public static double[] color = new double[3];

    public static Screen screen;

	public static String ip = "";

	public static boolean fancyGraphics = true;
	public static boolean superGraphics = true;

	public static boolean autostart = true;
	public static double startTime = 400;

	public static Screen lastOfflineScreen = null;

	public static boolean enableCustomTankRegistry = false;
	public static boolean enableCustomObstacleRegistry = false;
	public static boolean enableCustomItemRegistry = false;

	public static RegistryTank registryTank = new RegistryTank();
	public static RegistryObstacle registryObstacle = new RegistryObstacle();
	public static RegistryItem registryItem = new RegistryItem();

	public BaseWindow window;

	public BaseFileManager fileManager;

	public static Level currentLevel = null;
	public static String currentLevelString = "";

	public static ChatFilter chatFilter = new ChatFilter();

	public ArrayList<InputBindingGroup> inputBindings = new ArrayList<>();
	public InputBindings input;

	public static PrintStream logger = System.err;

	public static String directoryPath = "/.tanks";
	public static final String logPath = directoryPath + "/logfile.txt";
	public static final String crashesPath = directoryPath + "/crashes/";
	public static final String tankRegistryPath = directoryPath + "/tank-registry.txt";
	public static final String obstacleRegistryPath = directoryPath + "/obstacle-registry.txt";
	public static final String itemRegistryPath = directoryPath + "/item-registry.txt";
	public static final String optionsPath = directoryPath + "/options.txt";
	public static final String controlsPath = directoryPath + "/controls.txt";
	public static final String tutorialPath = directoryPath + "/tutorial.txt";
	public static final String uuidPath = directoryPath + "/uuid";
	public static final String savedCrusadePath = directoryPath + "/crusades/progress/";

	public static final float musicVolume = 0.5f;

	public static String homedir;

	public static ArrayList<RegistryTank.DefaultTankEntry> defaultTanks = new ArrayList<RegistryTank.DefaultTankEntry>();
	public static ArrayList<RegistryObstacle.DefaultObstacleEntry> defaultObstacles = new ArrayList<RegistryObstacle.DefaultObstacleEntry>();
	public static ArrayList<RegistryItem.DefaultItemEntry> defaultItems = new ArrayList<RegistryItem.DefaultItemEntry>();

	public static Game game = new Game();

	public static boolean isOnlineServer;
	public static boolean connectedToOnline = false;

	private Game()
	{
		Game.game = this;
		input = new InputBindings();
	}

	public static void registerEvents()
	{
		NetworkEventMap.register(EventSendClientDetails.class);
		NetworkEventMap.register(EventPing.class);
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
		NetworkEventMap.register(EventUpdateReadyPlayers.class);
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
		NetworkEventMap.register(EventBulletBounce.class);
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
		NetworkEventMap.register(EventSendTankColors.class);
		NetworkEventMap.register(EventShareLevel.class);

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
		NetworkEventMap.register(EventSetMusic.class);
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

		Hotbar.toggle = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - 20, 150, 40, "", new Runnable()
		{
			@Override
			public void run()
			{
				Game.player.hotbar.persistent = !Game.player.hotbar.persistent;
			}
		}
		);

		registerEvents();

		ItemBullet.initializeMaps();

		defaultObstacles.add(new RegistryObstacle.DefaultObstacleEntry(Obstacle.class, "normal"));
		defaultObstacles.add(new RegistryObstacle.DefaultObstacleEntry(ObstacleIndestructible.class, "hard"));
		defaultObstacles.add(new RegistryObstacle.DefaultObstacleEntry(ObstacleHole.class, "hole"));
		defaultObstacles.add(new RegistryObstacle.DefaultObstacleEntry(ObstacleBouncy.class, "bouncy"));
		defaultObstacles.add(new RegistryObstacle.DefaultObstacleEntry(ObstacleShrubbery.class, "shrub"));
		defaultObstacles.add(new RegistryObstacle.DefaultObstacleEntry(ObstacleMud.class, "mud"));
		defaultObstacles.add(new RegistryObstacle.DefaultObstacleEntry(ObstacleIce.class, "ice"));
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

		defaultItems.add(new RegistryItem.DefaultItemEntry(ItemBullet.class, ItemBullet.item_name));
		defaultItems.add(new RegistryItem.DefaultItemEntry(ItemShield.class, ItemShield.item_name));

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

		BaseFile itemRegistryFile = game.fileManager.getFile(homedir + itemRegistryPath);
		if (!itemRegistryFile.exists())
		{
			RegistryItem.initRegistry(homedir);
		}

		BaseFile savedCrusadesProgressFile = game.fileManager.getFile(homedir + savedCrusadePath + "/internal");
		if (!savedCrusadesProgressFile.exists())
		{
			savedCrusadesProgressFile.mkdirs();
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
		RegistryItem.loadRegistry(homedir);

		BaseFile optionsFile = Game.game.fileManager.getFile(Game.homedir + Game.optionsPath);
		if (!optionsFile.exists())
		{
			ScreenOptions.initOptions(Game.homedir);
		}

		ScreenOptions.loadOptions(Game.homedir);
		game.input.file = game.fileManager.getFile(Game.homedir + Game.controlsPath);
		game.input.load();

		createModels();
	}

	public static void createModels()
	{
		if (Game.framework == Framework.swing)
		{
			double centerFrac = 0.8;

			Tank.base_model.shapes = new Model.Shape[1];
			Tank.base_model.shapes[0] = new Model.LegacySquare(1, 1);

			Tank.color_model.shapes = new Model.Shape[1];
			Tank.color_model.shapes[0] = new Model.LegacySquare(centerFrac, centerFrac);

			Drawing.rotatedRect.shapes = new Model.Shape[1];
			Drawing.rotatedRect.shapes[0] = new Model.LegacySquare(1, 1);
		}
		else
		{
			Tank.base_model.shapes = new Model.Shape[10];

			double size = 0.5;
			double longSize = size + 0.1;
			double height = size;
			double halfHeight = height / 2;
			double colorEdge = 0.35;
			double colorMargin = 0.05;
			double colorMarginEdge = colorMargin / 2;

			Tank.base_model.shapes[0] = new Model.Quad(
					new Model.Point(-size, -size, 0),
					new Model.Point(size, -size, 0),
					new Model.Point(size, size, 0),
					new Model.Point(-size, size, 0), 0.4);
			Tank.base_model.shapes[1] = new Model.Quad(
					new Model.Point(-longSize, -size, halfHeight),
					new Model.Point(-size, -size, 0),
					new Model.Point(-size, size, 0),
					new Model.Point(-longSize, size, halfHeight), 0.4);
			Tank.base_model.shapes[2] = new Model.Quad(
					new Model.Point(size, -size, 0),
					new Model.Point(longSize, -size, halfHeight),
					new Model.Point(longSize, size, halfHeight),
					new Model.Point(size, size, 0), 0.4);
			Tank.base_model.shapes[3] = new Model.Quad(
					new Model.Point(-longSize, -size, halfHeight),
					new Model.Point(-size, -size, height),
					new Model.Point(-size, size, height),
					new Model.Point(-longSize, size, halfHeight), 0.8);
			Tank.base_model.shapes[4] = new Model.Quad(
					new Model.Point(size, -size, height),
					new Model.Point(longSize, -size, halfHeight),
					new Model.Point(longSize, size, halfHeight),
					new Model.Point(size, size, height), 0.8);
			Tank.base_model.shapes[5] = new Model.Quad(
					new Model.Point(-longSize, -size, halfHeight),
					new Model.Point(-size, -size, height),
					new Model.Point(size, -size, height),
					new Model.Point(longSize, -size, halfHeight), 0.6);
			Tank.base_model.shapes[6] = new Model.Quad(
					new Model.Point(-longSize, -size, halfHeight),
					new Model.Point(-size, -size, 0),
					new Model.Point(size, -size, 0),
					new Model.Point(longSize, -size, halfHeight), 0.6);
			Tank.base_model.shapes[7] = new Model.Quad(
					new Model.Point(-longSize, size, halfHeight),
					new Model.Point(-size, size, height),
					new Model.Point(size, size, height),
					new Model.Point(longSize, size, halfHeight), 0.6);
			Tank.base_model.shapes[8] = new Model.Quad(
					new Model.Point(-longSize, size, halfHeight),
					new Model.Point(-size, size, 0),
					new Model.Point(size, size, 0),
					new Model.Point(longSize, size, halfHeight), 0.6);
			Tank.base_model.shapes[9] = new Model.Quad(
					new Model.Point(-size, -size, height),
					new Model.Point(size, -size, height),
					new Model.Point(size, size, height),
					new Model.Point(-size, size, height), 1);


			Tank.color_model.shapes = new Model.Shape[10];
			Tank.color_model.shapes[0] = new Model.Quad(
					new Model.Point(-size, -colorEdge, -colorMargin),
					new Model.Point(size, -colorEdge, -colorMargin),
					new Model.Point(size, colorEdge, -colorMargin),
					new Model.Point(-size, colorEdge, -colorMargin), 0.4);
			Tank.color_model.shapes[1] = new Model.Quad(
					new Model.Point(-(longSize + colorMarginEdge), -colorEdge, halfHeight),
					new Model.Point(-size, -colorEdge, -colorMargin),
					new Model.Point(-size, colorEdge, -colorMargin),
					new Model.Point(-(longSize + colorMarginEdge), colorEdge, halfHeight), 0.4);
			Tank.color_model.shapes[2] = new Model.Quad(
					new Model.Point(size, -colorEdge, -colorMargin),
					new Model.Point((longSize + colorMarginEdge), -colorEdge, halfHeight),
					new Model.Point((longSize + colorMarginEdge), colorEdge, halfHeight),
					new Model.Point(size, colorEdge, -colorMargin), 0.4);
			Tank.color_model.shapes[3] = new Model.Quad(
					new Model.Point(-(longSize + colorMarginEdge), -colorEdge, halfHeight),
					new Model.Point(-size, -colorEdge, height + colorMargin),
					new Model.Point(-size, colorEdge, height + colorMargin),
					new Model.Point(-(longSize + colorMarginEdge), colorEdge, halfHeight), 0.8);
			Tank.color_model.shapes[4] = new Model.Quad(
					new Model.Point(size, -colorEdge, height + colorMargin),
					new Model.Point((longSize + colorMarginEdge), -colorEdge, halfHeight),
					new Model.Point((longSize + colorMarginEdge), colorEdge, halfHeight),
					new Model.Point(size, colorEdge, height + colorMargin), 0.8);
			Tank.color_model.shapes[5] = new Model.Quad(
					new Model.Point(-(longSize + colorMarginEdge), -colorEdge, halfHeight),
					new Model.Point(-size, -colorEdge, height + colorMargin),
					new Model.Point(size, -colorEdge, height + colorMargin),
					new Model.Point((longSize + colorMarginEdge), -colorEdge, halfHeight), 0.6);
			Tank.color_model.shapes[6] = new Model.Quad(
					new Model.Point(-(longSize + colorMarginEdge), -colorEdge, halfHeight),
					new Model.Point(-size, -colorEdge, -colorMargin),
					new Model.Point(size, -colorEdge, -colorMargin),
					new Model.Point((longSize + colorMarginEdge), -colorEdge, halfHeight), 0.6);
			Tank.color_model.shapes[7] = new Model.Quad(
					new Model.Point(-(longSize + colorMarginEdge), colorEdge, halfHeight),
					new Model.Point(-size, colorEdge, height + colorMargin),
					new Model.Point(size, colorEdge, height + colorMargin),
					new Model.Point((longSize + colorMarginEdge), colorEdge, halfHeight), 0.6);
			Tank.color_model.shapes[8] = new Model.Quad(
					new Model.Point(-(longSize + colorMarginEdge), colorEdge, halfHeight),
					new Model.Point(-size, colorEdge, -colorMargin),
					new Model.Point(size, colorEdge, -colorMargin),
					new Model.Point((longSize + colorMarginEdge), colorEdge, halfHeight), 0.6);
			Tank.color_model.shapes[9] = new Model.Quad(
					new Model.Point(-size, -colorEdge, height + colorMargin),
					new Model.Point(size, -colorEdge, height + colorMargin),
					new Model.Point(size, colorEdge, height + colorMargin),
					new Model.Point(-size, colorEdge, height + colorMargin), 1);

			double turretThickness = 0.08;
			double turretLength = 1.00;
			Turret.turret_model.shapes = new Model.Shape[6];
			Turret.turret_model.shapes[0] = new Model.Quad(
					new Model.Point(-turretThickness, -turretThickness, -turretThickness),
					new Model.Point(turretLength + turretThickness, -turretThickness, -turretThickness),
					new Model.Point(turretLength + turretThickness, turretThickness, -turretThickness),
					new Model.Point(-turretThickness, turretThickness, -turretThickness), 0.4);
			Turret.turret_model.shapes[1] = new Model.Quad(
					new Model.Point(-turretThickness, -turretThickness, turretThickness),
					new Model.Point(turretLength + turretThickness, -turretThickness, turretThickness),
					new Model.Point(turretLength + turretThickness, -turretThickness, -turretThickness),
					new Model.Point(-turretThickness, -turretThickness, -turretThickness), 0.8);
			Turret.turret_model.shapes[2] = new Model.Quad(
					new Model.Point(-turretThickness, -turretThickness, turretThickness),
					new Model.Point(-turretThickness, -turretThickness, turretThickness),
					new Model.Point(-turretThickness, turretThickness, -turretThickness),
					new Model.Point(-turretThickness, turretThickness, -turretThickness), 0.6);
			Turret.turret_model.shapes[3] = new Model.Quad(
					new Model.Point(-turretThickness, turretThickness, turretThickness),
					new Model.Point(turretLength + turretThickness, turretThickness, turretThickness),
					new Model.Point(turretLength + turretThickness, turretThickness, -turretThickness),
					new Model.Point(-turretThickness, turretThickness, -turretThickness), 0.8);
			Turret.turret_model.shapes[4] = new Model.Quad(
					new Model.Point(turretLength + turretThickness, -turretThickness, -turretThickness),
					new Model.Point(turretLength + turretThickness, -turretThickness, turretThickness),
					new Model.Point(turretLength + turretThickness, turretThickness, turretThickness),
					new Model.Point(turretLength + turretThickness, turretThickness, -turretThickness), 0.6);
			Turret.turret_model.shapes[5] = new Model.Quad(
					new Model.Point(-turretThickness, -turretThickness, turretThickness),
					new Model.Point(turretLength + turretThickness, -turretThickness, turretThickness),
					new Model.Point(turretLength + turretThickness, turretThickness, turretThickness),
					new Model.Point(-turretThickness, turretThickness, turretThickness), 1);

			double turretTopSize = 0.25;
			double turretBaseSize = 0.30;
			double turretDepth = 0.30;

			Turret.base_model.shapes = new Model.Shape[5];
			Turret.base_model.shapes[0] = new Model.Quad(
					new Model.Point(-turretTopSize, -turretTopSize, turretDepth),
					new Model.Point(turretTopSize, -turretTopSize, turretDepth),
					new Model.Point(turretTopSize, turretTopSize, turretDepth),
					new Model.Point(-turretTopSize, turretTopSize, turretDepth), 1);
			Turret.base_model.shapes[1] = new Model.Quad(
					new Model.Point(-turretTopSize, -turretTopSize, turretDepth),
					new Model.Point(turretTopSize, -turretTopSize, turretDepth),
					new Model.Point(turretBaseSize, -turretBaseSize, 0),
					new Model.Point(-turretBaseSize, -turretBaseSize, 0), 0.8);
			Turret.base_model.shapes[2] = new Model.Quad(
					new Model.Point(turretTopSize, -turretTopSize, turretDepth),
					new Model.Point(turretTopSize, turretTopSize, turretDepth),
					new Model.Point(turretBaseSize, turretBaseSize, 0),
					new Model.Point(turretBaseSize, -turretBaseSize, 0), 0.6);
			Turret.base_model.shapes[3] = new Model.Quad(
					new Model.Point(-turretTopSize, turretTopSize, turretDepth),
					new Model.Point(turretTopSize, turretTopSize, turretDepth),
					new Model.Point(turretBaseSize, turretBaseSize, 0),
					new Model.Point(-turretBaseSize, turretBaseSize, 0), 0.8);
			Turret.base_model.shapes[4] = new Model.Quad(
					new Model.Point(-turretTopSize, -turretTopSize, turretDepth),
					new Model.Point(-turretTopSize, turretTopSize, turretDepth),
					new Model.Point(-turretBaseSize, turretBaseSize, 0),
					new Model.Point(-turretBaseSize, -turretBaseSize, 0), 0.6);

			Drawing.rotatedRect.shapes = new Model.Shape[1];
			Drawing.rotatedRect.shapes[0] = new Model.Quad(
					new Model.Point(-0.5, -0.5, 0),
					new Model.Point(0.5, -0.5, 0),
					new Model.Point(0.5, 0.5, 0),
					new Model.Point(-0.5, 0.5, 0), 1);

			double innerHealthEdge = 0.55;
			double outerHealthEdge = 0.575;
			double lengthMul = 1.2;
			double healthHeight = 0.025;
			Tank.health_model.shapes = new Model.Shape[16];

			Tank.health_model.shapes[0] = new Model.Quad(
					new Model.Point(-outerHealthEdge * lengthMul, -outerHealthEdge, 0),
					new Model.Point(-innerHealthEdge * lengthMul, -outerHealthEdge, 0),
					new Model.Point(-innerHealthEdge * lengthMul, innerHealthEdge, 0),
					new Model.Point(-outerHealthEdge * lengthMul, innerHealthEdge, 0), 0.4);
			Tank.health_model.shapes[1] = new Model.Quad(
					new Model.Point(innerHealthEdge * lengthMul, -innerHealthEdge, 0),
					new Model.Point(outerHealthEdge * lengthMul, -innerHealthEdge, 0),
					new Model.Point(outerHealthEdge * lengthMul, outerHealthEdge, 0),
					new Model.Point(innerHealthEdge * lengthMul, outerHealthEdge, 0), 0.4);
			Tank.health_model.shapes[2] = new Model.Quad(
					new Model.Point(-innerHealthEdge * lengthMul, -outerHealthEdge, 0),
					new Model.Point(outerHealthEdge * lengthMul, -outerHealthEdge, 0),
					new Model.Point(outerHealthEdge * lengthMul, -innerHealthEdge, 0),
					new Model.Point(-innerHealthEdge * lengthMul, -innerHealthEdge, 0), 0.4);
			Tank.health_model.shapes[3] = new Model.Quad(
					new Model.Point(-outerHealthEdge * lengthMul, innerHealthEdge, 0),
					new Model.Point(innerHealthEdge * lengthMul, innerHealthEdge, 0),
					new Model.Point(innerHealthEdge * lengthMul, outerHealthEdge, 0),
					new Model.Point(-outerHealthEdge * lengthMul, outerHealthEdge, 0), 0.4);

			Tank.health_model.shapes[4] = new Model.Quad(
					new Model.Point(-outerHealthEdge * lengthMul, -outerHealthEdge, 0),
					new Model.Point(-outerHealthEdge * lengthMul, -outerHealthEdge, healthHeight),
					new Model.Point(-outerHealthEdge * lengthMul, outerHealthEdge, healthHeight),
					new Model.Point(-outerHealthEdge * lengthMul, outerHealthEdge, 0), 0.6);
			Tank.health_model.shapes[5] = new Model.Quad(
					new Model.Point(outerHealthEdge * lengthMul, -outerHealthEdge, 0),
					new Model.Point(outerHealthEdge * lengthMul, -outerHealthEdge, healthHeight),
					new Model.Point(outerHealthEdge * lengthMul, outerHealthEdge, healthHeight),
					new Model.Point(outerHealthEdge * lengthMul, outerHealthEdge, 0), 0.6);
			Tank.health_model.shapes[6] = new Model.Quad(
					new Model.Point(-outerHealthEdge * lengthMul, -outerHealthEdge, 0),
					new Model.Point(-outerHealthEdge * lengthMul, -outerHealthEdge, healthHeight),
					new Model.Point(outerHealthEdge * lengthMul, -outerHealthEdge, healthHeight),
					new Model.Point(outerHealthEdge * lengthMul, -outerHealthEdge, 0), 0.8);
			Tank.health_model.shapes[7] = new Model.Quad(
					new Model.Point(-outerHealthEdge * lengthMul, outerHealthEdge, 0),
					new Model.Point(-outerHealthEdge * lengthMul, outerHealthEdge, healthHeight),
					new Model.Point(outerHealthEdge * lengthMul, outerHealthEdge, healthHeight),
					new Model.Point(outerHealthEdge * lengthMul, outerHealthEdge, 0), 0.8);

			Tank.health_model.shapes[8] = new Model.Quad(
					new Model.Point(-innerHealthEdge * lengthMul, -innerHealthEdge, 0),
					new Model.Point(-innerHealthEdge * lengthMul, -innerHealthEdge, healthHeight),
					new Model.Point(-innerHealthEdge * lengthMul, innerHealthEdge, healthHeight),
					new Model.Point(-innerHealthEdge * lengthMul, innerHealthEdge, 0), 0.6);
			Tank.health_model.shapes[9] = new Model.Quad(
					new Model.Point(innerHealthEdge * lengthMul, -innerHealthEdge, 0),
					new Model.Point(innerHealthEdge * lengthMul, -innerHealthEdge, healthHeight),
					new Model.Point(innerHealthEdge * lengthMul, innerHealthEdge, healthHeight),
					new Model.Point(innerHealthEdge * lengthMul, innerHealthEdge, 0), 0.6);
			Tank.health_model.shapes[10] = new Model.Quad(
					new Model.Point(-innerHealthEdge * lengthMul, -innerHealthEdge, 0),
					new Model.Point(-innerHealthEdge * lengthMul, -innerHealthEdge, healthHeight),
					new Model.Point(innerHealthEdge * lengthMul, -innerHealthEdge, healthHeight),
					new Model.Point(innerHealthEdge * lengthMul, -innerHealthEdge, 0), 0.8);
			Tank.health_model.shapes[11] = new Model.Quad(
					new Model.Point(-innerHealthEdge * lengthMul, innerHealthEdge, 0),
					new Model.Point(-innerHealthEdge * lengthMul, innerHealthEdge, healthHeight),
					new Model.Point(innerHealthEdge * lengthMul, innerHealthEdge, healthHeight),
					new Model.Point(innerHealthEdge * lengthMul, innerHealthEdge, 0), 0.8);

			Tank.health_model.shapes[12] = new Model.Quad(
					new Model.Point(-outerHealthEdge * lengthMul, -outerHealthEdge, healthHeight),
					new Model.Point(-innerHealthEdge * lengthMul, -outerHealthEdge, healthHeight),
					new Model.Point(-innerHealthEdge * lengthMul, innerHealthEdge, healthHeight),
					new Model.Point(-outerHealthEdge * lengthMul, innerHealthEdge, healthHeight), 1);
			Tank.health_model.shapes[13] = new Model.Quad(
					new Model.Point(innerHealthEdge * lengthMul, -innerHealthEdge, healthHeight),
					new Model.Point(outerHealthEdge * lengthMul, -innerHealthEdge, healthHeight),
					new Model.Point(outerHealthEdge * lengthMul, outerHealthEdge, healthHeight),
					new Model.Point(innerHealthEdge * lengthMul, outerHealthEdge, healthHeight), 1);
			Tank.health_model.shapes[14] = new Model.Quad(
					new Model.Point(-innerHealthEdge * lengthMul, -outerHealthEdge, healthHeight),
					new Model.Point(outerHealthEdge * lengthMul, -outerHealthEdge, healthHeight),
					new Model.Point(outerHealthEdge * lengthMul, -innerHealthEdge, healthHeight),
					new Model.Point(-innerHealthEdge * lengthMul, -innerHealthEdge, healthHeight), 1);
			Tank.health_model.shapes[15] = new Model.Quad(
					new Model.Point(-outerHealthEdge * lengthMul, innerHealthEdge, healthHeight),
					new Model.Point(innerHealthEdge * lengthMul, innerHealthEdge, healthHeight),
					new Model.Point(innerHealthEdge * lengthMul, outerHealthEdge, healthHeight),
					new Model.Point(-outerHealthEdge * lengthMul, outerHealthEdge, healthHeight), 1);
		}
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

	public static String formatString(String s)
	{
		if (s.length() == 0)
			return s;
		else if (s.length() == 1)
			return s.toUpperCase();
		else
			return Character.toUpperCase(s.charAt(0)) + s.substring(1).replace("-", " ").replace("_", " ").toLowerCase();
	}

	public static void reset()
	{
		resetNetworkIDs();

		obstacles.clear();
		tracks.clear();
		movables.clear();
		effects.clear();
		recycleEffects.clear();
		removeEffects.clear();
		removeTracks.clear();

		System.gc();
		start();
	}

	public static void exit()
	{
		screen = new ScreenInterlevel();

		resetNetworkIDs();

		obstacles.clear();
		tracks.clear();
		movables.clear();
		effects.clear();
		recycleEffects.clear();
		removeEffects.clear();
		removeTracks.clear();

		System.gc();
	}

	public static void exit(String name)
	{
		obstacles.clear();
		tracks.clear();
		movables.clear();
		effects.clear();
		recycleEffects.clear();
		removeEffects.clear();
		removeTracks.clear();

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
		tracks.clear();
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
		removeTracks.clear();

		System.gc();

		Drawing.drawing.playSound("leave.ogg");
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

	public static double sampleHeight(double px, double py)
	{
		int x = (int) (px / Game.tile_size);
		int y = (int) (py / Game.tile_size);

		if (!Game.fancyGraphics || !Game.enable3d || x < 0 || x >= Game.currentSizeX || y < 0 || y >= Game.currentSizeY)
			return 0;
		else
			return Game.tilesDepth[x][y] + 0;
	}

	public static boolean stringsEqual(String a, String b)
	{
		if (a == null && b == null)
			return true;

		if (a == null || b == null)
			return false;

		return a.equals(b);
	}

	public static double[] getRainbowColor(double fraction)
    {
        double col = fraction * 255 * 6;

        double r = 0;
        double g = 0;
        double b = 0;

        if (col <= 255)
        {
            r = 255;
            g = col;
            b = 0;
        }
        else if (col <= 255 * 2)
        {
            r = 255 * 2 - col;
            g = 255;
            b = 0;
        }
        else if (col <= 255 * 3)
        {
            g = 255;
            b = col - 255 * 2;
        }
        else if (col <= 255 * 4)
        {
            g = 255 * 4 - col;
            b = 255;
        }
        else if (col <= 255 * 5)
        {
            r = col - 255 * 4;
            g = 0;
            b = 255;
        }
        else if (col <= 255 * 6)
        {
            r = 255;
            g = 0;
            b = 255 * 6 - col;
        }

        color[0] = r;
        color[1] = g;
        color[2] = b;

        return color;
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
		Drawing.drawing.setScreenBounds(Game.tile_size * 28, Game.tile_size * 18);
		obstacles.clear();
		tracks.clear();
		movables.clear();
		effects.clear();
		recycleEffects.clear();
		removeEffects.clear();
		removeTracks.clear();

		resetNetworkIDs();

		Game.player.hotbar.coins = 0;
		Game.player.hotbar.enabledCoins = false;
		Game.player.hotbar.itemBar = new ItemBar(Game.player);
		Game.player.hotbar.enabledItemBar = false;
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

	public static boolean loadLevel(BaseFile f)
	{
		return Game.loadLevel(f, null);
	}

	public static boolean loadLevel(BaseFile f, ILevelPreviewScreen s)
	{
		String line = "Could not find level contents!";
		try
		{
			f.startReading();

			while (f.hasNextLine())
			{
				line = f.nextLine();
				Level l = new Level(line);
				l.loadLevel(s);
			}

			f.stopReading();
			return true;
		}
		catch (Exception e)
		{
			Game.screen = new ScreenFailedToLoadLevel(f.path, line, e, Game.screen);
			return false;
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
