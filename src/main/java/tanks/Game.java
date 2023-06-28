package tanks;

import basewindow.BaseFile;
import basewindow.BaseFileManager;
import basewindow.BaseWindow;
import basewindow.ModelPart;
import tanks.bullet.*;
import tanks.bullet.legacy.BulletAir;
import tanks.extension.Extension;
import tanks.extension.ExtensionRegistry;
import tanks.generator.LevelGenerator;
import tanks.generator.LevelGeneratorRandom;
import tanks.gui.Button;
import tanks.gui.ChatFilter;
import tanks.gui.input.InputBindingGroup;
import tanks.gui.input.InputBindings;
import tanks.gui.screen.*;
import tanks.gui.screen.leveleditor.OverlayEditorMenu;
import tanks.gui.screen.leveleditor.ScreenLevelEditor;
import tanks.hotbar.Hotbar;
import tanks.hotbar.ItemBar;
import tanks.hotbar.item.Item;
import tanks.hotbar.item.ItemBullet;
import tanks.hotbar.item.ItemMine;
import tanks.hotbar.item.ItemShield;
import tanks.minigames.Arcade;
import tanks.minigames.Minigame;
import tanks.network.Client;
import tanks.network.NetworkEventMap;
import tanks.network.SteamNetworkHandler;
import tanks.network.SynchronizedList;
import tanks.network.event.*;
import tanks.network.event.online.*;
import tanks.obstacle.*;
import tanks.registry.*;
import tanks.tank.*;
import tanks.translation.Translation;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

public class Game
{
	public enum Framework {lwjgl, libgdx}
	public static Framework framework;

	public static final double tile_size = 50;

	public static UUID computerID;
	public static final UUID clientID = UUID.randomUUID();

	public static final int absoluteDepthBase = 1000;

	public static ArrayList<Face> horizontalFaces = new ArrayList<>();
	public static ArrayList<Face> verticalFaces = new ArrayList<>();

	public boolean[][] solidGrid;
	public boolean[][] unbreakableGrid;
	public double[][] heightGrid;
	public double[][] groundHeightGrid;

	public double[][] lastHeightGrid;

	public static ArrayList<Movable> movables = new ArrayList<>();
	public static ArrayList<Obstacle> obstacles = new ArrayList<>();
	public static ArrayList<Effect> effects = new ArrayList<>();
	public static ArrayList<Effect> tracks = new ArrayList<>();
	public static ArrayList<Cloud> clouds = new ArrayList<>();
	public static SynchronizedList<Player> players = new SynchronizedList<>();
	public static Player player;

	public static HashSet<Obstacle> prevObstacles = new HashSet<>();

	public static ArrayList<Movable> removeMovables = new ArrayList<>();
	public static ArrayList<Obstacle> removeObstacles = new ArrayList<>();
	public static ArrayList<Effect> removeEffects = new ArrayList<>();
	public static ArrayList<Effect> removeTracks = new ArrayList<>();
	public static ArrayList<Cloud> removeClouds = new ArrayList<>();

	public static ArrayList<Effect> addEffects = new ArrayList<>();
	public static Queue<Effect> recycleEffects = new LinkedList<>();

	public static final SynchronizedList<INetworkEvent> eventsOut = new SynchronizedList<>();
	public static final SynchronizedList<INetworkEvent> eventsIn = new SynchronizedList<>();

	public static Team playerTeam = new Team("ally");
	public static Team enemyTeam = new Team("enemy");

	public static Team playerTeamNoFF = new Team("ally", false);
	public static Team enemyTeamNoFF = new Team("enemy", false);

	/** Use this if you want to spawn a mine not allied with any tank, or such*/
	public static Tank dummyTank;

	public static int currentSizeX = 28;
	public static int currentSizeY = 18;
	public static double bgResMultiplier = 1;

	public static double[][] tilesR = new double[28][18];
	public static double[][] tilesG = new double[28][18];
	public static double[][] tilesB = new double[28][18];
	public static double[][] tilesFlash = new double[28][18];

	public static Obstacle[][] tileDrawables = new Obstacle[28][18];

	public static double[][] tilesDepth = new double[28][18];

	//Remember to change the version in android's build.gradle and ios's robovm.properties
	public static final String version = "Tanks v1.5.1";
	public static final int network_protocol = 51;
	public static boolean debug = false;
	public static boolean traceAllRays = false;
	public static boolean showTankIDs = false;
	public static final boolean cinematic = false;

	public static String lastVersion = "Tanks v0";

	public static int port = 8080;

	public static String lastParty = "";
	public static String lastOnlineServer = "";
	public static boolean showIP = true;

	public static double levelSize = 1;

	public static Tank playerTank;

	public static boolean bulletLocked = false;

	public static boolean vsync = true;
	public static int maxFPS = 0;
	public static int networkRate = 60;

	public static boolean enable3d = true;
	public static boolean enable3dBg = true;
	public static boolean angledView = false;
	public static boolean xrayBullets = true;

	public static boolean followingCam = false;
	public static boolean firstPerson = false;

	public static boolean tankTextures = true;

	public static boolean soundsEnabled = true;
	public static boolean musicEnabled = true;

	public static boolean antialiasing = false;

	public static boolean enableVibrations = true;

	public static boolean enableChatFilter = true;
	public static boolean showSpeedrunTimer = false;
	public static boolean nameInMultiplayer = true;

	public static boolean previewCrusades = true;

	public static boolean deterministicMode = false;
	public static boolean deterministic30Fps = false;
	public static int seed = 0;

	public static boolean invulnerable = false;

	public static boolean warnBeforeClosing = true;

	public static String crashMessage = "Why would this game ever even crash anyway?";
	public static String crashLine = "What, did you think I was a bad programmer? smh";

	public static long crashTime = 0;

