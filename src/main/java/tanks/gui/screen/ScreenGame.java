package tanks.gui.screen;

import basewindow.InputCodes;
import basewindow.InputPoint;
import basewindow.transformation.RotationAboutPoint;
import basewindow.transformation.Translation;
import tanks.*;
import tanks.network.event.*;
import tanks.generator.LevelGeneratorVersus;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.gui.Minimap;
import tanks.gui.SpeedrunTimer;
import tanks.gui.screen.leveleditor.ScreenLevelEditor;
import tanks.hotbar.ItemBar;
import tanks.hotbar.item.Item;
import tanks.hotbar.item.ItemRemote;
import tanks.minigames.Minigame;
import tanks.network.Client;
import tanks.obstacle.Face;
import tanks.obstacle.ISolidObject;
import tanks.obstacle.Obstacle;
import tanks.tank.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class ScreenGame extends Screen implements IHiddenChatboxScreen, IPartyGameScreen
{
	public boolean playing = false;
	public boolean paused = false;
	public boolean savedRemainingTanks = false;

	public boolean shopScreen = false;
	public boolean npcShopScreen = false;

	public double slant = 0;

	public static boolean finishedQuick = false;
	public static boolean finished = false;
	public static double finishTimer = 100;
	public static double finishTimerMax = 100;
	public double finishQuickTimer = 0;

	public boolean cancelCountdown = false;
	public String name = null;

	public static boolean newItemsNotification = false;
	public static String lastShop = "";
	public ArrayList<Item> shop = new ArrayList<>();
	public boolean screenshotMode = false;

	public Tutorial tutorial;

	public boolean ready = false;
	public double readyNameSpacing = 10;
	public double lastNewReadyName = readyNameSpacing;
	public int readyNamesCount = 0;
	public int prevReadyNames = 0;
	public ArrayList<String> readyPlayers = new ArrayList<>();

	public static boolean versus = false;
	public String title = "";
	public String subtitle = "";

	public long introMusicEnd;
	public long introBattleMusicEnd;

	public RotationAboutPoint slantRotation;
	public Translation slantTranslation;

	public Face[] horizontalFaces;
	public Face[] verticalFaces;

	public Tank spectatingTank = null;

	public double readyPanelCounter = 0;
	public double playCounter = 0;

	public double timeRemaining;
	public double timePassed = 0;
	public static double lastTimePassed = 0;

	public double prevCursorX;
	public double prevCursorY;

	public double shrubberyScale = 0.25;

	public ScreenInfo overlay = null;
	public Minimap minimap = new Minimap();

	public HashSet<String> prevTankMusics = new HashSet<>();
	public HashSet<String> tankMusics = new HashSet<>();
	protected boolean musicStarted = false;

	public boolean zoomPressed = false;
	public boolean zoomScrolled = false;

	public boolean playedIntro = false;

	@SuppressWarnings("unchecked")
	public ArrayList<IDrawable>[] drawables = (ArrayList<IDrawable>[])(new ArrayList[10]);

	Button play = new Button(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY - 50, 350, 40, "Play", () ->
	{
		playing = true;
		Game.playerTank.setBufferCooldown(20);
	}
	);

	Button readyButton = new Button(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY - 50, 350, 40, "Ready", () ->
	{
		if (ScreenPartyLobby.isClient)
			Game.eventsOut.add(new EventPlayerReady());
		else
		{
			ScreenPartyHost.readyPlayers.add(Game.player);
			Game.eventsOut.add(new EventUpdateReadyPlayers(ScreenPartyHost.readyPlayers));

			//synchronized(ScreenPartyHost.server.connections)
			{
				if (ScreenPartyHost.readyPlayers.size() >= ScreenPartyHost.includedPlayers.size())
				{
					Game.eventsOut.add(new EventBeginLevelCountdown());
					cancelCountdown = false;
				}
			}
		}
		ready = true;
	}
	);

	Button startNow = new Button( 200, Drawing.drawing.interfaceSizeY - 50, 350, 40, "Start now", () ->
	{
		if (ScreenPartyHost.isServer)
		{
			for (Player p: Game.players)
			{
				if (!ScreenPartyHost.readyPlayers.contains(p) && ScreenPartyHost.includedPlayers.contains(p.clientID))
					ScreenPartyHost.readyPlayers.add(p);
			}

			Game.eventsOut.add(new EventUpdateReadyPlayers(ScreenPartyHost.readyPlayers));

			Game.eventsOut.add(new EventBeginLevelCountdown());
			cancelCountdown = false;
		}
		ready = true;
	}
	);


	Button enterShop = new Button(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY - 110, 350, 40, "Shop", new Runnable()
	{
		@Override
		public void run()
		{
			if (shopList != null)
			{
				newItemsNotification = false;
				cancelCountdown = true;
				shopScreen = true;
			}
		}
	}, "New items available in shop!"
	);

	Button pause = new Button(0, -1000, 70, 70, "", () ->
	{
		paused = true;
		Game.playerTank.setBufferCooldown(20);
	}
	);

	Button zoom = new Button(0, -1000, 70, 70, "", () ->
	{
		Panel.autoZoom = false;
		Panel.zoomTarget = -1;
		Drawing.drawing.movingCamera = !Drawing.drawing.movingCamera;
	});

	Button zoomAuto = new Button(0, -1000, 70, 70, "", () ->
	{
		Panel.autoZoom = !Panel.autoZoom;
		if (!Panel.autoZoom)
			Panel.zoomTarget = -1;
	});

	Button resume = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace * 1.5, this.objWidth, this.objHeight, "Resume", () ->
	{
		paused = false;
		Game.playerTank.setBufferCooldown(20);
	}
	);

	Button resumeLowerPos = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace, this.objWidth, this.objHeight, "Resume", () ->
	{
		paused = false;
		Game.playerTank.setBufferCooldown(20);
	}
	);

	Button closeMenu = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace * 1.5, this.objWidth, this.objHeight, "Close menu", () ->
	{
		paused = false;
		Game.playerTank.setBufferCooldown(20);
	}
	);

	Button closeMenuLowerPos = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace, this.objWidth, this.objHeight, "Close menu", () ->
	{
		paused = false;
		Game.playerTank.setBufferCooldown(20);
	}
	);

	Button closeMenuClient = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace / 2, this.objWidth, this.objHeight, "Close menu", () ->
	{
		paused = false;
		Game.playerTank.setBufferCooldown(20);
	}
	);

	Button newLevel = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace / 2, this.objWidth, this.objHeight, "Generate new level", () ->
	{
		playing = false;
		paused = false;

		if (ScreenPartyHost.isServer)
		{
			ready = false;
			readyButton.enabled = true;
			cancelCountdown = true;
			ScreenPartyHost.readyPlayers.clear();
			ScreenPartyHost.includedPlayers.clear();
		}

		if (versus)
		{
			Game.cleanUp();
			new Level(LevelGeneratorVersus.generateLevelString()).loadLevel();
		}
		else
		{
			Game.cleanUp();
			Game.loadRandomLevel();
		}

		Game.startTime = Game.currentLevel.startTime;
		Game.screen = new ScreenGame();
	}
	);

	Button restart = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace / 2, this.objWidth, this.objHeight, "Restart level", () ->
	{
		playing = false;
		paused = false;

		if (ScreenPartyHost.isServer)
		{
			ready = false;
			readyButton.enabled = true;
			cancelCountdown = true;
			ScreenPartyHost.readyPlayers.clear();
			ScreenPartyHost.includedPlayers.clear();
		}

		Game.silentCleanUp();

		if (!(Game.currentLevel instanceof Minigame))
		{
			Level level = new Level(Game.currentLevelString);
			level.loadLevel();
		}
		else
		{
			try
			{
				Game.currentLevel = Game.currentLevel.getClass().getConstructor().newInstance();
				Game.currentLevel.loadLevel();
			}
			catch (Exception e)
			{
				Game.exitToCrash(e);
			}
		}

		ScreenGame s = new ScreenGame();
		s.name = name;
		Game.screen = s;
	}
	);

	Button restartLowerPos = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, this.objWidth, this.objHeight, "Restart level", () ->
	{
		restart.function.run();
	}
	);

	Button restartTutorial = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, this.objWidth, this.objHeight, "Restart tutorial", () ->
	{
		Game.silentCleanUp();
		new Tutorial().loadTutorial(ScreenInterlevel.tutorialInitial, Game.game.window.touchscreen);
	}
	);

	Button edit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace / 2, this.objWidth, this.objHeight, "Edit the level", () ->
	{
		Game.cleanUp();
		ScreenLevelEditor s = new ScreenLevelEditor(name, Game.currentLevel);
		Game.loadLevel(Game.game.fileManager.getFile(Game.homedir + Game.levelDir + "/" + name), s);
		Game.screen = s;
	}
	);

	Button quit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace * 1.5, this.objWidth, this.objHeight, "Quit", () ->
	{
		Game.cleanUp();
		Panel.panel.zoomTimer = 0;
		Game.screen = new ScreenPlaySingleplayer();
	}
	);

	Button quitHigherPos = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace, this.objWidth, this.objHeight, "Quit", () ->
	{
		Game.cleanUp();
		Panel.panel.zoomTimer = 0;
		Game.screen = new ScreenPlaySingleplayer();
		ScreenInterlevel.tutorial = false;
	}
	);

	Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace, this.objWidth, this.objHeight, "Back to my levels", () ->
	{
		Game.cleanUp();
		System.gc();
		Panel.panel.zoomTimer = 0;

		if (ScreenInterlevel.fromMinigames)
			Game.screen = new ScreenMinigames();
		else
			Game.screen = new ScreenPlaySavedLevels();

		ScreenInterlevel.fromSavedLevels = false;
		ScreenInterlevel.fromMinigames = false;

		if (ScreenPartyHost.isServer)
		{
			ScreenPartyHost.readyPlayers.clear();
			ScreenPartyHost.includedPlayers.clear();
			Game.eventsOut.add(new EventReturnToLobby());
		}
	}
	);

	Button quitPartyGame = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace * 1.5, this.objWidth, this.objHeight, "Back to party", () ->
	{
		Game.cleanUp();
		System.gc();
		Panel.panel.zoomTimer = 0;
		Game.screen = ScreenPartyHost.activeScreen;
		ScreenPartyHost.readyPlayers.clear();
		ScreenPartyHost.includedPlayers.clear();
		Game.eventsOut.add(new EventReturnToLobby());
		versus = false;
	}
	);

	Button exitParty = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace / 2, this.objWidth, this.objHeight, "Leave party", () ->
	{
		Game.cleanUp();
		System.gc();
		Panel.panel.zoomTimer = 0;
		Drawing.drawing.playSound("leave.ogg");
		ScreenPartyLobby.isClient = false;
		Game.screen = new ScreenJoinParty();

		Client.handler.close();

		ScreenPartyLobby.connections.clear();
	}
	);

	Button quitCrusade = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace, this.objWidth, this.objHeight, "Quit", () ->
	{
		Crusade.currentCrusade.quit();
		Game.cleanUp();
		Panel.panel.zoomTimer = 0;
		Game.screen = new ScreenPlaySingleplayer();
	}
			, "Note! You will lose a life for quitting---in the middle of a level------Your crusade progress will be saved.");

	Button quitCrusadeFinalLife = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace, this.objWidth, this.objHeight, "Quit", () ->
	{
		Crusade.currentCrusade.quit();
		Game.cleanUp();
		Panel.panel.zoomTimer = 0;
		Game.screen = new ScreenPlaySingleplayer();
	}
			, "Note! You will lose a life for quitting---in the middle of a level------Since you do not have any other lives left,---your progress will be lost!");

	Button restartCrusade = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, this.objWidth, this.objHeight, "Restart the level", () ->
	{
		playing = false;
		paused = false;

		if (!finishedQuick)
		{
			for (int i = 0; i < Game.movables.size(); i++)
			{
				if (Game.movables.get(i) instanceof TankPlayer && !Game.movables.get(i).destroy)
					((TankPlayer) Game.movables.get(i)).player.remainingLives--;
				else if (Game.movables.get(i) instanceof TankPlayerRemote && !Game.movables.get(i).destroy)
					((TankPlayerRemote) Game.movables.get(i)).player.remainingLives--;
			}
		}

		if (ScreenPartyHost.isServer)
		{
			ready = false;
			readyButton.enabled = true;
			cancelCountdown = true;
			ScreenPartyHost.readyPlayers.clear();
			ScreenPartyHost.includedPlayers.clear();
		}

		Crusade.currentCrusade.recordPerformance(ScreenGame.lastTimePassed, false);

		Crusade.currentCrusade.retry = true;

		this.saveRemainingTanks();

		Crusade.currentCrusade.saveHotbars();
		Crusade.currentCrusade.crusadePlayers.get(Game.player).saveCrusade();
		Game.silentCleanUp();

		Crusade.currentCrusade.loadLevel();
		ScreenGame s = new ScreenGame(Crusade.currentCrusade.getShop());
		s.name = name;
		Game.screen = s;
	}
			, "Note! You will lose a life for restarting!");

	Button restartCrusadeFinalLife = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, this.objWidth, this.objHeight, "Restart the level",
			"You can't restart the level because---you have only one life left!");

	Button quitCrusadeParty = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace, this.objWidth, this.objHeight, "Back to party", () ->
	{
		Crusade.currentCrusade.retry = true;
		Crusade.currentCrusade.quit();
		Panel.panel.zoomTimer = 0;
		Game.cleanUp();

		Game.screen = ScreenPartyHost.activeScreen;
		ScreenPartyHost.readyPlayers.clear();
		ScreenPartyHost.includedPlayers.clear();
		Game.eventsOut.add(new EventReturnToLobby());
	}
			, "Note! All players will lose a life for---quitting in the middle of a level.");


	Button restartCrusadeParty = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, this.objWidth, this.objHeight, "Restart the level", () ->
	{
		if (!finishedQuick)
		{
			for (int i = 0; i < Game.movables.size(); i++)
			{
				if (Game.movables.get(i) instanceof TankPlayer && !Game.movables.get(i).destroy)
					((TankPlayer) Game.movables.get(i)).player.remainingLives--;
				else if (Game.movables.get(i) instanceof TankPlayerRemote && !Game.movables.get(i).destroy)
					((TankPlayerRemote) Game.movables.get(i)).player.remainingLives--;
			}
		}

		playing = false;
		paused = false;

		ready = false;
		readyButton.enabled = true;
		cancelCountdown = true;

		Crusade.currentCrusade.recordPerformance(ScreenGame.lastTimePassed, false);

		Crusade.currentCrusade.retry = true;

		this.saveRemainingTanks();

		Panel.panel.zoomTimer = 0;
		Game.silentCleanUp();
		System.gc();
		ScreenPartyHost.readyPlayers.clear();
		ScreenPartyHost.includedPlayers.clear();

		Crusade.currentCrusade.loadLevel();
		Game.screen = new ScreenGame(Crusade.currentCrusade.getShop());
	}
			, "Note! All players will lose a life for---restarting in the middle of a level.");

	Button restartCrusadePartyFinalLife = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, this.objWidth, this.objHeight, "Restart the level",
			"You can't restart the level because---nobody has more than one life left!");

	Button quitCrusadePartyFinalLife = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace, this.objWidth, this.objHeight, "Back to party", () ->
	{
		Crusade.currentCrusade.retry = true;
		Crusade.crusadeMode = false;
		Crusade.currentCrusade = null;

		Panel.panel.zoomTimer = 0;
		Game.cleanUp();
		System.gc();
		Game.screen = ScreenPartyHost.activeScreen;
		ScreenPartyHost.readyPlayers.clear();
		ScreenPartyHost.includedPlayers.clear();
		Game.eventsOut.add(new EventReturnToLobby());
	}
			, "Note! All players will lose a life for---quitting in the middle of a level.------Since nobody has any other lives left,---the crusade will end!");


	public static double shopOffset = -25;

	Button exitShop = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 300 + shopOffset, 350, 40, "Exit shop", () ->
	{
		shopScreen = false;
		npcShopScreen = false;
	}

	);

	public ArrayList<Button> shopItemButtons = new ArrayList<>();
	public ButtonList shopList;
	public ButtonList npcShopList = new ButtonList(new ArrayList<>(), 0, 0, (int) shopOffset, -30);

	public ScreenGame()
	{
		this.selfBatch = false;
		this.enableMargins = !Game.followingCam;

		introMusicEnd = Long.parseLong(Game.game.fileManager.getInternalFileContents("/music/ready_music_intro_length.txt").get(0));
		introBattleMusicEnd = Long.parseLong(Game.game.fileManager.getInternalFileContents("/music/battle_intro_length.txt").get(0));

		if (Game.framework == Game.Framework.libgdx)
			introBattleMusicEnd -= 40;

		this.drawDarkness = false;

		Game.clouds.clear();

		if (Game.currentLevel instanceof Minigame && !(Game.currentLevel instanceof Tutorial))
		{
			ScreenInterlevel.fromMinigames = true;
			ScreenInterlevel.fromSavedLevels = false;
			back.setText("Back to minigames");
		}
		else
		{
			ScreenInterlevel.fromMinigames = false;
			ModAPI.menuGroup.clear();
		}

		Game.startTime = Game.currentLevel.startTime;
		ScreenGame.lastTimePassed = 0;

		if (ScreenPartyHost.isServer || ScreenPartyLobby.isClient)
		{
			this.music = "waiting_music.ogg";
			cancelCountdown = true;
		}

		ScreenGame.finishTimer = ScreenGame.finishTimerMax;

		for (int i = 0; i < this.drawables.length; i++)
		{
			this.drawables[i] = new ArrayList<>();
		}

		slantRotation = new RotationAboutPoint(Game.game.window, 0, 0, 0, 0.5, 0.5, -1);
		slantTranslation = new Translation(Game.game.window, 0, 0, 0);

		this.horizontalFaces = new Face[2];
		this.horizontalFaces[0] = new Face(null, 0, 0, Game.currentSizeX * Game.tile_size, 0, true, false, true, true);
		this.horizontalFaces[1] = new Face(null, 0, Game.currentSizeY * Game.tile_size, Game.currentSizeX * Game.tile_size, Game.currentSizeY * Game.tile_size, true, true,true, true);

		this.verticalFaces = new Face[2];
		this.verticalFaces[0] = new Face(null, 0, 0,0, Game.currentSizeY * Game.tile_size, false, false,true, true);
		this.verticalFaces[1] = new Face(null, Game.currentSizeX * Game.tile_size, 0, Game.currentSizeX * Game.tile_size, Game.currentSizeY * Game.tile_size, false, true, true, true);

		if (!Crusade.crusadeMode)
		{
			boolean shop = false;
			boolean startingItems = false;

			if (!Game.currentLevel.shop.isEmpty())
			{
				shop = true;
				this.initShop(Game.currentLevel.shop);
			}

			if (!Game.currentLevel.startingItems.isEmpty())
			{
				startingItems = true;
			}

			for (Player p: Game.players)
			{
				p.hotbar.enabledItemBar = false;
				p.hotbar.enabledCoins = false;

				if (startingItems || shop || (Game.currentLevel instanceof Minigame && ((Minigame) Game.currentLevel).enableItemBar))
				{
					p.hotbar.enabledItemBar = true;
					p.hotbar.itemBar = new ItemBar(p);
				}

				if (startingItems)
				{
					for (Item i: Game.currentLevel.startingItems)
						p.hotbar.itemBar.addItem(i);
				}

				if (shop)
				{
					p.hotbar.enabledCoins = true;
					p.hotbar.coins = Game.currentLevel.startingCoins;
					Game.eventsOut.add(new EventUpdateCoins(p));
				}

				if (p != Game.player)
				{
					Game.eventsOut.add(new EventSetupHotbar(p));
				}
			}
		}

		if (Drawing.drawing.interfaceScaleZoom > 1)
		{
			startNow.sizeX *= 0.7;
			startNow.posX -= 20;
		}

		if (Game.currentLevel != null && Game.currentLevel.timed)
		{
			this.timeRemaining = Game.currentLevel.timer;
		}
	}

	public ScreenGame(String s)
	{
		this();
		this.name = s;
	}

	public ScreenGame(ArrayList<Item> shop)
	{
		this();
		this.initShop(shop);
	}

	public void initShop(ArrayList<Item> shop)
	{
		this.shop = shop;

		for (int i = 0; i < this.shop.size(); i++)
		{
			final int j = i;
			Item item = this.shop.get(j);
			if (item instanceof ItemRemote)
				continue;

			Button b = new Button(0, 0, 350, 40, item.name, () ->
			{
				int pr = shop.get(j).price;
				if (Game.player.hotbar.coins >= pr)
				{
					if (Game.player.hotbar.itemBar.addItem(shop.get(j)))
						Game.player.hotbar.coins -= pr;
				}
			}
			);

			int p = item.price;

			if (p == 0)
				b.setSubtext("Free!");
			else if (p == 1)
				b.setSubtext("1 coin");
			else
				b.setSubtext("%d coins", p);

			this.shopItemButtons.add(b);

			Game.eventsOut.add(new EventAddShopItem(i, item.name, b.rawSubtext, p, item.icon));
		}

		this.initializeShopList();

		Game.eventsOut.add(new EventSortShopButtons());
	}

	public void initializeShopList()
	{
		StringBuilder s = new StringBuilder();
		for (Button b: this.shopItemButtons)
			s.append(b.text);

		if (!lastShop.equals(s.toString()))
			newItemsNotification = true;

		lastShop = s.toString();

		this.shopList = new ButtonList(this.shopItemButtons, 0, 0, (int) shopOffset, -30);
	}

	@Override
	public void setupLights()
	{
		for (Obstacle o: Game.obstacles)
		{
			if (o instanceof IDrawableLightSource && ((IDrawableLightSource) o).lit())
			{
				double[] l = ((IDrawableLightSource) o).getLightInfo();
				l[0] = Drawing.drawing.gameToAbsoluteX(o.posX, 0);
				l[1] = Drawing.drawing.gameToAbsoluteY(o.posY, 0);
				l[2] = (o.startHeight + 25) * Drawing.drawing.scale;
				Panel.panel.lights.add(l);
			}
		}

		for (Movable o: Game.movables)
		{
			if (o instanceof IDrawableLightSource && ((IDrawableLightSource) o).lit())
			{
				double[] l = ((IDrawableLightSource) o).getLightInfo();
				l[0] = Drawing.drawing.gameToAbsoluteX(o.posX, 0);
				l[1] = Drawing.drawing.gameToAbsoluteY(o.posY, 0);
				l[2] = (o.posZ + 25) * Drawing.drawing.scale;
				Panel.panel.lights.add(l);
			}
		}

		for (Effect o: Game.effects)
		{
			if (o instanceof IDrawableLightSource && ((IDrawableLightSource) o).lit())
			{
				double[] l = ((IDrawableLightSource) o).getLightInfo();
				l[0] = Drawing.drawing.gameToAbsoluteX(o.posX, 0);
				l[1] = Drawing.drawing.gameToAbsoluteY(o.posY, 0);
				l[2] = (o.posZ) * Drawing.drawing.scale;
				Panel.panel.lights.add(l);
			}
		}
	}

	@Override
	public void update()
	{
		if (ScreenPartyHost.isServer && this.shop.isEmpty() && Game.autoReady && !this.ready)
			this.readyButton.function.run();

		if (Game.game.input.zoom.isValid())
		{
			zoomScrolled = false;
			zoomPressed = true;
			Game.game.input.zoom.invalidate();
		}

		if (playing)
		{
			if (Game.game.input.zoomIn.isPressed())
			{
				if (Panel.autoZoom)
					Panel.zoomTarget = Panel.panel.zoomTimer;

				Panel.autoZoom = false;
				zoomScrolled = true;
				Drawing.drawing.movingCamera = true;

				if (Panel.zoomTarget == -1)
					Panel.zoomTarget = Panel.panel.zoomTimer;

				Game.game.window.validScrollUp = false;
				Panel.zoomTarget = Math.min(1, Panel.zoomTarget + 0.02 * Panel.frameFrequency * Drawing.drawing.unzoomedScale);
			}

			if (Game.game.input.zoomOut.isPressed())
			{
				if (Panel.autoZoom)
					Panel.zoomTarget = Panel.panel.zoomTimer;

				Panel.autoZoom = false;
				zoomScrolled = true;
				Drawing.drawing.movingCamera = true;

				if (Panel.zoomTarget == -1)
					Panel.zoomTarget = Panel.panel.zoomTimer;

				Game.game.window.validScrollDown = false;
				Panel.zoomTarget = Math.max(0, Panel.zoomTarget - 0.02 * Panel.frameFrequency * Drawing.drawing.unzoomedScale);
			}

			if (Game.playerTank != null && !Game.playerTank.destroy && Panel.autoZoom)
				Panel.zoomTarget = Game.playerTank.getAutoZoom();

			if (spectatingTank != null && !spectatingTank.destroy && Panel.autoZoom)
				Panel.zoomTarget = spectatingTank.getAutoZoom();
		}

		if (Game.game.input.zoom.isPressed() && playing)
		{
			if (Panel.autoZoom)
				Panel.zoomTarget = Panel.panel.zoomTimer;

			Panel.autoZoom = false;

			if (Game.game.window.validScrollUp)
			{
				zoomScrolled = true;
				Drawing.drawing.movingCamera = true;

				if (Panel.zoomTarget == -1)
					Panel.zoomTarget = Panel.panel.zoomTimer;

				Game.game.window.validScrollUp = false;
				Panel.zoomTarget = Math.min(1, Panel.zoomTarget + 0.1 * Drawing.drawing.unzoomedScale);
			}

			if (Game.game.window.validScrollDown)
			{
				zoomScrolled = true;
				Drawing.drawing.movingCamera = true;

				if (Panel.zoomTarget == -1)
					Panel.zoomTarget = Panel.panel.zoomTimer;

				Game.game.window.validScrollDown = false;
				Panel.zoomTarget = Math.max(0, Panel.zoomTarget - 0.1 * Drawing.drawing.unzoomedScale);
			}
		}
		else if (zoomPressed)
		{
			if (!zoomScrolled)
			{
				Drawing.drawing.movingCamera = !Drawing.drawing.movingCamera;
				Panel.zoomTarget = -1;
			}

			zoomPressed = false;
		}

		if (Game.game.input.zoomAuto.isValid() && playing)
		{
			if (Panel.autoZoom)
				Panel.zoomTarget = Panel.panel.zoomTimer;

			Game.game.input.zoomAuto.invalidate();
			Panel.autoZoom = !Panel.autoZoom;
		}

		Game.player.hotbar.update();
		minimap.update();

		for (Obstacle o: Game.obstacles)
		{
			int x = (int) (o.posX / Game.tile_size);
			int y = (int) (o.posY / Game.tile_size);

			if (!(!Game.fancyTerrain || !Game.enable3d || x < 0 || x >= Game.currentSizeX || y < 0 || y >= Game.currentSizeY))
			{
				Game.game.groundHeightGrid[x][y] = Math.max(o.getGroundHeight(), Game.game.groundHeightGrid[x][y]);
			}
		}

		for (int i = 0; i < Game.currentSizeX; i++)
		{
			for (int j = 0; j < Game.currentSizeY; j++)
			{
				if (Game.game.groundHeightGrid[i][j] <= -1000)
					Game.game.groundHeightGrid[i][j] = Game.tilesDepth[i][j];
			}
		}

		String prevMusic = this.music;
		this.music = null;
		this.musicID = null;

		if (this.playCounter >= 0 && this.playing)
		{
			if (!this.playedIntro)
			{
				this.playedIntro = true;
				if (Game.currentLevel instanceof Minigame && ((Minigame) Game.currentLevel).customIntroMusic)
					Drawing.drawing.playSound(((Minigame) Game.currentLevel).introMusic, 1f, true);
				else if (Game.currentLevel != null && Game.currentLevel.timed)
					Drawing.drawing.playSound("battle_timed_intro.ogg", 1f, true);
				else if (Level.isDark())
					Drawing.drawing.playSound("battle_night_intro.ogg", 1f, true);
				else
					Drawing.drawing.playSound("battle_intro.ogg", 1f, true);
			}

			this.playCounter += Panel.frameFrequency;
		}

		if (this.playCounter * 10 >= introBattleMusicEnd)
		{
			Panel.forceRefreshMusic = true;
			this.playCounter = -1;
		}

		if (this.playCounter < 0 && !finishedQuick)
		{
			if (Game.currentLevel != null && Game.currentLevel.timed)
			{
				if (this.paused || Game.playerTank == null || Game.playerTank.destroy)
					this.music = "battle_timed_paused.ogg";
				else
					this.music = "battle_timed.ogg";

				this.musicID = "battle_timed";
			}
			else
			{
				if (this.paused || Game.playerTank == null || Game.playerTank.destroy)
					this.music = "battle_paused.ogg";
				else if (Level.isDark())
					this.music = "battle_night.ogg";
				else
					this.music = "battle.ogg";

				this.musicID = "battle";

				if (Level.isDark())
					this.musicID = "battle_night";


				if (!this.musicStarted)
					this.musicStarted = true;
				else
				{
					this.prevTankMusics.clear();
					this.prevTankMusics.addAll(this.tankMusics);
					this.tankMusics.clear();

					if (!this.paused)
					{
						for (Movable m : Game.movables)
						{
							if (m instanceof Tank && !m.destroy)
							{
								this.tankMusics.addAll(((Tank) m).musicTracks);
							}
						}
					}

					for (String m : this.prevTankMusics)
					{
						if (!this.tankMusics.contains(m))
							Drawing.drawing.removeSyncedMusic(m, 500);
					}

					for (String m : this.tankMusics)
					{
						if (!this.prevTankMusics.contains(m))
							Drawing.drawing.addSyncedMusic(m, Game.musicVolume, true, 500);
					}
				}
			}
		}

		if (finishedQuick)
		{
			this.finishQuickTimer += Panel.frameFrequency;

			this.musicID = null;

			if (!(Game.currentLevel instanceof Minigame && ((Minigame) Game.currentLevel).disableEndMusic))
			{
				if (Panel.win && this.finishQuickTimer >= 75)
					this.music = "waiting_win.ogg";

				if (!Panel.win && this.finishQuickTimer >= 150)
					this.music = "waiting_lose.ogg";
			}
		}

		if (Game.game.input.pause.isValid())
		{
			if (shopScreen || npcShopScreen)
			{
				shopScreen = false;
				npcShopScreen = false;
			}
			else
				this.paused = !this.paused;

			if (Game.followingCam)
				Game.game.window.setCursorPos(Panel.windowWidth / 2, Panel.windowHeight / 2);

			if (this.paused)
			{
				Game.game.window.setCursorLocked(false);
				Game.game.window.setShowCursor(!Panel.showMouseTarget);
			}
			else
			{
				Game.game.window.setCursorLocked(Game.followingCam);

				if (Game.followingCam)
					Game.game.window.setShowCursor(false);
				else
					Game.game.window.setShowCursor(!Panel.showMouseTarget);
			}

			Game.game.input.pause.invalidate();
		}

		if (Game.game.input.hidePause.isValid())
		{
			this.screenshotMode = !this.screenshotMode;
			Game.game.input.hidePause.invalidate();
		}

		if (!finished)
		{
			if (Obstacle.draw_size == 0)
				Drawing.drawing.playSound("level_start.ogg");

			Obstacle.draw_size = Math.min(Game.tile_size, Obstacle.draw_size + Panel.frameFrequency);
		}

		if (npcShopScreen)
		{
			Game.player.hotbar.hidden = false;
			Game.player.hotbar.hideTimer = 100;

			this.exitShop.update();

			this.npcShopList.update();
		}

		if (paused)
		{
			if (!ScreenPartyHost.isServer && !ScreenPartyLobby.isClient)
			{
				if (!playing && Game.startTime >= 0)
					this.updateSingleplayerWaitingMusic();

				this.updateMusic(prevMusic);
			}

			if (!this.screenshotMode)
			{
				if (this.overlay != null)
					this.overlay.update();
				else
				{
					if (ScreenPartyLobby.isClient)
					{
						closeMenuClient.update();
						exitParty.update();
					}
					else if (ScreenPartyHost.isServer)
					{
						if (ScreenInterlevel.fromSavedLevels || ScreenInterlevel.fromMinigames)
						{
							closeMenuLowerPos.update();
							restartLowerPos.update();
							back.update();
						}
						else if (Crusade.crusadeMode)
						{
							closeMenuLowerPos.update();

							if (Crusade.currentCrusade.finalLife())
							{
								restartCrusadePartyFinalLife.update();

								if (finishedQuick && Panel.win)
									quitCrusadeParty.update();
								else
									quitCrusadePartyFinalLife.update();
							}
							else
							{
								restartCrusadeParty.update();
								quitCrusadeParty.update();
							}
						}
						else
						{
							closeMenu.update();
							newLevel.update();
							restart.update();
							quitPartyGame.update();
						}
					}
					else if (ScreenInterlevel.fromSavedLevels || ScreenInterlevel.fromMinigames)
					{
						resumeLowerPos.update();
						restartLowerPos.update();
						back.update();
					}
					else if (ScreenInterlevel.tutorialInitial)
					{
						resumeLowerPos.update();
						restartTutorial.update();
					}
					else if (ScreenInterlevel.tutorial)
					{
						resumeLowerPos.update();
						restartTutorial.update();
						quitHigherPos.update();
					}
					else if (Crusade.crusadeMode)
					{
						if (Crusade.currentCrusade.finalLife())
						{
							restartCrusadeFinalLife.update();
							quitCrusadeFinalLife.update();
						}
						else
						{
							restartCrusade.update();
							quitCrusade.update();
						}

						resumeLowerPos.update();
					}
					else if (name != null)
					{
						resume.update();
						edit.update();
						restart.update();
						quit.update();
					}
					else
					{
						resume.update();
						newLevel.update();
						restart.update();
						quit.update();
					}
				}
			}

			if (!ScreenPartyHost.isServer && !ScreenPartyLobby.isClient)
			{
				return;
			}

			Game.game.window.validPressedKeys.clear();
			Game.game.window.pressedKeys.clear();

			Game.game.window.validPressedButtons.clear();
			Game.game.window.pressedButtons.clear();

			Game.game.window.validScrollUp = false;
			Game.game.window.validScrollDown = false;

			if (Game.game.window.touchscreen)
			{
				TankPlayer.controlStick.activeInput = -1;
				TankPlayer.controlStick.inputIntensity = 0;
				TankPlayer.controlStick.update();

				for (InputPoint p : Game.game.window.touchPoints.values())
				{
					p.valid = false;
					p.tag = "backgroundscreen";
				}
			}
		}
		else if (Game.game.window.touchscreen && !shopScreen)
		{
			boolean vertical = Drawing.drawing.interfaceScale * Drawing.drawing.interfaceSizeY >= Game.game.window.absoluteHeight - Drawing.drawing.statsHeight;
			double vStep = 0;
			double hStep = 0;

			if (vertical)
				vStep = 100;
			else
				hStep = 100;

			pause.posX = (Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2
					+ Drawing.drawing.interfaceSizeX - 50 - Game.game.window.getEdgeBounds() / Drawing.drawing.interfaceScale;
			pause.posY = -((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2 + 50;
			pause.update();

			zoom.posX = pause.posX - hStep;
			zoom.posY = pause.posY + vStep;

			zoomAuto.posX = zoom.posX - hStep;
			zoomAuto.posY = zoom.posY + vStep;

			if (Drawing.drawing.enableMovingCamera)
			{
				zoom.update();

				if (!Panel.autoZoom)
					zoomAuto.update();
			}

			if (playing)
			{
				TankPlayer.controlStick.mobile = TankPlayer.controlStickMobile;
				TankPlayer.controlStick.snap = TankPlayer.controlStickSnap;
				TankPlayer.controlStick.update();
			}
		}

		if (!playing && Game.startTime >= 0)
		{
			if (shopScreen)
			{
				Game.player.hotbar.hidden = false;
				Game.player.hotbar.hideTimer = 100;

				this.exitShop.update();

				this.shopList.update();

				if (ScreenPartyHost.isServer || ScreenPartyLobby.isClient)
				{
					this.music = "waiting_music.ogg";
					this.musicID = null;
				}
				else
				{
					this.music = "ready_music_1.ogg";
					this.musicID = "ready";
				}
			}
			else
			{
				if ((ScreenPartyHost.isServer || ScreenPartyLobby.isClient || Game.autostart) && !cancelCountdown)
					Game.startTime -= Panel.frameFrequency;

				if (!ScreenPartyHost.isServer && !ScreenPartyLobby.isClient)
				{
					play.update();

					if (Game.game.input.play.isValid())
					{
						play.function.run();
						Game.game.input.play.invalidate();
					}

					this.updateSingleplayerWaitingMusic();
				}
				else
				{
					if (this.cancelCountdown)
					{
						readyButton.enabled = !this.ready;

						if (this.ready)
						{
							if (this.readyPanelCounter * 10 >= introMusicEnd)
							{
								this.music = "ready_music_1.ogg";
								this.musicID = "ready";
							}
							else
							{
								if (this.readyPanelCounter == 0)
									Drawing.drawing.playSound("ready_music_intro.ogg", 1f, true);

								this.music = null;
								this.musicID = null;
							}

							this.readyPanelCounter += Panel.frameFrequency;
							readyButton.setText("Waiting... (%d/%d)");
						}
						else
						{
							readyButton.setText("Ready (%d/%d)");
							this.music = "waiting_music.ogg";
							this.musicID = null;
						}

						if (ScreenPartyHost.isServer)
						{
							if (!ScreenPartyHost.includedPlayers.contains(Game.clientID))
							{
								readyButton.setText("Spectating... (%d/%d)");
								readyButton.enabled = false;
							}

							readyButton.setTextArgs(ScreenPartyHost.readyPlayers.size(), ScreenPartyHost.includedPlayers.size());
						}
						else
						{
							if (!ScreenPartyLobby.includedPlayers.contains(Game.clientID))
							{
								readyButton.setText("Spectating... (%d/%d)");
								readyButton.enabled = false;
							}

							readyButton.setTextArgs(ScreenPartyLobby.readyPlayers.size(), ScreenPartyLobby.includedPlayers.size());
						}
					}
					else
					{
						if (this.readyPanelCounter * 10 >= introMusicEnd)
						{
							this.music = "ready_music_2.ogg";
							this.musicID = "ready";
						}
						else
						{
							if (this.readyPanelCounter == 0)
								Drawing.drawing.playSound("ready_music_intro.ogg", 1f, true);

							this.music = null;
							this.musicID = null;
						}

						this.readyPanelCounter += Panel.frameFrequency;
						readyButton.enabled = false;
						readyButton.setText("Starting in %d", ((int)(Game.startTime / 100) + 1));
					}

					readyButton.update();

					if (Game.game.input.play.isValid() && readyButton.enabled)
					{
						readyButton.function.run();
						Game.game.input.play.invalidate();
					}
				}

				if (!this.shopItemButtons.isEmpty() && readyButton.enabled)
					enterShop.update();

				if (ScreenPartyHost.isServer && this.cancelCountdown)
				{
					startNow.update();
				}

				TankPlayer.controlStick.mobile = TankPlayer.controlStickMobile;
				TankPlayer.controlStick.snap = TankPlayer.controlStickSnap;
				TankPlayer.controlStick.update();
			}
		}
		else
		{
			if (Game.currentLevel instanceof Minigame)
				((Minigame) Game.currentLevel).update();

			playing = true;

			if (Game.followingCam)
			{
				Game.playerTank.angle += (Drawing.drawing.getInterfaceMouseX() - prevCursorX) / 100;
				Game.game.window.setCursorLocked(true);
				this.prevCursorX = Drawing.drawing.getInterfaceMouseX();
				this.prevCursorY = Drawing.drawing.getInterfaceMouseX();
			}

			Obstacle.draw_size = Math.min(Game.tile_size, Obstacle.draw_size);
			ArrayList<Team> aliveTeams = new ArrayList<>();
			ArrayList<Team> fullyAliveTeams = new ArrayList<>();

			for (Effect e : Game.effects)
				e.update();

			/*for (Cloud c : Game.clouds)
				c.update();

			for (int i = 0; i < Level.currentCloudCount - Game.clouds.size(); i++)
				Game.clouds.add(new Cloud(Math.random() * (Game.currentSizeX * 50), Math.random() * (Game.currentSizeY * 50)));*/

			Game.horizontalFaces.clear();
			Game.verticalFaces.clear();

			this.horizontalFaces[0].update(0, 0, Game.currentSizeX * Game.tile_size, 0);
			this.horizontalFaces[1].update(0, Game.currentSizeY * Game.tile_size, Game.currentSizeX * Game.tile_size, Game.currentSizeY * Game.tile_size);
			Game.horizontalFaces.add(this.horizontalFaces[0]);
			Game.horizontalFaces.add(this.horizontalFaces[1]);

			this.verticalFaces[0].update(0, 0,0, Game.currentSizeY * Game.tile_size);
			this.verticalFaces[1].update(Game.currentSizeX * Game.tile_size, 0, Game.currentSizeX * Game.tile_size, Game.currentSizeY * Game.tile_size);
			Game.verticalFaces.add(this.verticalFaces[0]);
			Game.verticalFaces.add(this.verticalFaces[1]);

			for (Movable m: Game.movables)
			{
				if (Double.isNaN(m.posX) || Double.isNaN(m.posY))
				{
					throw new RuntimeException("Movable with NaN position: " + m.toString() + " " + m.lastPosX + " " + m.lastPosY);
				}

				if (m instanceof ISolidObject && !(m instanceof Tank && !((Tank) m).targetable))
				{
					Game.horizontalFaces.addAll(Arrays.asList(((ISolidObject) m).getHorizontalFaces()));

					Game.verticalFaces.addAll(Arrays.asList(((ISolidObject) m).getVerticalFaces()));
				}
			}

			for (Obstacle o: Game.obstacles)
			{
				Face[] faces = o.getHorizontalFaces();
				boolean[] valid = o.getValidHorizontalFaces(true);
				for (int i = 0; i < faces.length; i++)
				{
					if (valid[i])
						Game.horizontalFaces.add(faces[i]);
				}

				faces = o.getVerticalFaces();
				valid = o.getValidVerticalFaces(true);
				for (int i = 0; i < faces.length; i++)
				{
					if (valid[i])
						Game.verticalFaces.add(faces[i]);
				}
			}

			try
			{
				Collections.sort(Game.horizontalFaces);
			}
			catch (Exception e)
			{
				System.err.println(Game.horizontalFaces);
				Game.exitToCrash(e);
			}

			try
			{
				Collections.sort(Game.verticalFaces);
			}
			catch (Exception e)
			{
				System.err.println(Game.verticalFaces);
				Game.exitToCrash(e);
			}

			for (int i = 0; i < Game.movables.size(); i++)
			{
				Game.movables.get(i).preUpdate();
			}

			for (int i = 0; i < Game.movables.size(); i++)
			{
				Movable m = Game.movables.get(i);

				if (m.skipNextUpdate)
				{
					m.skipNextUpdate = false;
					continue;
				}

				m.update();

				if (m instanceof Crate)
					m = ((Crate) m).tank;

				if (m instanceof Tank && ((Tank)m).mandatoryKill)
				{
					Team t;

					if (m.team == null)
					{
						if (m instanceof TankPlayer || m instanceof TankPlayerController)
							t = new Team(Game.clientID.toString());
						else if (m instanceof TankPlayerRemote)
							t = new Team(((TankPlayerRemote) m).player.clientID.toString());
						else
							t = new Team("*");
					}
					else
						t = m.team;

					if (!aliveTeams.contains(t))
						aliveTeams.add(t);

					if (!fullyAliveTeams.contains(t) && !m.destroy)
						fullyAliveTeams.add(t);
				}
			}

			for (Obstacle o : Game.obstacles)
			{
				if (o.update)
					o.update();
			}

			for (Effect e : Game.tracks)
				e.update();

			Game.player.hotbar.update();

			if (!finishedQuick)
			{
				this.timePassed += Panel.frameFrequency;
				lastTimePassed = this.timePassed;

				if (Crusade.crusadeMode)
					Crusade.currentCrusade.timePassed += Panel.frameFrequency;
			}

			if (Game.currentLevel != null && Game.currentLevel.timed)
			{
				if (!finishedQuick)
				{
					int seconds = (int) (timeRemaining / 100 + 0.5);
					int secondHalves = (int) (timeRemaining / 50);

					this.timeRemaining -= Panel.frameFrequency;

					int newSeconds = (int) (timeRemaining / 100 + 0.5);
					int newSecondHalves = (int) (timeRemaining / 50);

					if (seconds <= 5)
					{
						if (newSecondHalves < secondHalves)
							Drawing.drawing.playSound("tick.ogg", 2f, 0.5f);
					}
					else if (newSeconds < seconds && seconds <= 10)
						Drawing.drawing.playSound("tick.ogg", 2f, 0.5f);

					if (seconds > newSeconds && (newSeconds == 10 || newSeconds == 30 || newSeconds == 60))
						Drawing.drawing.playSound("timer.ogg");
				}

				if (this.timeRemaining <= 0)
				{
					this.saveRemainingTanks();

					for (int i = 0; i < Game.movables.size(); i++)
					{
						Movable m = Game.movables.get(i);

						m.destroy = true;

						if (m instanceof Tank)
							((Tank) m).health = 0;
					}
				}
			}

			boolean done = fullyAliveTeams.size() <= 1;

			if (Game.currentLevel instanceof Minigame && ((Minigame) Game.currentLevel).customLevelEnd)
				done = ((Minigame) Game.currentLevel).levelEnded();

			if (Game.screen == this && done)
			{
				if (!ScreenGame.finishedQuick)
				{
					Panel.forceRefreshMusic = true;

					if (Game.playerTank != null && (fullyAliveTeams.contains(Game.playerTank.team) || (fullyAliveTeams.size() > 0 && fullyAliveTeams.get(0).name.equals(Game.clientID.toString()))))
					{
						if (Crusade.crusadeMode && !Crusade.currentCrusade.respawnTanks)
						{
							restartCrusade.enabled = false;
							restartCrusadeParty.enabled = false;
						}

						if (!ScreenPartyLobby.isClient)
						{
							Drawing.drawing.playSound("win.ogg", 1.0f, true);
							Panel.win = true;
						}
					}
					else
					{
						if (!ScreenPartyLobby.isClient)
						{
							Drawing.drawing.playSound("lose.ogg", 1.0f, true);

							Panel.win = Game.currentLevel instanceof Minigame && ((Minigame) Game.currentLevel).noLose;
						}
					}

					String s = "**";

					if (fullyAliveTeams.size() > 0)
						s = fullyAliveTeams.get(0).name;

					if (ScreenPartyHost.isServer)
						Game.eventsOut.add(new EventLevelEndQuick(s));
				}

				ScreenGame.finishedQuick = true;
				TankPlayer.shootStickHidden = false;
			}

			if (aliveTeams.size() <= 1 && done)
			{
				ScreenGame.finished = true;
				Game.bulletLocked = true;

				if (ScreenGame.finishTimer > 0)
				{
					ScreenGame.finishTimer -= Panel.frameFrequency;
					if (ScreenGame.finishTimer < 0)
						ScreenGame.finishTimer = 0;
				}
				else
				{
					boolean noMovables = true;

					for (int m = 0; m < Game.movables.size(); m++)
					{
						Movable mo = Game.movables.get(m);
						if (mo instanceof tanks.bullet.Bullet || mo instanceof Mine)
						{
							noMovables = false;
							mo.destroy = true;
						}
					}

					if (Game.effects.size() <= 0 && noMovables)
					{
						if (Game.followingCam)
							Game.game.window.setCursorPos(Panel.windowWidth / 2, Panel.windowHeight / 2);

						if (Obstacle.draw_size == Game.tile_size)
							Drawing.drawing.playSound("level_end.ogg");

						Obstacle.draw_size = Math.max(0, Obstacle.draw_size - Panel.frameFrequency);

						this.saveRemainingTanks();

						for (Movable m: Game.movables)
							m.destroy = true;

						if (Obstacle.draw_size <= 0)
						{
							Panel.levelPassed = false;

							for (int i = 0; i < Game.players.size(); i++)
							{
								if (Game.players.get(i) != null && Game.players.get(i).tank != null && aliveTeams.contains(Game.players.get(i).tank.team) || (aliveTeams.size() > 0 && aliveTeams.get(0).name.equals(Game.players.get(i).clientID.toString())))
								{
									Panel.levelPassed = true;

									if (Crusade.crusadeMode)
										Panel.winlose = "Battle cleared!";

									break;
								}
							}

							if (Game.playerTank != null)
							{
								if (aliveTeams.contains(Game.playerTank.team) || (aliveTeams.size() > 0 && aliveTeams.get(0).name.equals(Game.clientID.toString())))
								{
									if (Crusade.crusadeMode)
										Panel.winlose = "Battle cleared!";
									else
										Panel.winlose = "Victory!";

									if (!ScreenPartyLobby.isClient)
										Panel.win = true;
								}
								else
								{
									if (Crusade.crusadeMode)
										Panel.winlose = "Battle failed!";
									else
										Panel.winlose = "You were destroyed!";

									if (!ScreenPartyLobby.isClient)
										Panel.win = (Game.currentLevel instanceof Minigame && ((Minigame) Game.currentLevel).noLose);
								}

								if (Game.currentLevel instanceof Minigame)
									((Minigame) Game.currentLevel).onLevelEnd(Panel.win);
							}
							else if (!ScreenPartyLobby.isClient)
								Panel.win = false;

							if (Crusade.crusadeMode)
								Crusade.currentCrusade.saveHotbars();

							if (ScreenPartyHost.isServer)
							{
								Game.silentCleanUp();

								String s = "**";

								if (aliveTeams.size() > 0)
									s = aliveTeams.get(0).name;

								ScreenPartyHost.readyPlayers.clear();
								Game.eventsOut.add(new EventLevelEnd(s));

								if (Crusade.crusadeMode)
								{
									Crusade.currentCrusade.levelFinished(Panel.levelPassed);

									EventReturnToCrusade e = new EventReturnToCrusade(Crusade.currentCrusade);

									e.execute();
									Game.eventsOut.add(e);

									if (Crusade.currentCrusade.win || Crusade.currentCrusade.lose)
										Game.eventsOut.add(new EventShowCrusadeStats());

									for (int i = 0; i < Game.players.size(); i++)
									{
										Game.eventsOut.add(new EventUpdateRemainingLives(Game.players.get(i)));
									}
								}
								else
									Game.exitToInterlevel();

								System.gc();
							}
							else if (Game.currentLevel != null && !Game.currentLevel.remote)
							{
								if (name != null)
									Game.exitToEditor(name);
								else
									Game.exitToInterlevel();
							}
						}
					}
				}
			}
			else
				Game.bulletLocked = false;
		}

		if (spectatingTank != null && spectatingTank.destroy)
			spectatingTank = null;

		if (!Game.game.window.touchscreen)
		{
			double mx = Drawing.drawing.getInterfaceMouseX();
			double my = Drawing.drawing.getInterfaceMouseY();

			boolean handled = checkMouse(mx, my, Game.game.window.validPressedButtons.contains(InputCodes.MOUSE_BUTTON_1));

			if (handled)
				Game.game.window.validPressedButtons.remove((Integer) InputCodes.MOUSE_BUTTON_1);
		}
		else
		{
			for (int i: Game.game.window.touchPoints.keySet())
			{
				InputPoint p = Game.game.window.touchPoints.get(i);

				if (p.tag.equals(""))
				{
					double mx = Drawing.drawing.toGameCoordsX(Drawing.drawing.getInterfacePointerX(p.x));
					double my = Drawing.drawing.getInterfacePointerY(p.y);

					boolean handled = checkMouse(mx, my, p.valid);

					if (handled)
						p.tag = "spectate";
				}
			}
		}

		if (playing && !paused && !finishedQuick)
		{
			this.shrubberyScale = Math.min(this.shrubberyScale + Panel.frameFrequency / 200, 1);
		}

		if (finishedQuick)
		{
			this.shrubberyScale = Math.max(this.shrubberyScale - Panel.frameFrequency / 200, 0.25);
		}

		this.updateMusic(prevMusic);

		Game.movables.removeAll(Game.removeMovables);
		Game.clouds.removeAll(Game.removeClouds);
		ModAPI.menuGroup.removeAll(ModAPI.removeMenus);

		for (Obstacle o: Game.removeObstacles)
		{
			o.removed = true;
			Drawing.drawing.terrainRenderer2.remove(o);

			int x = (int) (o.posX / Game.tile_size);
			int y = (int) (o.posY / Game.tile_size);

			if (x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY && o.bulletCollision)
			{
				Game.game.solidGrid[x][y] = false;
				Game.game.unbreakableGrid[x][y] = false;
			}

			Game.obstacles.remove(o);
		}

		for (Effect e: Game.removeEffects)
		{
			if (e.state == Effect.State.removed)
			{
				e.state = Effect.State.recycle;
				Game.effects.remove(e);
				Game.recycleEffects.add(e);
			}
		}

		Game.effects.addAll(Game.addEffects);
		Game.addEffects.clear();

		for (Effect e: Game.removeTracks)
		{
			if (e.state == Effect.State.removed)
			{
				e.state = Effect.State.recycle;
				Game.tracks.remove(e);
				Game.recycleEffects.add(e);
			}
		}

		Game.removeMovables.clear();
		Game.removeObstacles.clear();
		Game.removeEffects.clear();
		Game.removeTracks.clear();
		Game.removeClouds.clear();
		ModAPI.removeMenus.clear();

		if (this.tutorial != null)
		{
			this.tutorial.update();
		}
	}

	public void updateMusic(String prevMusic)
	{
		if (this.music == null && prevMusic != null)
			Panel.forceRefreshMusic = true;

		if (this.music != null && prevMusic == null)
			Panel.forceRefreshMusic = true;

		if (this.music != null && !this.music.equals(prevMusic))
			Panel.forceRefreshMusic = true;
	}

	public void updateSingleplayerWaitingMusic()
	{
		if (ScreenInterlevel.tutorialInitial)
			return;

		if (this.readyPanelCounter * 10 >= introMusicEnd)
		{
			this.music = "ready_music_2.ogg";
			this.musicID = "ready";

			if (this.paused)
				this.music = "ready_music_1.ogg";
		}
		else
		{
			if (this.readyPanelCounter == 0)
				Drawing.drawing.playSound("ready_music_intro.ogg", 1f, true);

			this.music = null;
			this.musicID = null;
		}

		this.readyPanelCounter += Panel.frameFrequency;
	}

	public boolean checkMouse(double mx, double my, boolean valid)
	{
		if (!valid)
			return false;

		double x = Drawing.drawing.toGameCoordsX(mx);
		double y = Drawing.drawing.toGameCoordsY(my);

		if ((Game.playerTank == null || Game.playerTank.destroy) && (spectatingTank == null || !Drawing.drawing.movingCamera) && Panel.panel.zoomTimer <= 0)
		{
			if (Game.game.window.validPressedButtons.contains(InputCodes.MOUSE_BUTTON_1))
			{
				for (Movable m: Game.movables)
				{
					if (m instanceof Tank && !m.destroy && !((Tank) m).hidden)
					{
						if (x >= m.posX - ((Tank) m).size && x <= m.posX + ((Tank) m).size &&
								y >= m.posY - ((Tank) m).size && y <= m.posY + ((Tank) m).size)
						{
							this.spectatingTank = (Tank) m;
							Panel.panel.pastPlayerX.clear();
							Panel.panel.pastPlayerY.clear();
							Panel.panel.pastPlayerTime.clear();
							Drawing.drawing.movingCamera = true;
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	public void setPerspective()
	{
		if (Game.angledView && Game.framework == Game.Framework.lwjgl)
		{
			if (!Game.game.window.drawingShadow)
			{
				if (this.playing && (!this.paused || ScreenPartyHost.isServer || ScreenPartyLobby.isClient) && !ScreenGame.finished)
					slant = Math.min(1, slant + 0.01 * Panel.frameFrequency);
				else if (ScreenGame.finished)
					slant = Math.max(0, slant - 0.01 * Panel.frameFrequency);
			}

			this.slantRotation.pitch = this.slant * -Math.PI / 16;
			this.slantTranslation.y = -this.slant * 0.05;

			if (!Game.followingCam)
			{
				Game.game.window.transformations.add(this.slantTranslation);
				Game.game.window.transformations.add(this.slantRotation);
			}

			Game.game.window.loadPerspective();
		}

		if (Game.followingCam && Game.framework == Game.Framework.lwjgl && !Game.game.window.drawingShadow)
		{
			double frac = Panel.panel.zoomTimer;

			if (!Game.firstPerson)
			{
				Game.game.window.transformations.add(new RotationAboutPoint(Game.game.window, 0, 0, frac * ((Game.playerTank.angle + Math.PI * 3 / 2) % (Math.PI * 2) - Math.PI), 0, -Drawing.drawing.statsHeight / Game.game.window.absoluteHeight / 2, 0));
				Game.game.window.transformations.add(new Translation(Game.game.window, 0, 0.1 * frac, 0));
				Game.game.window.transformations.add(new RotationAboutPoint(Game.game.window, 0, -Math.PI * 0.35 * frac, 0, 0, 0, -1));
				Game.game.window.transformations.add(new Translation(Game.game.window, 0, 0, 0.5 * frac));
			}
			else
			{
				Game.game.window.transformations.add(new RotationAboutPoint(Game.game.window, 0, 0, frac * ((Game.playerTank.angle + Math.PI * 3 / 2) % (Math.PI * 2) - Math.PI), 0, -Drawing.drawing.statsHeight / Game.game.window.absoluteHeight / 2, 0));
				Game.game.window.transformations.add(new Translation(Game.game.window, 0, 0.1 * frac, 0));
				Game.game.window.transformations.add(new RotationAboutPoint(Game.game.window, 0, -Math.PI * 0.5 * frac, 0, 0, 0, -1));
				Game.game.window.transformations.add(new Translation(Game.game.window, 0, 0.0575 * frac, 0.9 * frac));
			}

			Game.game.window.loadPerspective();
		}
	}

	@Override
	public void draw()
	{
		long start = System.nanoTime();
		this.showDefaultMouse = !(((!this.paused && !this.npcShopScreen) && this.playing && Game.angledView || Game.firstPerson));

		if (Game.enable3d)
			for (Obstacle o: Game.obstacles)
			{
				if (o.replaceTiles)
					o.postOverride();

				int x = (int) (o.posX / Game.tile_size);
				int y = (int) (o.posY / Game.tile_size);

				if (!(!Game.fancyTerrain || !Game.enable3d || x < 0 || x >= Game.currentSizeX || y < 0 || y >= Game.currentSizeY))
				{
					Game.game.heightGrid[x][y] = Math.max(o.getTileHeight(), Game.game.heightGrid[x][y]);
					Game.game.groundHeightGrid[x][y] = Math.max(o.getGroundHeight(), Game.game.groundHeightGrid[x][y]);
				}
			}

		if (Game.game.lastHeightGrid == null || Game.game.heightGrid.length != Game.game.lastHeightGrid.length || Game.game.heightGrid[0].length != Game.game.lastHeightGrid[0].length)
		{
			Game.game.lastHeightGrid = new double[Game.game.heightGrid.length][Game.game.heightGrid[0].length];
		}

		for (int i = 0; i < Game.game.heightGrid.length; i++)
		{
			for (int j = 0; j < Game.game.heightGrid[i].length; j++)
			{
				Game.game.lastHeightGrid[i][j] = Game.game.heightGrid[i][j];
			}
		}

		long t1 = System.nanoTime();

		this.setPerspective();

		Drawing.drawing.setColor(174, 92, 16);

		double mul = 1;
		if (Game.angledView)
			mul = 2;

		Drawing.drawing.fillShadedInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2,
				mul * Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale, mul * Game.game.window.absoluteHeight / Drawing.drawing.interfaceScale);

		if (Game.enable3d && (Obstacle.draw_size <= 0 || Obstacle.draw_size >= Game.tile_size))
			Drawing.drawing.beginTerrainRenderers();

		this.drawDefaultBackground();

		Drawing drawing = Drawing.drawing;

		drawables[0].addAll(Game.tracks);

		for (Movable m: Game.movables)
		{
			drawables[m.drawLevel].add(m);

			if (m.showName)
				drawables[m.nameTag.drawLevel].add(m.nameTag);
		}

		long t1a = System.nanoTime();
		if (Game.enable3d && /*(Obstacle.draw_size <= 0 || Obstacle.draw_size >= Game.tile_size) && */Game.game.window.shapeRenderer.supportsBatching)
		{
			for (int i = 0; i < drawables.length; i++)
			{
				for (Obstacle o : Game.obstacles)
				{
					if (o.drawLevel == i && !o.batchDraw)
						drawables[i].add(o);
				}
			}
		}
		else
		{
			for (Obstacle o : Game.obstacles)
				drawables[o.drawLevel].add(o);
		}
		long t1b = System.nanoTime();

		for (Effect e: Game.effects)
		{
			drawables[e.drawLayer].add(e);
		}

		for (Cloud c: Game.clouds)
		{
			drawables[c.drawLevel].add(c);
		}

		if (Game.enable3d && (Obstacle.draw_size <= 0 || Obstacle.draw_size >= Game.tile_size))
			Drawing.drawing.drawTerrainRenderers();

		if (Game.game.window.touchscreen)
		{
			drawables[9].add(TankPlayer.controlStick);

			if (TankPlayer.shootStickEnabled && !TankPlayer.shootStickHidden)
				drawables[9].add(TankPlayer.shootStick);
		}
		long t2 = System.nanoTime();

		for (int i = 0; i < this.drawables.length; i++)
		{
			if (i == 5 && Game.enable3d)
			{
				double frac = Obstacle.draw_size / Game.tile_size;
				Drawing.drawing.setColor(174 * frac + Level.currentColorR * (1 - frac), 92 * frac + Level.currentColorG * (1 - frac), 16 * frac + Level.currentColorB * (1 - frac));
				Drawing.drawing.fillForcedBox(drawing.sizeX / 2, -Game.tile_size / 2, 0, drawing.sizeX + Game.tile_size * 2, Game.tile_size, Obstacle.draw_size, (byte) 0);
				Drawing.drawing.fillForcedBox(drawing.sizeX / 2, Drawing.drawing.sizeY + Game.tile_size / 2, 0, drawing.sizeX + Game.tile_size * 2, Game.tile_size, Obstacle.draw_size, (byte) 0);
				Drawing.drawing.fillForcedBox(-Game.tile_size / 2, drawing.sizeY / 2, 0, Game.tile_size, drawing.sizeY, Obstacle.draw_size, (byte) 0);
				Drawing.drawing.fillForcedBox(drawing.sizeX + Game.tile_size / 2, drawing.sizeY / 2, 0, Game.tile_size, drawing.sizeY, Obstacle.draw_size, (byte) 0);
			}

			if (i == 9 && this.tutorial != null)
			{
				this.tutorial.drawTutorial();
			}

			for (IDrawable d: this.drawables[i])
			{
				if (d != null)
					d.draw();
			}

			if (Game.glowEnabled)
			{
				for (IDrawable d: this.drawables[i])
				{
					if (d instanceof IDrawableWithGlow && ((IDrawableWithGlow) d).isGlowEnabled())
						((IDrawableWithGlow) d).drawGlow();
				}
			}

			if (i == 9 && (Game.playerTank instanceof ILocalPlayerTank && ((ILocalPlayerTank) Game.playerTank).showTouchCircle()))
			{
				Drawing.drawing.setColor(255, 127, 0, 63);
				Drawing.drawing.fillInterfaceOval(Drawing.drawing.toInterfaceCoordsX(Game.playerTank.posX),
						Drawing.drawing.toInterfaceCoordsY(Game.playerTank.posY),
						((ILocalPlayerTank) Game.playerTank).getTouchCircleSize(), ((ILocalPlayerTank) Game.playerTank).getTouchCircleSize());
			}

			if (i == 9 && (Game.playerTank instanceof ILocalPlayerTank && ((ILocalPlayerTank) Game.playerTank).getDrawRange() >= 0) && !Game.game.window.drawingShadow)
			{
				if (Level.isDark())
					Drawing.drawing.setColor(255, 255, 255, 50);
				else
					Drawing.drawing.setColor(0, 0, 0, 50);

				Mine.drawRange2D(Game.playerTank.posX, Game.playerTank.posY,
						((ILocalPlayerTank) Game.playerTank).getDrawRange());

				((ILocalPlayerTank) Game.playerTank).setDrawRange(-1);
			}

			if (i == 9 && Game.playerTank != null && !Game.playerTank.destroy
					&& Game.screen instanceof ScreenGame && !((ScreenGame) Game.screen).playing && Game.movables.contains(Game.playerTank))
			{
				double s = Game.startTime;

				if (cancelCountdown)
					s = 400;

				Game.playerTank.drawSpinny(s);
			}

			if (i == 9 && Game.playerTank != null && !Game.playerTank.destroy
					&& Game.screen instanceof ScreenGame && ((ScreenGame) Game.screen).playing && Game.movables.contains(Game.playerTank) && Game.playerTank.invulnerabilityTimer > 0)
			{
				Game.playerTank.drawSpinny(Game.playerTank.invulnerabilityTimer);
			}

			drawables[i].clear();
		}

		long t3 = System.nanoTime();

		/*Drawing.drawing.setColor(255, 0, 0);
		for (Face f: Game.horizontalFaces)
		{
			drawing.fillRect(0.5 * (f.endX + f.startX), f.startY, f.endX - f.startX, 5);
		}

		Drawing.drawing.setColor(0, 255, 0);
		for (Face f: Game.verticalFaces)
		{
			drawing.fillRect(f.startX, 0.5 * (f.endY + f.startY), 5, f.endY - f.startY);
		}

		Drawing.drawing.setColor(0, 0, 0, 127);*/

		if (Panel.darkness > 0)
		{
			Drawing.drawing.setColor(0, 0, 0, Math.max(0, Panel.darkness));
			Game.game.window.shapeRenderer.fillRect(0, 0, Game.game.window.absoluteWidth, Game.game.window.absoluteHeight - Drawing.drawing.statsHeight);
		}

		if (Game.game.window.touchscreen && TankPlayer.shootStickEnabled)
		{
			double size = TankPlayer.mineButton.sizeX * Obstacle.draw_size / Game.tile_size;
			Drawing.drawing.setColor(255, 127, 0, 64);
			Drawing.drawing.fillInterfaceOval(TankPlayer.mineButton.posX, TankPlayer.mineButton.posY, size, size);

			Drawing.drawing.setColor(255, 255, 0, 64);
			Drawing.drawing.fillInterfaceOval(TankPlayer.mineButton.posX, TankPlayer.mineButton.posY, size * 0.8, size * 0.8);

			//Drawing.drawing.setColor(255, 255, 255, 64);
			//Drawing.drawing.drawInterfaceImage("/mine.png", TankPlayer.mineButton.posX, TankPlayer.mineButton.posY, TankPlayer.mineButton.sizeX, TankPlayer.mineButton.sizeY);
		}

		if (!this.showDefaultMouse)
			Panel.panel.drawMouseTarget(true);

		if (Game.framework == Game.Framework.lwjgl)
		{
			Game.game.window.transformations.clear();
			Game.game.window.loadPerspective();
		}

		if (npcShopScreen)
		{
			Drawing.drawing.setColor(127, 178, 228, 64);
			Game.game.window.shapeRenderer.fillRect(0, 0, Game.game.window.absoluteWidth + 1, Game.game.window.absoluteHeight + 1);

			Drawing.drawing.setInterfaceFontSize(this.titleSize);

			if (Level.isDark())
				Drawing.drawing.setColor(255, 255, 255);
			else
				Drawing.drawing.setColor(0, 0, 0);

			Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - 210 + shopOffset, "Shop");

			this.exitShop.draw();

			this.npcShopList.draw();
		}

		if (!playing)
		{
			if (Crusade.crusadeMode)
			{
				if (Level.isDark())
					Drawing.drawing.setColor(255, 255, 255, 127);
				else
					Drawing.drawing.setColor(0, 0, 0, 127);

				Drawing.drawing.setInterfaceFontSize(100);
				Drawing.drawing.displayInterfaceText(this.centerX, this.centerY, "Battle %d", (Crusade.currentCrusade.currentLevel + 1));

				if (Crusade.currentCrusade != null && Crusade.currentCrusade.showNames)
				{
					Drawing.drawing.setInterfaceFontSize(50);
					Drawing.drawing.drawInterfaceText(this.centerX, this.centerY + 75, Crusade.currentCrusade.levels.get(Crusade.currentCrusade.currentLevel).levelName.replace("_", " "));
				}
			}

			if (!title.equals(""))
			{
				if (Level.isDark())
					Drawing.drawing.setColor(255, 255, 255, 127);
				else
					Drawing.drawing.setColor(0, 0, 0, 127);

				Drawing.drawing.setInterfaceFontSize(100);
				Drawing.drawing.drawInterfaceText(this.centerX, this.centerY, title);
			}

			if (!subtitle.equals(""))
			{
				if (Level.isDark())
					Drawing.drawing.setColor(255, 255, 255, 127);
				else
					Drawing.drawing.setColor(0, 0, 0, 127);

				Drawing.drawing.setInterfaceFontSize(50);
				Drawing.drawing.drawInterfaceText(this.centerX, this.centerY + 75, subtitle);
			}

			if (shopScreen)
			{
				Drawing.drawing.setColor(127, 178, 228, 64);
				Game.game.window.shapeRenderer.fillRect(0, 0, Game.game.window.absoluteWidth + 1, Game.game.window.absoluteHeight + 1);

				Drawing.drawing.setInterfaceFontSize(this.titleSize);

				if (Level.isDark())
					Drawing.drawing.setColor(255, 255, 255);
				else
					Drawing.drawing.setColor(0, 0, 0);

				Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 210 + shopOffset, "Shop");

				this.exitShop.draw();

				this.shopList.draw();

				for (int i = Math.min((this.shopList.page + 1) * this.shopList.rows * this.shopList.columns, shopItemButtons.size()) - 1; i >= this.shopList.page * this.shopList.rows * this.shopList.columns; i--)
				{
					Button b = this.shopItemButtons.get(i);
					b.draw();
					Drawing.drawing.setColor(255, 255, 255);
					Drawing.drawing.drawInterfaceImage(this.shop.get(i).icon, b.posX - 135, b.posY, 40, 40);
				}
			}
			else
			{
				if (!ScreenPartyHost.isServer && !ScreenPartyLobby.isClient)
					play.draw();
				else
				{
					if (ScreenPartyHost.isServer)
					{
						readyPlayers.clear();

						for (Player p : ScreenPartyHost.readyPlayers)
							readyPlayers.add(p.username);
					}
					else
						readyPlayers = ScreenPartyLobby.readyPlayers;

					double s = Game.startTime;

					if (cancelCountdown)
						s = 400;

					double extraWidth = (Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2;
					double height = (Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale;

					Drawing.drawing.setColor(0, 0, 0, Math.max(0, 127 * Math.min(1, (readyPanelCounter * 10) / 200) * Math.min(s / 25, 1)));
					Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX + extraWidth / 2, Drawing.drawing.interfaceSizeY / 2, extraWidth, height);
					Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX - Math.min(readyPanelCounter * 10, 200), Drawing.drawing.interfaceSizeY / 2,
							Math.min(readyPanelCounter * 20, 400), height);

					double c = readyPanelCounter - 35;

					if (c > 0)
					{
						Drawing.drawing.setColor(255, 255, 255, Math.max(Math.min(s / 25, 1) * 255, 0));
						Drawing.drawing.setInterfaceFontSize(this.titleSize);

						Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX - 200, 50, "Ready players:");
					}

					int includedPlayers = 0;

					if (ScreenPartyHost.isServer)
						includedPlayers = ScreenPartyHost.includedPlayers.size();
					else if (ScreenPartyLobby.isClient)
						includedPlayers = ScreenPartyLobby.includedPlayers.size();

					double spacing = readyNameSpacing;

					if (includedPlayers > 15)
						spacing = spacing / 2;

					if (includedPlayers > 30)
						spacing = spacing / 2;

					if (includedPlayers > 60)
						spacing = spacing / 2;


					if (readyPlayers.size() > readyNamesCount && c > lastNewReadyName + spacing)
					{
						lastNewReadyName = c;
						readyNamesCount++;
					}

					int slots = (int) ((Drawing.drawing.interfaceSizeY - 200) / 40) - 1;
					int base = 0;

					if (readyNamesCount >= includedPlayers)
						slots++;

					if (readyNamesCount > slots)
						base = readyNamesCount - slots;

					for (int i = 0; i < readyPlayers.size(); i++)
					{
						if (i < readyNamesCount)
						{
							Drawing.drawing.setColor(255, 255, 255, Math.max(Math.min(s / 25, 1) * 255, 0));
							Drawing.drawing.setInterfaceFontSize(this.textSize);

							if (i >= base)
							{
								if (Game.enableChatFilter)
									Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX - 200, 40 * (i - base) + 100, Game.chatFilter.filterChat(readyPlayers.get(i)));
								else
									Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX - 200, 40 * (i - base) + 100, readyPlayers.get(i));
							}
						}
					}

					if (c >= 0)
					{
						Drawing.drawing.setColor(255, 255, 255, Math.min(s / 25, 1) * 127);
						Drawing.drawing.setInterfaceFontSize(this.textSize);

						for (int i = readyNamesCount; i < Math.min(includedPlayers, slots); i++)
						{
							Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX - 200, 40 * i + 100, "Waiting...");
						}

						int extra = includedPlayers - Math.max(readyNamesCount, slots);
						if (extra > 0)
						{
							if (extra == 1)
								Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX - 200, 40 * slots + 100, "Waiting...");
							else
								Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX - 200, 40 * slots + 100, "%d waiting...", extra);
						}
					}

					if (prevReadyNames != readyNamesCount)
						Drawing.drawing.playSound("bullet_explode.ogg", 1.5f);

					prevReadyNames = readyNamesCount;

					readyButton.draw();
				}

				if (!this.shopItemButtons.isEmpty() && this.readyButton.enabled)
				{
					enterShop.enableHover = newItemsNotification;
					enterShop.fullInfo = true;
					enterShop.draw();

					if (newItemsNotification)
					{
						Button.drawGlow(enterShop.posX - enterShop.sizeX / 2 + enterShop.sizeY / 2, enterShop.posY + 2.5 + 1, enterShop.sizeY * 3 / 4, enterShop.sizeY * 3 / 4, 0.6, 0, 0, 0, 100, false);
						drawing.setInterfaceFontSize(this.textSize / Drawing.drawing.interfaceScaleZoom);
						drawing.setColor(255, 127, 0);
						drawing.fillInterfaceOval(enterShop.posX - enterShop.sizeX / 2 + enterShop.sizeY / 2, enterShop.posY, enterShop.sizeY * 3 / 4, enterShop.sizeY * 3 / 4);
						drawing.setColor(255, 255, 255);
						Drawing.drawing.drawInterfaceText(enterShop.posX - enterShop.sizeX / 2 + enterShop.sizeY / 2 + 0.5, enterShop.posY, "!");
					}
				}

				if (ScreenPartyHost.isServer && this.cancelCountdown)
					startNow.draw();

				if ((ScreenPartyHost.isServer || ScreenPartyLobby.isClient || Game.autostart) && !cancelCountdown)
				{
					Drawing.drawing.setColor(127, 127, 127);
					Drawing.drawing.fillInterfaceRect(play.posX, play.posY + play.sizeY / 2 - 5, play.sizeX * 32 / 35, 3);
					Drawing.drawing.setColor(255, 127, 0);
					Drawing.drawing.fillInterfaceProgressRect(play.posX, play.posY + play.sizeY / 2 - 5, play.sizeX * 32 / 35, 3, Math.max(Game.startTime / Game.currentLevel.startTime, 0));

					if (Game.glowEnabled)
					{
						Drawing.drawing.fillInterfaceGlow(play.posX + ((Game.startTime / Game.currentLevel.startTime - 0.5) * (play.sizeX * 32 / 35)), play.posY + play.sizeY / 2 - 5, 20, 20);
					}
				}
			}
		}

		if (!paused && Game.game.window.touchscreen && !shopScreen)
		{
			pause.draw();
			Drawing.drawing.drawInterfaceImage("icons/pause.png", pause.posX, pause.posY, 40, 40);

			if (Drawing.drawing.enableMovingCamera)
			{
				zoom.draw();

				if (!Panel.autoZoom)
					zoomAuto.draw();

				if (Drawing.drawing.movingCamera)
					Drawing.drawing.drawInterfaceImage("icons/zoom_out.png", zoom.posX, zoom.posY, 40, 40);
				else
					Drawing.drawing.drawInterfaceImage("icons/zoom_in.png", zoom.posX, zoom.posY, 40, 40);

				if (!Panel.autoZoom)
					Drawing.drawing.drawInterfaceImage("icons/zoom_auto.png", zoomAuto.posX, zoomAuto.posY, 40, 40);
			}
		}

		if (!(paused && screenshotMode))
		{
			Game.player.hotbar.draw();

			if (Game.showSpeedrunTimer && !(paused && screenshotMode) && !(Game.currentLevel instanceof Minigame && ((Minigame) Game.currentLevel).hideSpeedrunTimer))
				SpeedrunTimer.draw();

			minimap.draw();
		}

		if (Game.deterministicMode && !ScreenPartyLobby.isClient)
		{
			if (Level.isDark() || (Game.screen instanceof IDarkScreen && Panel.win && Game.effectsEnabled))
				Drawing.drawing.setColor(255, 255, 255, 127);
			else
				Drawing.drawing.setColor(0, 0, 0, 127);

			double posX = Drawing.drawing.interfaceSizeX + (Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2 - Game.game.window.getEdgeBounds() / Drawing.drawing.interfaceScale - 50;
			double posY = -((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2 + 50;

			if (Drawing.drawing.interfaceScaleZoom > 1)
				posX -= 50;

			Drawing.drawing.setInterfaceFontSize(24);

			if (Game.deterministic30Fps)
				Drawing.drawing.drawInterfaceText(posX, posY, "Deterministic mode (30 FPS)", true);
			else
				Drawing.drawing.drawInterfaceText(posX, posY, "Deterministic mode (60 FPS)", true);
		}

		if (Game.currentLevel instanceof Minigame)
			((Minigame) Game.currentLevel).draw();

		if (paused && !screenshotMode)
		{
			if (ScreenGame.finishedQuick)
			{
				quitCrusade.enableHover = false;
				quitCrusadeParty.enableHover = false;

				quitCrusadeFinalLife.enableHover = false;
				quitCrusadePartyFinalLife.enableHover = false;

				restartCrusade.enableHover = false;
				restartCrusadeParty.enableHover = false;

				restartCrusadeFinalLife.enableHover = false;
				restartCrusadePartyFinalLife.enableHover = false;
			}

			Drawing.drawing.setColor(127, 178, 228, 64);
			Game.game.window.shapeRenderer.fillRect(0, 0, Game.game.window.absoluteWidth + 1, Game.game.window.absoluteHeight + 1);

			if (ScreenPartyLobby.isClient)
			{
				closeMenuClient.draw();
				exitParty.draw();
			}
			else if (ScreenPartyHost.isServer)
			{
				if (ScreenInterlevel.fromSavedLevels || ScreenInterlevel.fromMinigames)
				{
					closeMenuLowerPos.draw();
					restartLowerPos.draw();
					back.draw();
				}
				else if (Crusade.crusadeMode)
				{
					closeMenuLowerPos.draw();

					if (Crusade.currentCrusade.finalLife())
					{
						if (Panel.win && finishedQuick)
							quitCrusadeParty.draw();
						else
							quitCrusadePartyFinalLife.draw();

						restartCrusadePartyFinalLife.draw();
					}
					else
					{
						quitCrusadeParty.draw();
						restartCrusadeParty.draw();
					}
				}
				else
				{
					closeMenu.draw();
					newLevel.draw();
					restart.draw();
					quitPartyGame.draw();
				}
			}
			else if (ScreenInterlevel.fromSavedLevels || ScreenInterlevel.fromMinigames)
			{
				resumeLowerPos.draw();
				restartLowerPos.draw();
				back.draw();
			}
			else if (ScreenInterlevel.tutorialInitial)
			{
				resumeLowerPos.draw();
				restartTutorial.draw();
			}
			else if (ScreenInterlevel.tutorial)
			{
				resumeLowerPos.draw();
				restartTutorial.draw();
				quitHigherPos.draw();
			}
			else if (Crusade.crusadeMode)
			{
				if (Crusade.currentCrusade.finalLife())
				{
					quitCrusadeFinalLife.draw();
					restartCrusadeFinalLife.draw();
				}
				else
				{
					quitCrusade.draw();
					restartCrusade.draw();
				}

				resumeLowerPos.draw();
			}
			else if (name != null)
			{
				resume.draw();
				edit.draw();
				restart.draw();
				quit.draw();
			}
			else
			{
				resume.draw();
				newLevel.draw();
				restart.draw();
				quit.draw();
			}

			Drawing.drawing.setInterfaceFontSize(this.titleSize);
			Drawing.drawing.setColor(0, 0, 0);

			if (Level.isDark())
				Drawing.drawing.setColor(255, 255, 255);

			if (!ScreenPartyHost.isServer && !ScreenPartyLobby.isClient)
				Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, "Game paused");
			else
				Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, "Game menu");
		}

		if (this.overlay != null)
			this.overlay.draw();

		Drawing.drawing.setInterfaceFontSize(this.textSize);
		long t4 = System.nanoTime();
		//System.out.println((t1 - start) + " " + (t2 - t1) + " " + (t3 - t2) + " " + (t4 - t3) + " / " + (t1b - t1a));
	}

	public void saveRemainingTanks()
	{
		if (!savedRemainingTanks && Crusade.crusadeMode && Crusade.currentCrusade != null)
		{
			Crusade.currentCrusade.livingTankIDs.clear();
			for (Movable m : Game.movables)
			{
				if (m instanceof Tank && !m.destroy && ((Tank) m).crusadeID >= 0)
					Crusade.currentCrusade.livingTankIDs.add(((Tank) m).crusadeID);
			}
		}
		savedRemainingTanks = true;
	}

	@Override
	public double getOffsetX()
	{
		return Drawing.drawing.getPlayerOffsetX();
	}

	@Override
	public double getOffsetY()
	{
		return Drawing.drawing.getPlayerOffsetY();
	}

	@Override
	public double getScale()
	{
		return Drawing.drawing.scale * (1 - Panel.panel.zoomTimer) + Drawing.drawing.interfaceScale * Panel.panel.zoomTimer;
	}

}