	//public static boolean autoMinimapEnabled = true;
	//public static float defaultZoom = 1.5f;

    public static double[] color = new double[3];

    public static Screen screen;
	public static Screen prevScreen;

	public static String ip = "";

	public static boolean fancyTerrain = true;
	public static boolean effectsEnabled = true;
	public static boolean bulletTrails = true;
	public static boolean fancyBulletTrails = true;
	public static boolean glowEnabled = true;

	public static double effectMultiplier = 1;

	public static boolean shadowsEnabled = Game.framework != Framework.libgdx;
	public static int shadowQuality = 10;

	public static boolean autostart = true;
	public static boolean autoReady = false;
	public static double startTime = 400;
	public static boolean fullStats = true;

	public static boolean constrainMouse = false;

	public static double partyStartTime = 400;
	public static boolean disablePartyFriendlyFire = false;

	public static Screen lastOfflineScreen = null;

	public static RegistryTank registryTank = new RegistryTank();
	public static RegistryBullet registryBullet = new RegistryBullet();
	public static RegistryObstacle registryObstacle = new RegistryObstacle();
	public static RegistryItem registryItem = new RegistryItem();
	public static RegistryGenerator registryGenerator = new RegistryGenerator();
	public static RegistryModelTank registryModelTank = new RegistryModelTank();
	public static RegistryMinigame registryMinigame = new RegistryMinigame();

	public static boolean enableExtensions = false;
	public static boolean autoLoadExtensions = true;
	public static ExtensionRegistry extensionRegistry = new ExtensionRegistry();

	public static Extension[] extraExtensions;
	public static int[] extraExtensionOrder;

	public BaseWindow window;

	public BaseFileManager fileManager;

	public static Level currentLevel = null;
	public static String currentLevelString = "";

	public static LevelGenerator lastGenerator = null;

	public static ChatFilter chatFilter = new ChatFilter();

	public ArrayList<InputBindingGroup> inputBindings = new ArrayList<>();
	public InputBindings input;

	public static PrintStream logger = System.err;

	public static String directoryPath = "/.tanks";

	public static final String logPath = directoryPath + "/logfile.txt";
	public static final String extensionRegistryPath = directoryPath + "/extensions.txt";
	public static final String optionsPath = directoryPath + "/options.txt";
	public static final String controlsPath = directoryPath + "/controls.txt";
	public static final String tutorialPath = directoryPath + "/tutorial.txt";
	public static final String uuidPath = directoryPath + "/uuid";
	public static final String levelDir = directoryPath + "/levels";
	//public static final String modLevelDir = directoryPath + "/modlevels/";
	public static final String crusadeDir = directoryPath + "/crusades";
	public static final String savedCrusadePath = directoryPath + "/crusades/progress/";
	public static final String itemDir = directoryPath + "/items";
	public static final String tankDir = directoryPath + "/tanks";
	public static final String extensionDir = directoryPath + "/extensions/";
	public static final String crashesPath = directoryPath + "/crashes/";

	public static final String resourcesPath = directoryPath + "/resources/";
	public static final String languagesPath = resourcesPath + "languages/";

	public static float soundVolume = 1f;
	public static float musicVolume = 0.5f;

	public static boolean isOnlineServer;
	public static boolean connectedToOnline = false;

	public static SteamNetworkHandler steamNetworkHandler;

	public static String homedir;
	public static Game game = new Game();

	// Note: this is not used by the game to determine fullscreen status
	// It is simply a value defined before
	// Refer to Game.game.window.fullscreen for true fullscreen status
	// Value is set before Game.game.window is initialized
	public boolean fullscreen = false;

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
		NetworkEventMap.register(EventLevelEndQuick.class);
		NetworkEventMap.register(EventLevelEnd.class);
		NetworkEventMap.register(EventReturnToLobby.class);
		NetworkEventMap.register(EventBeginCrusade.class);
		NetworkEventMap.register(EventReturnToCrusade.class);
		NetworkEventMap.register(EventShowCrusadeStats.class);
		NetworkEventMap.register(EventLoadCrusadeHotbar.class);
		NetworkEventMap.register(EventSetupHotbar.class);
		NetworkEventMap.register(EventAddShopItem.class);
		NetworkEventMap.register(EventSortShopButtons.class);
		NetworkEventMap.register(EventPurchaseItem.class);
		NetworkEventMap.register(EventSetItem.class);
		NetworkEventMap.register(EventSetItemBarSlot.class);
		NetworkEventMap.register(EventLoadItemBarSlot.class);
		NetworkEventMap.register(EventUpdateCoins.class);
		NetworkEventMap.register(EventPlayerReady.class);
		NetworkEventMap.register(EventPlayerAutoReady.class);
		NetworkEventMap.register(EventPlayerAutoReadyConfirm.class);
		NetworkEventMap.register(EventUpdateReadyPlayers.class);
		NetworkEventMap.register(EventUpdateRemainingLives.class);
		NetworkEventMap.register(EventBeginLevelCountdown.class);
		NetworkEventMap.register(EventTankUpdate.class);
		NetworkEventMap.register(EventTankControllerUpdateS.class);
		NetworkEventMap.register(EventTankControllerUpdateC.class);
		NetworkEventMap.register(EventTankControllerUpdateAmmunition.class);
		NetworkEventMap.register(EventTankControllerAddVelocity.class);
		NetworkEventMap.register(EventTankPlayerCreate.class);
		NetworkEventMap.register(EventTankCreate.class);
		NetworkEventMap.register(EventTankCustomCreate.class);
		NetworkEventMap.register(EventTankSpawn.class);
		NetworkEventMap.register(EventAirdropTank.class);
		NetworkEventMap.register(EventTankUpdateHealth.class);
		NetworkEventMap.register(EventTankRemove.class);
		NetworkEventMap.register(EventShootBullet.class);
		NetworkEventMap.register(EventBulletBounce.class);
		NetworkEventMap.register(EventBulletUpdate.class);
		NetworkEventMap.register(EventBulletDestroyed.class);
		NetworkEventMap.register(EventBulletInstantWaypoint.class);
		NetworkEventMap.register(EventBulletAddAttributeModifier.class);
		NetworkEventMap.register(EventBulletStunEffect.class);
		NetworkEventMap.register(EventBulletUpdateTarget.class);
		NetworkEventMap.register(EventLayMine.class);
		NetworkEventMap.register(EventMineRemove.class);
		NetworkEventMap.register(EventMineChangeTimer.class);
		NetworkEventMap.register(EventExplosion.class);
		NetworkEventMap.register(EventTankTeleport.class);
		NetworkEventMap.register(EventTankUpdateVisibility.class);
		NetworkEventMap.register(EventTankUpdateColor.class);
		NetworkEventMap.register(EventTankTransform.class);
		NetworkEventMap.register(EventTankCharge.class);
		NetworkEventMap.register(EventTankMimicTransform.class);
		NetworkEventMap.register(EventTankMimicLaser.class);
		NetworkEventMap.register(EventTankAddAttributeModifier.class);
		NetworkEventMap.register(EventCreateFreezeEffect.class);
		NetworkEventMap.register(EventObstacleDestroy.class);
		NetworkEventMap.register(EventObstacleHit.class);
		NetworkEventMap.register(EventObstacleShrubberyBurn.class);
		NetworkEventMap.register(EventObstacleSnowMelt.class);
		NetworkEventMap.register(EventObstacleBoostPanelEffect.class);
		NetworkEventMap.register(EventPlaySound.class);
		NetworkEventMap.register(EventSendTankColors.class);
		NetworkEventMap.register(EventShareLevel.class);
		NetworkEventMap.register(EventShareCrusade.class);
		NetworkEventMap.register(EventItemDrop.class);
		NetworkEventMap.register(EventItemPickup.class);
		NetworkEventMap.register(EventItemDropDestroy.class);
		NetworkEventMap.register(EventStatusEffectBegin.class);
		NetworkEventMap.register(EventStatusEffectDeteriorate.class);
		NetworkEventMap.register(EventStatusEffectEnd.class);
		NetworkEventMap.register(EventArcadeHit.class);
		NetworkEventMap.register(EventArcadeRampage.class);
		NetworkEventMap.register(EventArcadeClearMovables.class);
		NetworkEventMap.register(EventArcadeFrenzy.class);
		NetworkEventMap.register(EventArcadeEnd.class);
		NetworkEventMap.register(EventArcadeBonuses.class);

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

	public static void registerObstacle(Class<? extends Obstacle> obstacle, String name)
	{
		if (Game.registryObstacle.getEntry(name).obstacle == ObstacleUnknown.class)
			new RegistryObstacle.ObstacleEntry(Game.registryObstacle, obstacle, name);
	}

	public static void registerTank(Class<? extends Tank> tank, String name, double weight)
	{
		if (Game.registryTank.getEntry(name).tank == TankUnknown.class)
			new RegistryTank.TankEntry(Game.registryTank, tank, name, weight);
	}

	public static void registerTank(Class<? extends Tank> tank, String name, double weight, boolean isBoss)
	{
		if (Game.registryTank.getEntry(name).tank == TankUnknown.class)
			new RegistryTank.TankEntry(Game.registryTank, tank, name, weight, isBoss);
	}

	public static void registerBullet(Class<? extends Bullet> bullet, String name, String icon)
	{
		new RegistryBullet.BulletEntry(Game.registryBullet, bullet, name, icon);
	}

	public static void registerItem(Class<? extends Item> item, String name, String image)
	{
		new RegistryItem.ItemEntry(Game.registryItem, item, name, image);
	}

	public static void registerGenerator(Class<? extends LevelGenerator> generator, String name)
	{
		try
		{
			Game.registryGenerator.generators.put(name, generator.getConstructor().newInstance());
		}
		catch (Exception e)
		{
			Game.exitToCrash(e);
		}
	}

	public static void registerTankModel(String dir)
	{
		Game.registryModelTank.registerFullModel(dir);
	}

	public static void registerTankEmblem(String dir)
	{
		Game.registryModelTank.tankEmblems.add(new RegistryModelTank.TankModelEntry("emblems/" + dir));
	}

	public static void registerMinigame(Class<? extends Minigame> minigame, String name, String desc)
	{
		registryMinigame.minigames.put(name, minigame);
		registryMinigame.minigameDescriptions.put(name, desc);
	}

	public static void initScript()
	{
		player = new Player(clientID, "");
		Game.players.add(player);

		Drawing.initialize();
		Panel.initialize();
		Game.exitToTitle();

		Hotbar.toggle = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - 20, 150, 40, "", () -> Game.player.hotbar.persistent = !Game.player.hotbar.persistent
		);

		steamNetworkHandler = new SteamNetworkHandler();
		steamNetworkHandler.load();

		registerEvents();

		ItemBullet.initializeMaps();

		registerObstacle(Obstacle.class, "normal");
		registerObstacle(ObstacleIndestructible.class, "hard");
		registerObstacle(ObstacleHole.class, "hole");
		registerObstacle(ObstacleBouncy.class, "bouncy");
		registerObstacle(ObstacleNoBounce.class, "nobounce");
		registerObstacle(ObstacleBreakable.class, "breakable");
		registerObstacle(ObstacleExplosive.class, "explosive");
		registerObstacle(ObstacleLight.class, "light");
		registerObstacle(ObstacleShrubbery.class, "shrub");
		registerObstacle(ObstacleMud.class, "mud");
		registerObstacle(ObstacleIce.class, "ice");
		registerObstacle(ObstacleSnow.class, "snow");
		registerObstacle(ObstacleLava.class, "lava");
		registerObstacle(ObstacleBoostPanel.class, "boostpanel");
		registerObstacle(ObstacleTeleporter.class, "teleporter");

		registerTank(TankDummy.class, "dummy", 0);
		registerTank(TankBrown.class, "brown", 1);
		registerTank(TankGray.class, "gray", 1);
		registerTank(TankMint.class, "mint", 1.0 / 2);
		registerTank(TankYellow.class, "yellow", 1.0 / 2);
		registerTank(TankMagenta.class, "magenta", 1.0 / 3);
		registerTank(TankRed.class, "red", 1.0 / 6);
		registerTank(TankGreen.class, "green", 1.0 / 10);
		registerTank(TankPurple.class, "purple", 1.0 / 10);
		registerTank(TankBlue.class, "blue", 1.0 / 4);
		registerTank(TankWhite.class, "white", 1.0 / 10);
		registerTank(TankCyan.class, "cyan", 1.0 / 4);
		registerTank(TankOrange.class, "orange", 1.0 / 4);
		registerTank(TankMaroon.class, "maroon", 1.0 / 4);
		registerTank(TankMustard.class, "mustard", 1.0 / 4);
		registerTank(TankMedic.class, "medic", 1.0 / 4);
		registerTank(TankOrangeRed.class, "orangered", 1.0 / 4);
		registerTank(TankGold.class, "gold", 1.0 / 4);
		registerTank(TankDarkGreen.class, "darkgreen", 1.0 / 10);
		registerTank(TankBlack.class, "black", 1.0 / 10);
		registerTank(TankMimic.class, "mimic", 1.0 / 4);
		registerTank(TankLightBlue.class, "lightblue", 1.0 / 8);
		registerTank(TankPink.class, "pink", 1.0 / 12);
		registerTank(TankMini.class, "mini", 0);
		registerTank(TankSalmon.class, "salmon", 1.0 / 10);
		registerTank(TankLightPink.class, "lightpink", 1.0 / 10);
		registerTank(TankBoss.class, "boss", 1.0 / 40, true);

		registerBullet(Bullet.class, Bullet.bullet_name, "bullet_normal.png");
		registerBullet(BulletFlame2.class, BulletFlame2.bullet_name, "bullet_flame.png");
		registerBullet(BulletLaser.class, BulletLaser.bullet_name, "bullet_laser.png");
		registerBullet(BulletFreeze.class, BulletFreeze.bullet_name, "bullet_freeze.png");
		registerBullet(BulletElectric.class, BulletElectric.bullet_name, "bullet_electric.png");
		registerBullet(BulletHealing.class, BulletHealing.bullet_name, "bullet_healing.png");
		registerBullet(BulletArc.class, BulletArc.bullet_name, "bullet_arc.png");
		registerBullet(BulletExplosive.class, BulletExplosive.bullet_name, "bullet_explosive.png");
		registerBullet(BulletBoost.class, BulletBoost.bullet_name, "bullet_boost.png");
		registerBullet(BulletAir2.class, BulletAir2.bullet_name, "bullet_air.png");
		registerBullet(BulletHoming.class, BulletHoming.bullet_name, "bullet_homing.png");

		registerItem(ItemBullet.class, ItemBullet.item_name, "bullet_normal.png");
		registerItem(ItemMine.class, ItemMine.item_name, "mine.png");
		registerItem(ItemShield.class, ItemShield.item_name, "shield.png");

		registerMinigame(Arcade.class, "Arcade mode", "A gamemode which gets crazier as you---destroy more tanks.------Featuring a score mechanic, unlimited---lives, a time limit, item drops, and---end-game bonuses!");

		TankPlayer.default_bullet = (ItemBullet) Item.parseItem(null, Translation.translate("Basic bullet") + ",bullet_normal.png,1,0,1,100,bullet,normal,trail,3.125,1,1.0,5,20.0,10.0,1.0,false");
		TankPlayer.default_mine = (ItemMine) Item.parseItem(null, Translation.translate("Basic mine") + ",mine.png,1,0,1,100,mine,1000.0,50.0,125.0,2.0,2,50.0,30.0,true");

		homedir = System.getProperty("user.home");

		if (Game.framework == Framework.libgdx)
			homedir = "";

		BaseFile directoryFile = game.fileManager.getFile(homedir + directoryPath);
		if (!directoryFile.exists() && Game.framework != Framework.libgdx)
		{
			directoryFile.mkdirs();
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

		BaseFile extensionRegistryFile = game.fileManager.getFile(homedir + extensionRegistryPath);
		if (!extensionRegistryFile.exists())
		{
			extensionRegistry.initRegistry();
		}

		BaseFile levelsFile = game.fileManager.getFile(homedir + levelDir);
		if (!levelsFile.exists())
		{
			levelsFile.mkdirs();
		}

		BaseFile crusadesFile = game.fileManager.getFile(homedir + crusadeDir);
		if (!crusadesFile.exists())
		{
			crusadesFile.mkdirs();
		}

		BaseFile savedCrusadesProgressFile = game.fileManager.getFile(homedir + savedCrusadePath + "/internal");
		if (!savedCrusadesProgressFile.exists())
		{
			savedCrusadesProgressFile.mkdirs();
		}

		BaseFile itemsFile = game.fileManager.getFile(homedir + itemDir);
		if (!itemsFile.exists())
		{
			itemsFile.mkdirs();
		}

		BaseFile tanksFile = game.fileManager.getFile(homedir + tankDir);
		if (!tanksFile.exists())
		{
			tanksFile.mkdirs();
		}

		BaseFile extensionsFile = game.fileManager.getFile(homedir + extensionDir);
		if (!extensionsFile.exists())
		{
			extensionsFile.mkdirs();
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

		BaseFile optionsFile = Game.game.fileManager.getFile(Game.homedir + Game.optionsPath);
		if (!optionsFile.exists())
		{
			ScreenOptions.initOptions(Game.homedir);
		}

		ScreenOptions.loadOptions(Game.homedir);

		extensionRegistry.loadRegistry();

		if (extraExtensions != null)
		{
			for (int i = 0; i < extraExtensions.length; i++)
			{
				if (extraExtensionOrder != null && i < extraExtensionOrder.length)
					extensionRegistry.extensions.add(extraExtensionOrder[i], extraExtensions[i]);
				else
					extensionRegistry.extensions.add(extraExtensions[i]);
			}
		}

		for (Extension e: extensionRegistry.extensions)
			e.setUp();

		for (RegistryTank.TankEntry e: registryTank.tankEntries)
			e.initialize();

		game.input.file = game.fileManager.getFile(Game.homedir + Game.controlsPath);
		game.input.load();
	}

	public static void postInitScript()
	{
		ArrayList<String> overrideLocations = new ArrayList<>();
		overrideLocations.add(Game.homedir + Game.resourcesPath);
		Game.game.window.setOverrideLocations(overrideLocations, Game.game.fileManager);
	}

	public static void createModels()
	{
		Tank.health_model = Drawing.drawing.createModel();
		Drawing.rotatedRect = Drawing.drawing.createModel();

		TankModels.initialize();

		Drawing.rotatedRect.shapes = new ModelPart.Shape[1];
		Drawing.rotatedRect.shapes[0] = new ModelPart.Quad(
				new ModelPart.Point(-0.5, -0.5, 0),
				new ModelPart.Point(0.5, -0.5, 0),
				new ModelPart.Point(0.5, 0.5, 0),
				new ModelPart.Point(-0.5, 0.5, 0), 1);

		double innerHealthEdge = 0.55;
		double outerHealthEdge = 0.575;
		double lengthMul = 1.2;
		double healthHeight = 0.025;
		Tank.health_model.shapes = new ModelPart.Shape[16];

		Tank.health_model.shapes[0] = new ModelPart.Quad(
				new ModelPart.Point(-outerHealthEdge * lengthMul, -outerHealthEdge, 0),
				new ModelPart.Point(-innerHealthEdge * lengthMul, -outerHealthEdge, 0),
				new ModelPart.Point(-innerHealthEdge * lengthMul, innerHealthEdge, 0),
				new ModelPart.Point(-outerHealthEdge * lengthMul, innerHealthEdge, 0), 0.4);
		Tank.health_model.shapes[1] = new ModelPart.Quad(
				new ModelPart.Point(innerHealthEdge * lengthMul, -innerHealthEdge, 0),
				new ModelPart.Point(outerHealthEdge * lengthMul, -innerHealthEdge, 0),
				new ModelPart.Point(outerHealthEdge * lengthMul, outerHealthEdge, 0),
				new ModelPart.Point(innerHealthEdge * lengthMul, outerHealthEdge, 0), 0.4);
		Tank.health_model.shapes[2] = new ModelPart.Quad(
				new ModelPart.Point(-innerHealthEdge * lengthMul, -outerHealthEdge, 0),
				new ModelPart.Point(outerHealthEdge * lengthMul, -outerHealthEdge, 0),
				new ModelPart.Point(outerHealthEdge * lengthMul, -innerHealthEdge, 0),
				new ModelPart.Point(-innerHealthEdge * lengthMul, -innerHealthEdge, 0), 0.4);
		Tank.health_model.shapes[3] = new ModelPart.Quad(
				new ModelPart.Point(-outerHealthEdge * lengthMul, innerHealthEdge, 0),
				new ModelPart.Point(innerHealthEdge * lengthMul, innerHealthEdge, 0),
				new ModelPart.Point(innerHealthEdge * lengthMul, outerHealthEdge, 0),
				new ModelPart.Point(-outerHealthEdge * lengthMul, outerHealthEdge, 0), 0.4);

		Tank.health_model.shapes[4] = new ModelPart.Quad(
				new ModelPart.Point(-outerHealthEdge * lengthMul, -outerHealthEdge, 0),
				new ModelPart.Point(-outerHealthEdge * lengthMul, -outerHealthEdge, healthHeight),
				new ModelPart.Point(-outerHealthEdge * lengthMul, outerHealthEdge, healthHeight),
				new ModelPart.Point(-outerHealthEdge * lengthMul, outerHealthEdge, 0), 0.6);
		Tank.health_model.shapes[5] = new ModelPart.Quad(
				new ModelPart.Point(outerHealthEdge * lengthMul, -outerHealthEdge, 0),
				new ModelPart.Point(outerHealthEdge * lengthMul, -outerHealthEdge, healthHeight),
				new ModelPart.Point(outerHealthEdge * lengthMul, outerHealthEdge, healthHeight),
				new ModelPart.Point(outerHealthEdge * lengthMul, outerHealthEdge, 0), 0.6);
		Tank.health_model.shapes[6] = new ModelPart.Quad(
				new ModelPart.Point(-outerHealthEdge * lengthMul, -outerHealthEdge, 0),
				new ModelPart.Point(-outerHealthEdge * lengthMul, -outerHealthEdge, healthHeight),
				new ModelPart.Point(outerHealthEdge * lengthMul, -outerHealthEdge, healthHeight),
				new ModelPart.Point(outerHealthEdge * lengthMul, -outerHealthEdge, 0), 0.8);
		Tank.health_model.shapes[7] = new ModelPart.Quad(
				new ModelPart.Point(-outerHealthEdge * lengthMul, outerHealthEdge, 0),
				new ModelPart.Point(-outerHealthEdge * lengthMul, outerHealthEdge, healthHeight),
				new ModelPart.Point(outerHealthEdge * lengthMul, outerHealthEdge, healthHeight),
				new ModelPart.Point(outerHealthEdge * lengthMul, outerHealthEdge, 0), 0.8);

		Tank.health_model.shapes[8] = new ModelPart.Quad(
				new ModelPart.Point(-innerHealthEdge * lengthMul, -innerHealthEdge, 0),
				new ModelPart.Point(-innerHealthEdge * lengthMul, -innerHealthEdge, healthHeight),
				new ModelPart.Point(-innerHealthEdge * lengthMul, innerHealthEdge, healthHeight),
				new ModelPart.Point(-innerHealthEdge * lengthMul, innerHealthEdge, 0), 0.6);
		Tank.health_model.shapes[9] = new ModelPart.Quad(
				new ModelPart.Point(innerHealthEdge * lengthMul, -innerHealthEdge, 0),
				new ModelPart.Point(innerHealthEdge * lengthMul, -innerHealthEdge, healthHeight),
				new ModelPart.Point(innerHealthEdge * lengthMul, innerHealthEdge, healthHeight),
				new ModelPart.Point(innerHealthEdge * lengthMul, innerHealthEdge, 0), 0.6);
		Tank.health_model.shapes[10] = new ModelPart.Quad(
				new ModelPart.Point(-innerHealthEdge * lengthMul, -innerHealthEdge, 0),
				new ModelPart.Point(-innerHealthEdge * lengthMul, -innerHealthEdge, healthHeight),
				new ModelPart.Point(innerHealthEdge * lengthMul, -innerHealthEdge, healthHeight),
				new ModelPart.Point(innerHealthEdge * lengthMul, -innerHealthEdge, 0), 0.8);
		Tank.health_model.shapes[11] = new ModelPart.Quad(
				new ModelPart.Point(-innerHealthEdge * lengthMul, innerHealthEdge, 0),
				new ModelPart.Point(-innerHealthEdge * lengthMul, innerHealthEdge, healthHeight),
				new ModelPart.Point(innerHealthEdge * lengthMul, innerHealthEdge, healthHeight),
				new ModelPart.Point(innerHealthEdge * lengthMul, innerHealthEdge, 0), 0.8);

		Tank.health_model.shapes[12] = new ModelPart.Quad(
				new ModelPart.Point(-outerHealthEdge * lengthMul, -outerHealthEdge, healthHeight),
				new ModelPart.Point(-innerHealthEdge * lengthMul, -outerHealthEdge, healthHeight),
				new ModelPart.Point(-innerHealthEdge * lengthMul, innerHealthEdge, healthHeight),
				new ModelPart.Point(-outerHealthEdge * lengthMul, innerHealthEdge, healthHeight), 1);
		Tank.health_model.shapes[13] = new ModelPart.Quad(
				new ModelPart.Point(innerHealthEdge * lengthMul, -innerHealthEdge, healthHeight),
				new ModelPart.Point(outerHealthEdge * lengthMul, -innerHealthEdge, healthHeight),
				new ModelPart.Point(outerHealthEdge * lengthMul, outerHealthEdge, healthHeight),
				new ModelPart.Point(innerHealthEdge * lengthMul, outerHealthEdge, healthHeight), 1);
		Tank.health_model.shapes[14] = new ModelPart.Quad(
				new ModelPart.Point(-innerHealthEdge * lengthMul, -outerHealthEdge, healthHeight),
				new ModelPart.Point(outerHealthEdge * lengthMul, -outerHealthEdge, healthHeight),
				new ModelPart.Point(outerHealthEdge * lengthMul, -innerHealthEdge, healthHeight),
				new ModelPart.Point(-innerHealthEdge * lengthMul, -innerHealthEdge, healthHeight), 1);
		Tank.health_model.shapes[15] = new ModelPart.Quad(
				new ModelPart.Point(-outerHealthEdge * lengthMul, innerHealthEdge, healthHeight),
				new ModelPart.Point(innerHealthEdge * lengthMul, innerHealthEdge, healthHeight),
				new ModelPart.Point(innerHealthEdge * lengthMul, outerHealthEdge, healthHeight),
				new ModelPart.Point(-outerHealthEdge * lengthMul, outerHealthEdge, healthHeight), 1);
	}

	/**
	 * Adds a tank to the game's movables list and generates/registers a network ID for it.
	 * Use this if you want to add computer-controlled tanks if you are not connected to a server.
	 *
	 * @param tank the tank to add
	 */
	public static void addTank(Tank tank)
	{
		if (tank instanceof TankPlayer || tank instanceof TankPlayerController || tank instanceof TankPlayerRemote || tank instanceof TankRemote)
			Game.exitToCrash(new RuntimeException("Invalid tank added with Game.addTank(" + tank + ")"));

		tank.registerNetworkID();
		Game.movables.add(tank);
		Game.eventsOut.add(new EventTankCreate(tank));
	}

	/**
	 * Adds a tank to the game's movables list and generates/registers a network ID for it after it was spawned by another tank.
	 * Use this if you want to spawn computer-controlled tanks from another tank if you are not connected to a server.
	 *
	 * @param tank the tank to add
	 * @param parent the tank that is spawning the tank
	 */
	public static void spawnTank(Tank tank, Tank parent)
	{
		tank.registerNetworkID();
		Game.movables.add(tank);
		Game.eventsOut.add(new EventTankSpawn(tank, parent));
	}

	/**
	 * Adds a tank to the game's movables list and generates/registers a network ID for it.
	 * Use this if you want to add computer-controlled tanks if you are not connected to a server.
	 */
	public static void addPlayerTank(Player player, double x, double y, double angle, Team t)
	{
		addPlayerTank(player, x, y, angle, t, 0);
	}

	public static void addPlayerTank(Player player, double x, double y, double angle, Team t, double drawAge)
	{
		int id = Tank.nextFreeNetworkID();
		EventTankPlayerCreate e = new EventTankPlayerCreate(player, x, y, angle, t, id, drawAge);
		Game.eventsOut.add(e);
		e.execute();
	}

	public static boolean usernameInvalid(String username)
	{
		if (username.length() > 20)
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

	public static void exitToInterlevel()
	{
		Minigame m = null;
		if (Game.currentLevel instanceof Minigame)
			m = (Minigame) Game.currentLevel;

		silentCleanUp();

		if (m == null)
		{
			if (ScreenPartyHost.isServer)
				screen = new ScreenPartyInterlevel();
			else
				screen = new ScreenInterlevel();
		}
		else
			m.loadInterlevelScreen();
	}

	public static void exitToEditor(String name)
	{
		silentCleanUp();

		ScreenLevelEditor s = new ScreenLevelEditor(name, Game.currentLevel);
		Game.loadLevel(game.fileManager.getFile(Game.homedir + levelDir + "/" + name), s);
		s.paused = true;

		OverlayEditorMenu m = new OverlayEditorMenu(s, s);
		m.showTime = true;
		Game.screen = m;
	}

	public static void exitToCrash(Throwable e)
	{
		System.gc();

		e.printStackTrace();

		if (ScreenPartyHost.isServer && ScreenPartyHost.server != null)
			ScreenPartyHost.server.close("The party has ended because the host crashed");

		if (ScreenPartyLobby.isClient || Game.connectedToOnline)
			Client.handler.ctx.close();

		ScreenPartyLobby.connections.clear();

		ScreenPartyHost.isServer = false;
		ScreenPartyLobby.isClient = false;

		cleanUp();

		Game.crashMessage = e.toString();
		Game.crashLine = "Unable to locate crash line. Please check the crash report for more info.";

		for (StackTraceElement se: e.getStackTrace())
		{
			String s = se.toString();
			if (s.startsWith("tanks") || (s.contains(".") && s.split("\\.")[0].endsWith("window")))
			{
				Game.crashLine = "at " + s;
				break;
			}
		}

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

		try
		{
			if (Crusade.currentCrusade != null && !ScreenPartyHost.isServer && !ScreenPartyLobby.isClient)
			{
				Crusade.currentCrusade.crusadePlayers.get(Game.player).saveCrusade();
			}
		}
		catch (Exception e1)
		{
			e1.printStackTrace(Game.logger);
			e1.printStackTrace();
		}

		Drawing.drawing.playSound("leave.ogg");
	}

	public static void resetTiles()
	{
		Drawing.drawing.setScreenBounds(Game.tile_size * 28, Game.tile_size * 18);

		Game.tilesR = new double[28][18];
		Game.tilesG = new double[28][18];
		Game.tilesB = new double[28][18];
		Game.tilesDepth = new double[28][18];
		Game.tilesFlash = new double[28][18];
		Game.game.heightGrid = new double[28][18];
		Game.game.groundHeightGrid = new double[28][18];
		Game.tileDrawables = new Obstacle[28][18];

		double var = 0;

		if (Game.fancyTerrain)
			var = 20;

		Random tilesRandom = new Random(0);
		for (int i = 0; i < 28; i++)
		{
			for (int j = 0; j < 18; j++)
			{
				Game.tilesR[i][j] = (235 + tilesRandom.nextDouble() * var);
				Game.tilesG[i][j] = (207 + tilesRandom.nextDouble() * var);
				Game.tilesB[i][j] = (166 + tilesRandom.nextDouble() * var);
				Game.tilesDepth[i][j] = tilesRandom.nextDouble() * var / 2;
			}
		}

		Level.currentColorR = 235;
		Level.currentColorG = 207;
		Level.currentColorB = 166;

		Level.currentColorVarR = 20;
		Level.currentColorVarG = 20;
		Level.currentColorVarB = 20;

		Level.currentLightIntensity = 1.0;
		Level.currentShadowIntensity = 0.75;
	}

	public static double sampleGroundHeight(double px, double py)
	{
		int x = (int) (px / Game.tile_size);
		int y = (int) (py / Game.tile_size);

		if (!Game.enable3dBg || !Game.enable3d || x < 0 || x >= Game.currentSizeX || y < 0 || y >= Game.currentSizeY)
			return 0;
		else
			return Game.tilesDepth[x][y] + 0;
	}

	public static double sampleTerrainGroundHeight(double px, double py)
	{
		int x = (int) (px / Game.tile_size);
		int y = (int) (py / Game.tile_size);

		if (px < 0)
			x--;

		if (py < 0)
			y--;

		double r;
		if (!Game.fancyTerrain || !Game.enable3d || x < 0 || x >= Game.currentSizeX || y < 0 || y >= Game.currentSizeY)
			r = 0;
		else
			r = Game.game.groundHeightGrid[x][y];

		return r;
	}

	public static double sampleObstacleHeight(double px, double py)
	{
		int x = (int) (px / Game.tile_size);
		int y = (int) (py / Game.tile_size);

		if (px < 0)
			x--;

		if (py < 0)
			y--;

		double r;
		if (!Game.fancyTerrain || !Game.enable3d || x < 0 || x >= Game.currentSizeX || y < 0 || y >= Game.currentSizeY)
			r = 0;
		else
			r = Game.game.heightGrid[x][y];

		return r;
	}

	public static boolean stringsEqual(String a, String b)
	{
		if (a == null && b == null)
			return true;

		if (a == null || b == null)
			return false;

		return a.equals(b);
	}

	public static void loadTankMusic()
	{
		if (!Game.game.window.soundsEnabled)
			return;

		ArrayList<String> music = Game.game.fileManager.getInternalFileContents("/music/tank/tank_music.txt");

		HashSet<String> loadedMusics = new HashSet<>();
		for (String s: music)
		{
			String[] sections = s.split("=");

			if (sections.length < 2)
				continue;

			String tank = sections[0];
			String[] musics = sections[1].split(",");

			for (String track: musics)
			{
				if (!loadedMusics.contains(track))
				{
					Game.game.window.soundPlayer.loadMusic("/music/" + track);
					loadedMusics.add(track);
				}

				registerTankMusic(tank, track);
			}
		}
	}

	public static void registerTankMusic(String tank, String track)
	{
		if (!Game.registryTank.tankMusics.containsKey(tank))
			Game.registryTank.tankMusics.put(tank, new HashSet<>());

		Game.registryTank.tankMusics.get(tank).add(track);
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
		obstacles.clear();
		tracks.clear();
		movables.clear();
		effects.clear();
		clouds.clear();
		recycleEffects.clear();
		removeEffects.clear();
		removeTracks.clear();
		removeClouds.clear();

		resetNetworkIDs();

		Game.player.hotbar.coins = 0;
		Game.player.hotbar.enabledCoins = false;
		Game.player.hotbar.itemBar = new ItemBar(Game.player);
		Game.player.hotbar.enabledItemBar = false;

		//if (Game.game.window != null)
		//	Game.game.window.setShowCursor(false);
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
		StringBuilder line = new StringBuilder();
		try
		{
			f.startReading();

			while (f.hasNextLine())
			{
				line.append(f.nextLine()).append("\n");
			}

			Level l = new Level(line.substring(0, line.length() - 1));
			l.loadLevel(s);

			f.stopReading();
			return true;
		}
		catch (Exception e)
		{
			Game.screen = new ScreenFailedToLoadLevel(f.path, line.toString(), e, Game.screen);
			return false;
		}
	}

	public static int compareVersions(String v1, String v2)
	{
		String[] a = v1.substring(v1.indexOf(" v") + 2).split("\\.");
		String[] b = v2.substring(v2.indexOf(" v") + 2).split("\\.");

		for (int i = 0; i < Math.max(a.length, b.length); i++)
		{
			String a1 = "0";
			String b1 = "0";

			if (i < a.length)
				a1 = a[i];

			if (i < b.length)
				b1 = b[i];

			StringBuilder na = new StringBuilder("0");
			StringBuilder nb = new StringBuilder("0");
			StringBuilder la = new StringBuilder();
			StringBuilder lb = new StringBuilder();

			for (int j = 0; j < a1.length(); j++)
			{
				if ("0123456789".indexOf(a1.charAt(j)) != -1)
					na.append(a1.charAt(j));
				else
					la.append(a1.charAt(j));
			}

			for (int j = 0; j < b1.length(); j++)
			{
				if ("0123456789".indexOf(b1.charAt(j)) != -1)
					nb.append(b1.charAt(j));
				else
					lb.append(b1.charAt(j));
			}

			int ia = Integer.parseInt(na.toString());
			int ib = Integer.parseInt(nb.toString());

			if (ia != ib)
				return ia - ib;
			else if ((la.toString().length() == 0 || lb.toString().length() == 0) && la.toString().length() + lb.toString().length() > 0)
				return lb.toString().length() - la.toString().length();
			else if (la.toString().length() != lb.toString().length())
				return la.toString().length() - lb.toString().length();
			else if (!la.toString().equals(lb.toString()))
				return la.toString().compareTo(lb.toString());
		}

		return 0;
	}

	public static void loadRandomLevel()
	{
		loadRandomLevel(-1);
	}

	public static void loadRandomLevel(int seed)
	{
		//Level level = new Level("{28,18|4...11-6,11-0...5,17...27-6,16-3...6,0...10-11,11-11...14,16...23-11,16-12...17|3-15-player,7-3-purple2-2,20-14-green,22-3-green-2,8-8.5-brown,19-8.5-mint-2,13.5-5-yellow-1}");
		//Level level = new Level("{28,18|4...11-6,11-0...5,17...27-6,16-3...6,0...10-11,11-11...14,16...23-11,16-12...17|3-15-player,7-3-green-2,20-14-green,22-3-green-2,8-8.5-green,19-8.5-green-2,13.5-5-green-1}");

		//System.out.println(LevelGenerator.generateLevelString());
		//Game.currentLevel = "{28,18|0-17,1-16,2-15,3-14,4-13,5-12,6-11,7-10,10-7,12-5,15-2,16-1,17-0,27-0,26-1,25-2,24-3,23-4,22-5,21-6,20-7,17-10,15-12,12-15,11-16,10-17,27-17,26-16,25-15,24-14,23-13,22-12,21-11,20-10,17-7,15-5,12-2,11-1,10-0,0-0,1-1,3-3,2-2,4-4,5-5,6-6,7-7,10-10,12-12,15-15,16-16,17-17,11-11,16-11,16-6,11-6|0-8-player-0,13-8-magenta-1,14-9-magenta-3,12-10-yellow-0,15-7-yellow-2,13-0-mint-1,14-17-mint-3,27-8-mint-2,27-9-mint-2}";///LevelGenerator.generateLevelString();
		Level level = new Level(LevelGeneratorRandom.generateLevelString(seed));
		//Level level = new Level("{28,18|3...6-3...4,3...4-5...6,10...19-13...14,18...19-4...12|22-14-player,14-10-brown}");
		//Level level = new Level("{28,18|0...27-9,0...27-7|2-8-player,26-8-purple2-2}");
		level.loadLevel();
	}


}
