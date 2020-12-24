package tanks.gui.screen;

import basewindow.InputCodes;
import basewindow.InputPoint;
import basewindow.transformation.RotationAboutPoint;
import basewindow.transformation.Translation;
import tanks.*;
import tanks.event.*;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.hotbar.ItemBar;
import tanks.hotbar.item.Item;
import tanks.hotbar.item.ItemRemote;
import tanks.network.Client;
import tanks.obstacle.*;
import tanks.obstacle.Obstacle;
import tanks.tank.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class ScreenGame extends Screen implements IHiddenChatboxScreen, IPartyGameScreen
{
	public boolean playing = false;
	public boolean paused = false;

	public boolean shopScreen = false;

	public double slant = 0;

	public static boolean finishedQuick = false;
	public static boolean finished = false;
	public static double finishTimer = 100;
	public static double finishTimerMax = 100;

	public boolean cancelCountdown = false;
	public String name = null;

	public ArrayList<Item> shop = new ArrayList<Item>();
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

	public long introMusicEnd;
	public long introBattleMusicEnd;

	public RotationAboutPoint slantRotation;
	public Translation slantTranslation;

	public Face[] horizontalFaces;
	public Face[] verticalFaces;

	public Tank spectatingTank = null;

	public double readyPanelCounter = 0;
	public double playCounter = 0;

	public double prevCursorX;
	public double prevCursorY;

	public ScreenInfo overlay = null;

	@SuppressWarnings("unchecked")
	public ArrayList<IDrawable>[] drawables = (ArrayList<IDrawable>[])(new ArrayList[10]);

	Button play = new Button(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY - 50, 350, 40, "Play", new Runnable()
	{
		@Override
		public void run()
		{
			playing = true;
			Game.playerTank.cooldown = 20;
		}
	}
	);

	Button readyButton = new Button(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY - 50, 350, 40, "Ready", new Runnable()
	{
		@Override
		public void run()
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
	}
	);

	Button startNow = new Button( 200, Drawing.drawing.interfaceSizeY - 50, 350, 40, "Start now", new Runnable()
	{
		@Override
		public void run()
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
	}
	);


	Button enterShop = new Button(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY - 110, 350, 40, "Shop", new Runnable()
	{
		@Override
		public void run()
		{
			if (shopList != null)
			{
				cancelCountdown = true;
				shopScreen = true;
			}
		}
	}
	);

	Button pause = new Button(0, -1000, 70, 70, "", new Runnable()
	{
		@Override
		public void run()
		{
			paused = true;
			Game.playerTank.cooldown = 20;
		}
	}
	);

	Button zoom = new Button(0, -1000, 70, 70, "", new Runnable()
	{
		@Override
		public void run()
		{
			Drawing.drawing.movingCamera = !Drawing.drawing.movingCamera;
		}
	}
	);

	Button resume = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace * 1.5, this.objWidth, this.objHeight, "Continue playing", new Runnable()
	{
		@Override
		public void run()
		{
			paused = false;
			Game.playerTank.cooldown = 20;
		}
	}
	);

	Button resumeLowerPos = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace, this.objWidth, this.objHeight, "Continue playing", new Runnable()
	{
		@Override
		public void run()
		{
			paused = false;
			Game.playerTank.cooldown = 20;
		}
	}
	);

	Button closeMenu = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace * 1.5, this.objWidth, this.objHeight, "Close menu", new Runnable()
	{
		@Override
		public void run()
		{
			paused = false;
			Game.playerTank.cooldown = 20;
		}
	}
	);

	Button closeMenuLowerPos = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace, this.objWidth, this.objHeight, "Close menu", new Runnable()
	{
		@Override
		public void run()
		{
			paused = false;
			Game.playerTank.cooldown = 20;
		}
	}
	);

	Button closeMenuClient = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace / 2, this.objWidth, this.objHeight, "Close menu", new Runnable()
	{
		@Override
		public void run()
		{
			paused = false;
			Game.playerTank.cooldown = 20;
		}
	}
	);

	Button newLevel = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace / 2, this.objWidth, this.objHeight, "Generate a new level", new Runnable()
	{
		@Override
		public void run()
		{
			playing = false;
			Game.startTime = 400;
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
				Game.reset();

			Game.screen = new ScreenGame();
		}
	}
	);

	Button restart = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace / 2, this.objWidth, this.objHeight, "Restart this level", new Runnable()
	{
		@Override
		public void run()
		{
			playing = false;
			Game.startTime = 400;
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

			Level level = new Level(Game.currentLevelString);
			level.loadLevel();
			ScreenGame s = new ScreenGame();
			s.name = name;
			Game.screen = s;
		}
	}
	);

	Button restartLowerPos = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, this.objWidth, this.objHeight, "Restart this level", new Runnable()
	{
		@Override
		public void run()
		{
			playing = false;
			Game.startTime = 400;
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

			Level level = new Level(Game.currentLevelString);
			level.loadLevel();
			ScreenGame s = new ScreenGame();
			s.name = name;
			Game.screen = s;
		}
	}
	);

	Button restartTutorial = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, this.objWidth, this.objHeight, "Restart this level", new Runnable()
	{
		@Override
		public void run()
		{
			Game.silentCleanUp();
			new Tutorial().loadTutorial(ScreenInterlevel.tutorialInitial, Game.game.window.touchscreen);
		}
	}
	);

	Button edit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace / 2, this.objWidth, this.objHeight, "Edit the level", new Runnable()
	{
		@Override
		public void run()
		{
			Game.cleanUp();
			ScreenLevelBuilder s = new ScreenLevelBuilder(name);
			Game.loadLevel(Game.game.fileManager.getFile(Game.homedir + Game.levelDir + "/" + name), s);
			Game.screen = s;
		}
	}
	);

	Button quit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace * 1.5, this.objWidth, this.objHeight, "Quit to title", new Runnable()
	{
		@Override
		public void run()
		{
			Game.exitToTitle();
		}
	}
	);

	Button quitHigherPos = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace, this.objWidth, this.objHeight, "Quit to title", new Runnable()
	{
		@Override
		public void run()
		{
			Game.exitToTitle();
			ScreenInterlevel.tutorial = false;
		}
	}
	);

	Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace, this.objWidth, this.objHeight, "Back to my levels", new Runnable()
	{
		@Override
		public void run()
		{
			Game.cleanUp();
			System.gc();
			Panel.panel.zoomTimer = 0;
			Game.screen = new ScreenPlaySavedLevels();
			ScreenInterlevel.fromSavedLevels = false;

			if (ScreenPartyHost.isServer)
			{
				ScreenPartyHost.readyPlayers.clear();
				ScreenPartyHost.includedPlayers.clear();
				Game.eventsOut.add(new EventReturnToLobby());
			}
		}
	}
	);

	Button quitPartyGame = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace * 1.5, this.objWidth, this.objHeight, "Back to party", new Runnable()
	{
		@Override
		public void run()
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
	}
	);

	Button exitParty = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace / 2, this.objWidth, this.objHeight, "Leave party", new Runnable()
	{
		@Override
		public void run()
		{
			Game.cleanUp();
			System.gc();
			Panel.panel.zoomTimer = 0;
			Drawing.drawing.playSound("leave.ogg");
			ScreenPartyLobby.isClient = false;
			Game.screen = new ScreenJoinParty();
			Client.handler.ctx.close();
			ScreenPartyLobby.connections.clear();
		}
	}
	);

	Button quitCrusade = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace, this.objWidth, this.objHeight, "Quit to title", new Runnable()
	{
		@Override
		public void run()
		{
			Crusade.crusadeMode = false;
			Game.player.saveCrusade();
			Crusade.currentCrusade = null;

			for (int i = 0; i < Game.movables.size(); i++)
			{
				if (Game.movables.get(i) instanceof TankPlayer && !Game.movables.get(i).destroy)
					((TankPlayer) Game.movables.get(i)).player.remainingLives--;
				else if (Game.movables.get(i) instanceof TankPlayerRemote && !Game.movables.get(i).destroy)
					((TankPlayerRemote) Game.movables.get(i)).player.remainingLives--;
			}

			Game.exitToTitle();
		}
	}
			, "Note! You will lose a life for quitting---in the middle of a level------Your crusade progress will be saved.");

	Button quitCrusadeFinalLife = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace, this.objWidth, this.objHeight, "Quit to title", new Runnable()
	{
		@Override
		public void run()
		{
			Crusade.crusadeMode = false;
			Game.player.saveCrusade();
			Crusade.currentCrusade = null;
			Game.exitToTitle();
		}
	}
			, "Note! You will lose a life for quitting---in the middle of a level------Since you do not have any other lives left,---your progress will be lost!");

	Button restartCrusade = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, this.objWidth, this.objHeight, "Restart the level", new Runnable()
	{
		@Override
		public void run()
		{
			playing = false;
			Game.startTime = 400;
			paused = false;

			if (ScreenPartyHost.isServer)
			{
				ready = false;
				readyButton.enabled = true;
				cancelCountdown = true;
				ScreenPartyHost.readyPlayers.clear();
				ScreenPartyHost.includedPlayers.clear();
			}

			Crusade.currentCrusade.saveHotbars();
			Game.player.saveCrusade();
			Game.silentCleanUp();

			Crusade.currentCrusade.loadLevel();
			ScreenGame s = new ScreenGame(Crusade.currentCrusade.getShop());
			s.name = name;
			Game.screen = s;
		}
	}
			, "Note! You will lose a life for restarting!");

	Button restartCrusadeFinalLife = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, this.objWidth, this.objHeight, "Restart the level",
			"You can't restart the level because---you have only one life left!");

	Button quitCrusadeParty = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace, this.objWidth, this.objHeight, "Back to party", new Runnable()
	{
		@Override
		public void run()
		{
			Crusade.crusadeMode = false;

			for (int i = 0; i < Game.movables.size(); i++)
			{
				if (Game.movables.get(i) instanceof TankPlayer && !Game.movables.get(i).destroy)
					((TankPlayer) Game.movables.get(i)).player.remainingLives--;
				else if (Game.movables.get(i) instanceof TankPlayerRemote && !Game.movables.get(i).destroy)
					((TankPlayerRemote) Game.movables.get(i)).player.remainingLives--;
			}

			Panel.panel.zoomTimer = 0;
			Game.cleanUp();
			System.gc();
			Game.screen = ScreenPartyHost.activeScreen;
			ScreenPartyHost.readyPlayers.clear();
			ScreenPartyHost.includedPlayers.clear();
			Game.eventsOut.add(new EventReturnToLobby());
		}
	}
			, "Note! All players will lose a life for---quitting in the middle of a level.");


	Button restartCrusadeParty = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, this.objWidth, this.objHeight, "Restart the level", new Runnable()
	{
		@Override
		public void run()
		{
			for (int i = 0; i < Game.movables.size(); i++)
			{
				if (Game.movables.get(i) instanceof TankPlayer && !Game.movables.get(i).destroy)
					((TankPlayer) Game.movables.get(i)).player.remainingLives--;
				else if (Game.movables.get(i) instanceof TankPlayerRemote && !Game.movables.get(i).destroy)
					((TankPlayerRemote) Game.movables.get(i)).player.remainingLives--;
			}

			playing = false;
			Game.startTime = 400;
			paused = false;

			ready = false;
			readyButton.enabled = true;
			cancelCountdown = true;

			Panel.panel.zoomTimer = 0;
			Game.silentCleanUp();
			System.gc();
			ScreenPartyHost.readyPlayers.clear();
			ScreenPartyHost.includedPlayers.clear();

			Crusade.currentCrusade.loadLevel();
			Game.screen = new ScreenGame(Crusade.currentCrusade.getShop());
		}
	}
			, "Note! All players will lose a life for---restarting in the middle of a level.");

	Button restartCrusadePartyFinalLife = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, this.objWidth, this.objHeight, "Restart the level",
			"You can't restart the level because---nobody has more than one life left!");

	Button quitCrusadePartyFinalLife = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace, this.objWidth, this.objHeight, "Back to party", new Runnable()
	{
		@Override
		public void run()
		{
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
	}
			, "Note! All players will lose a life for---quitting in the middle of a level.------Since nobody has any other lives left,---the crusade will end!");


	public static double shopOffset = -25;

	Button exitShop = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 300 + shopOffset, 350, 40, "Exit shop", new Runnable()
	{
		@Override
		public void run()
		{
			shopScreen = false;
		}
	}
	);

	public ArrayList<Button> shopItemButtons = new ArrayList<>();
	public ButtonList shopList;

	public ScreenGame()
	{
		introMusicEnd = Long.parseLong(Game.game.fileManager.getInternalFileContents("/music/ready_music_intro_length.txt").get(0));
		introBattleMusicEnd = Long.parseLong(Game.game.fileManager.getInternalFileContents("/music/battle_intro_length.txt").get(0));

		if (Game.framework == Game.Framework.libgdx)
			introBattleMusicEnd -= 40;

		this.drawDarkness = false;

		Game.startTime = 400;

		if (ScreenPartyHost.isServer || ScreenPartyLobby.isClient)
		{
			this.music = "waiting_music.ogg";
			cancelCountdown = true;
		}

		ScreenGame.finishTimer = ScreenGame.finishTimerMax;

		for (int i = 0; i < this.drawables.length; i++)
		{
			this.drawables[i] = new ArrayList<IDrawable>();
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

				if (startingItems || shop)
					p.hotbar.itemBar = new ItemBar(p);

				if (startingItems)
				{
					p.hotbar.enabledItemBar = true;

					for (Item i: Game.currentLevel.startingItems)
						p.hotbar.itemBar.addItem(i);
				}

				if (shop)
				{
					p.hotbar.enabledItemBar = true;
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

			String price = item.price + " ";
			if (item.price == 0)
				price = "Free!";
			else if (item.price == 1)
				price += "coin";
			else
				price += "coins";

			Button b = new Button(0, 0, 350, 40, item.name, new Runnable()
			{
				@Override
				public void run()
				{
					int pr = shop.get(j).price;
					if (Game.player.hotbar.coins >= pr)
					{
						if (Game.player.hotbar.itemBar.addItem(shop.get(j)))
							Game.player.hotbar.coins -= pr;
					}
				}
			}
			);

			b.subtext = price;

			this.shopItemButtons.add(b);

			Game.eventsOut.add(new EventAddShopItem(i, item.name, price, item.icon));
		}

		this.initializeShopList();

		Game.eventsOut.add(new EventSortShopButtons());
	}

	public void initializeShopList()
	{
		this.shopList = new ButtonList(this.shopItemButtons, 0, 0, (int) shopOffset, -30);
	}

	@Override
	public void update()
	{
		Game.player.hotbar.update();

		String prevMusic = this.music;
		this.music = null;
		this.musicID = null;

		if (this.playCounter >= 0 && this.playing)
		{
			if (this.playCounter == 0)
				Drawing.drawing.playSound("battle_intro.ogg", 1f, true);

			this.playCounter += Panel.frameFrequency;
		}

		if (this.playCounter * 10 >= introBattleMusicEnd)
		{
			Panel.forceRefreshMusic = true;
			this.playCounter = -1;
		}

		if (this.playCounter < 0)
		{
			if (this.paused || Game.playerTank == null || Game.playerTank.destroy)
				this.music = "battle_paused.ogg";
			else
				this.music = "battle.ogg";

			this.musicID = "battle";
		}

		if (Game.enable3d)
			for (int i = 0; i < Game.obstacles.size(); i++)
			{
				tanks.obstacle.Obstacle o = Game.obstacles.get(i);

				if (o.replaceTiles)
					o.postOverride();
			}

		if (Game.game.input.pause.isValid())
		{
			if (shopScreen)
				shopScreen = false;
			else
				this.paused = !this.paused;

			//if (this.paused)
			//	Game.game.window.setCursorLocked(false);

			Game.game.input.pause.invalidate();
		}

		if (Game.game.input.hidePause.isValid())
		{
			this.screenshotMode = !this.screenshotMode;
			Game.game.input.hidePause.invalidate();
		}

		if (Game.game.input.zoom.isValid())
		{
			Drawing.drawing.movingCamera = !Drawing.drawing.movingCamera;
			Game.game.input.zoom.invalidate();
		}

		if (!finished)
		{
			if (Obstacle.draw_size == 0)
				Drawing.drawing.playSound("level_start.ogg");

			Obstacle.draw_size = Math.min(Game.tile_size, Obstacle.draw_size + Panel.frameFrequency);
		}

		if (paused)
		{
			if (!ScreenPartyHost.isServer && !ScreenPartyLobby.isClient)
			{
				Panel.panel.age -= Panel.frameFrequency;

				if (!playing && Game.startTime >= 0)
					this.updateSingleplayerWaitingMusic();

				this.updateMusic(prevMusic);
			}

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
					if (ScreenInterlevel.fromSavedLevels)
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
				else if (ScreenInterlevel.fromSavedLevels)
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

			if (Drawing.drawing.enableMovingCamera)
				zoom.update();

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
							readyButton.text = "Waiting... (";
						}
						else
						{
							readyButton.text = "Ready (";
							this.music = "waiting_music.ogg";
							this.musicID = null;
						}

						if (ScreenPartyHost.isServer)
						{
							if (!ScreenPartyHost.includedPlayers.contains(Game.clientID))
							{
								readyButton.text = "Spectating... (";
								readyButton.enabled = false;
							}

							readyButton.text += ScreenPartyHost.readyPlayers.size() + "/" + ScreenPartyHost.includedPlayers.size() + ")";
						}
						else
						{
							if (!ScreenPartyLobby.includedPlayers.contains(Game.clientID))
							{
								readyButton.text = "Spectating... (";
								readyButton.enabled = false;
							}

							readyButton.text += ScreenPartyLobby.readyPlayers.size() + "/" + ScreenPartyLobby.includedPlayers.size() + ")";
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
						readyButton.text = "Starting in " + ((int)(Game.startTime / 100) + 1);
					}

					readyButton.update();
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
			playing = true;

			if (Game.followingCam)
			{
				Game.playerTank.angle += (Drawing.drawing.getInterfaceMouseX() - prevCursorX) / 100;
				Game.game.window.setCursorLocked(true);
				this.prevCursorX = Drawing.drawing.getInterfaceMouseX();
				this.prevCursorY = Drawing.drawing.getInterfaceMouseX();
			}

			Obstacle.draw_size = Math.min(Game.tile_size, Obstacle.draw_size);
			ArrayList<Team> aliveTeams = new ArrayList<Team>();
			ArrayList<Team> fullyAliveTeams = new ArrayList<Team>();

			for (int i = 0; i < Game.effects.size(); i++)
			{
				Game.effects.get(i).update();
			}

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
				if (m instanceof ISolidObject && !(m instanceof Tank && !((Tank) m).targetable))
				{
					for (Face f: ((ISolidObject) m).getHorizontalFaces())
						Game.horizontalFaces.add(f);

					for (Face f: ((ISolidObject) m).getVerticalFaces())
						Game.verticalFaces.add(f);
				}
			}

			for (int i = 0; i < Game.game.heightGrid.length; i++)
			{
				Arrays.fill(Game.game.heightGrid[i], 0);
			}

			for (Obstacle o: Game.obstacles)
			{
				Face[] faces = o.getHorizontalFaces();
				boolean[] valid = o.getValidHorizontalFaces();
				for (int i = 0; i < faces.length; i++)
				{
					if (valid[i])
						Game.horizontalFaces.add(faces[i]);
				}

				faces = o.getVerticalFaces();
				valid = o.getValidVerticalFaces();
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
				System.out.println(Game.horizontalFaces);
				Game.exitToCrash(e);
			}

			try
			{
				Collections.sort(Game.verticalFaces);
			}
			catch (Exception e)
			{
				System.out.println(Game.verticalFaces);
				Game.exitToCrash(e);
			}

			for (int i = 0; i < Game.movables.size(); i++)
			{
				Game.movables.get(i).preUpdate();
			}

			for (int i = 0; i < Game.movables.size(); i++)
			{
				Movable m = Game.movables.get(i);
				m.update();

				if (m instanceof Tank)
				{
					Team t;

					if (m.team == null)
					{
						if (m instanceof TankPlayer)
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

			for (int i = 0; i < Game.obstacles.size(); i++)
			{
				Obstacle o = Game.obstacles.get(i);

				if (o.update)
					o.update();
			}

			for (int i = 0; i < Game.tracks.size(); i++)
			{
				Game.tracks.get(i).update();
			}

			Game.player.hotbar.update();

			if (fullyAliveTeams.size() <= 1)
				ScreenGame.finishedQuick = true;

			if (aliveTeams.size() <= 1)
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
						if (Obstacle.draw_size == Game.tile_size)
							Drawing.drawing.playSound("level_end.ogg");

						Obstacle.draw_size = Math.max(0, Obstacle.draw_size - Panel.frameFrequency);
						for (int i = 0; i < Game.movables.size(); i++)
							Game.movables.get(i).destroy = true;

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

									Panel.win = true;
								}
								else
								{
									if (Crusade.crusadeMode)
										Panel.winlose = "Battle failed!";
									else
										Panel.winlose = "You were destroyed!";

									Panel.win = false;
								}
							}
							else
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

									for (int i = 0; i < Game.players.size(); i++)
									{
										Game.eventsOut.add(new EventUpdateRemainingLives(Game.players.get(i)));
									}
								}
								else
									Game.screen = new ScreenPartyInterlevel();

								System.gc();
							}
							else if (!Game.currentLevel.remote)
							{
								if (name != null)
									Game.exit(name);
								else
									Game.exit();
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

		this.updateMusic(prevMusic);

		for (int i = 0; i < Game.removeMovables.size(); i++)
			Game.movables.remove(Game.removeMovables.get(i));

		for (int i = 0; i < Game.removeObstacles.size(); i++)
		{
			Obstacle o = Game.removeObstacles.get(i);
			int x = (int) (o.posX / Game.tile_size);
			int y = (int) (o.posY / Game.tile_size);

			if (x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY && o.bulletCollision)
			{
				Game.game.solidGrid[x][y] = false;
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
			if (this.playing && (!this.paused || ScreenPartyHost.isServer || ScreenPartyLobby.isClient) && !ScreenGame.finished)
				slant = Math.min(1, slant + 0.01 * Panel.frameFrequency);
			else if (ScreenGame.finished)
				slant = Math.max(0, slant - 0.01 * Panel.frameFrequency);

			this.slantRotation.pitch = this.slant * -Math.PI / 16;
			this.slantTranslation.y = -this.slant * 0.05;

			if (!Game.followingCam)
			{
				Game.game.window.transformations.add(this.slantTranslation);
				Game.game.window.transformations.add(this.slantRotation);
			}
			Game.game.window.loadPerspective();
		}

		if (Game.followingCam && Game.framework == Game.Framework.lwjgl)
		{
			double frac = Panel.panel.zoomTimer;

			if (!Game.firstPerson)
			{
				Game.game.window.transformations.add(new RotationAboutPoint(Game.game.window, 0, 0, frac * ((Game.playerTank.angle + Math.PI * 3 / 2) % (Math.PI * 2) - Math.PI), 0, -Drawing.drawing.statsHeight / Game.game.window.absoluteHeight / 2, 0));
				Game.game.window.transformations.add(new Translation(Game.game.window, 0, 0.1 * frac, 0));
				Game.game.window.transformations.add(new RotationAboutPoint(Game.game.window, 0, -Math.PI * 0.25 * frac, 0, 0, 0, -1));
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
		this.showDefaultMouse = !(!this.paused && this.playing && Game.angledView);

		this.setPerspective();

		this.drawDefaultBackground();

		Drawing drawing = Drawing.drawing;

		for (int i = 0; i < Game.obstacles.size(); i++)
		{
			Obstacle o = Game.obstacles.get(i);
			drawables[o.drawLevel].add(o);

			int x = (int) (o.posX / Game.tile_size);
			int y = (int) (o.posY / Game.tile_size);

			if (!(!Game.fancyGraphics || !Game.enable3d || x < 0 || x >= Game.currentSizeX || y < 0 || y >= Game.currentSizeY))
				Game.game.heightGrid[x][y] = o.getTileHeight();
		}

		for (int i = 0; i < Game.tracks.size(); i++)
			drawables[0].add(Game.tracks.get(i));

		for (int i = 0; i < Game.movables.size(); i++)
		{
			Movable m = Game.movables.get(i);

			drawables[m.drawLevel].add(m);

			if (m.showName)
				drawables[m.nameTag.drawLevel].add(m.nameTag);
		}

		for (int i = 0; i < Game.effects.size(); i++)
		{
			Effect e = Game.effects.get(i);
			drawables[e.drawLayer].add(e);
		}

		if (Game.game.window.touchscreen)
		{
			drawables[9].add(TankPlayer.controlStick);

			if (TankPlayer.shootStickEnabled)
				drawables[9].add(TankPlayer.shootStick);
		}

		for (int i = 0; i < this.drawables.length; i++)
		{
			if (i == 5 && Game.enable3d)
			{
				double frac = Obstacle.draw_size / Game.tile_size;
				Drawing.drawing.setColor(174 * frac + Level.currentColorR * (1 - frac), 92 * frac + Level.currentColorG * (1 - frac), 16  * frac + Level.currentColorB * (1 - frac));
				Drawing.drawing.fillForcedBox(drawing.sizeX / 2, -Game.tile_size / 2, 0, drawing.sizeX + Game.tile_size * 2, Game.tile_size, Obstacle.draw_size, (byte) 0);
				Drawing.drawing.fillForcedBox(drawing.sizeX / 2, Drawing.drawing.sizeY + Game.tile_size / 2, 0, drawing.sizeX + Game.tile_size * 2, Game.tile_size, Obstacle.draw_size, (byte) 0);
				Drawing.drawing.fillForcedBox(-Game.tile_size / 2, drawing.sizeY / 2, 0, Game.tile_size, drawing.sizeY, Obstacle.draw_size, (byte) 0);
				Drawing.drawing.fillForcedBox(drawing.sizeX + Game.tile_size / 2, drawing.sizeY / 2, 0, Game.tile_size, drawing.sizeY, Obstacle.draw_size, (byte) 0);
			}

			if (i == 9 && this.tutorial != null)
			{
				this.tutorial.draw();
			}

			for (int j = 0; j < this.drawables[i].size(); j++)
			{
				IDrawable d = this.drawables[i].get(j);

				if (d != null)
					d.draw();
			}

			if (Game.superGraphics)
			{
				for (int j = 0; j < this.drawables[i].size(); j++)
				{
					IDrawable d = this.drawables[i].get(j);

					if (d instanceof IDrawableWithGlow && ((IDrawableWithGlow) d).isGlowEnabled())
						((IDrawableWithGlow) d).drawGlow();
				}
			}

			if (i == 9 && (Game.playerTank instanceof IPlayerTank && ((IPlayerTank) Game.playerTank).showTouchCircle()))
			{
				Drawing.drawing.setColor(255, 127, 0, 63);
				Drawing.drawing.fillInterfaceOval(Drawing.drawing.toInterfaceCoordsX(Game.playerTank.posX),
						Drawing.drawing.toInterfaceCoordsY(Game.playerTank.posY),
						((IPlayerTank) Game.playerTank).getTouchCircleSize(), ((IPlayerTank) Game.playerTank).getTouchCircleSize());
			}

			if (i == 9 && Game.playerTank != null && !Game.playerTank.destroy && (ScreenPartyHost.isServer || ScreenPartyLobby.isClient)
					&& Game.screen instanceof ScreenGame && !((ScreenGame) Game.screen).playing && Game.movables.contains(Game.playerTank))
			{
				double fade = Math.max(0, Math.sin(Math.min(Game.startTime, 50) / 100 * Math.PI));

				double frac = (System.currentTimeMillis() % 2000) / 2000.0;
				double size = Math.max(800 * (0.5 - frac), 0) * fade;
				Drawing.drawing.setColor(Game.player.colorR, Game.player.colorG, Game.player.colorB, 64 * Math.sin(Math.min(frac * Math.PI, Math.PI / 2)) * fade);

				if (Game.enable3d)
					Drawing.drawing.fillOval(Game.playerTank.posX, Game.playerTank.posY, Game.playerTank.size / 2, size, size, false, false);
				else
					Drawing.drawing.fillOval(Game.playerTank.posX, Game.playerTank.posY, size, size);

				double frac2 = ((250 + System.currentTimeMillis()) % 2000) / 2000.0;
				double size2 = Math.max(800 * (0.5 - frac2), 0) * fade;

				Drawing.drawing.setColor(Game.player.turretColorR, Game.player.turretColorG, Game.player.turretColorB, 64 * Math.sin(Math.min(frac2 * Math.PI, Math.PI / 2)) * fade);

				if (Game.enable3d)
					Drawing.drawing.fillOval(Game.playerTank.posX, Game.playerTank.posY, Game.playerTank.size / 2, size2, size2, false, false);
				else
					Drawing.drawing.fillOval(Game.playerTank.posX, Game.playerTank.posY, size2, size2);

				Drawing.drawing.setColor(Game.player.colorR, Game.player.colorG, Game.player.colorB);
				this.drawSpinny(Game.playerTank.posX, Game.playerTank.posY, Game.playerTank.size / 2, 200, 4, 0.3, 75 * fade, 0.5 * fade, false);
				Drawing.drawing.setColor(Game.player.turretColorR, Game.player.turretColorG, Game.player.turretColorB);
				this.drawSpinny(Game.playerTank.posX, Game.playerTank.posY, Game.playerTank.size / 2,198, 3, 0.5, 60 * fade, 0.375 * fade, false);
			}

			drawables[i].clear();
		}

		Drawing.drawing.setColor(0, 0, 0, Math.max(0, Panel.darkness));
		Game.game.window.fillRect(0, 0, Game.game.window.absoluteWidth, Game.game.window.absoluteHeight - Drawing.drawing.statsHeight);

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

		//Game.game.window.setAngles(0, 0, 0);
		//Game.game.window.setOffsets(0,  0, 0);

		if (!playing)
		{
			if (Crusade.crusadeMode)
			{
				if (Level.currentColorR + Level.currentColorG + Level.currentColorB < 127 * 3)
					Drawing.drawing.setColor(255, 255, 255, 127);
				else
					Drawing.drawing.setColor(0, 0, 0, 127);

				Drawing.drawing.setInterfaceFontSize(100);
				Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, "Battle " + (Crusade.currentCrusade.currentLevel + 1));
			}

			if (!title.equals(""))
			{
				if (Level.currentColorR + Level.currentColorG + Level.currentColorB < 127 * 3)
					Drawing.drawing.setColor(255, 255, 255, 127);
				else
					Drawing.drawing.setColor(0, 0, 0, 127);

				Drawing.drawing.setInterfaceFontSize(100);
				Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, title);
			}

			if (shopScreen)
			{
				Drawing.drawing.setColor(127, 178, 228, 64);
				Game.game.window.fillRect(0, 0, Game.game.window.absoluteWidth + 1, Game.game.window.absoluteHeight + 1);

				Drawing.drawing.setInterfaceFontSize(this.titleSize);

				if (Level.currentColorR + Level.currentColorG + Level.currentColorB < 127 * 3)
					Drawing.drawing.setColor(255, 255, 255);
				else
					Drawing.drawing.setColor(0, 0, 0);

				Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 210 + shopOffset, "Shop");

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

						for (Player p: ScreenPartyHost.readyPlayers)
							readyPlayers.add(p.username);
					}
					else
						readyPlayers = ScreenPartyLobby.readyPlayers;

					double extraWidth = (Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2;
					double height = (Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale;

					Drawing.drawing.setColor(0, 0, 0, Math.max(0, 127 * Math.min(1, (readyPanelCounter * 10) / 200) * Math.min(Game.startTime / 25, 1)));
					Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX + extraWidth / 2, Drawing.drawing.interfaceSizeY / 2, extraWidth, height);
					Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX - Math.min(readyPanelCounter * 10, 200), Drawing.drawing.interfaceSizeY / 2,
							Math.min(readyPanelCounter * 20, 400), height);

					double c = readyPanelCounter - 35;

					if (c > 0)
					{
						Drawing.drawing.setColor(255, 255, 255, Math.max(Math.min(Game.startTime / 25, 1) * 255, 0));
						Drawing.drawing.setInterfaceFontSize(this.titleSize);

						Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX - 200, 50, "Ready players:");
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
							Drawing.drawing.setColor(255, 255, 255, Math.max(Math.min(Game.startTime / 25, 1) * 255, 0));
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
						Drawing.drawing.setColor(255, 255, 255, Math.min(Game.startTime / 25, 1) * 127);
						Drawing.drawing.setInterfaceFontSize(this.textSize);

						for (int i = readyNamesCount; i < Math.min(includedPlayers, slots); i++)
						{
							Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX - 200, 40 * i + 100, "Waiting...");
						}

						int extra = includedPlayers - Math.max(readyNamesCount, slots);
						if (extra > 0)
						{
							if (extra == 1)
								Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX - 200, 40 * slots + 100, "Waiting...");
							else
								Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX - 200, 40 * slots + 100, extra + " waiting...");
						}
					}

					if (prevReadyNames != readyNamesCount)
						Drawing.drawing.playSound("bullet_explode.ogg", 1.5f);

					prevReadyNames = readyNamesCount;

					readyButton.draw();
				}

				if (!this.shopItemButtons.isEmpty() && this.readyButton.enabled)
					enterShop.draw();

				if (ScreenPartyHost.isServer && this.cancelCountdown)
					startNow.draw();

				if ((ScreenPartyHost.isServer || ScreenPartyLobby.isClient || Game.autostart) && !cancelCountdown)
				{
					Drawing.drawing.setColor(127, 127, 127);
					Drawing.drawing.fillInterfaceRect(play.posX, play.posY + play.sizeY / 2 - 5, play.sizeX * 32 / 35, 3);
					Drawing.drawing.setColor(255, 127, 0);
					Drawing.drawing.fillInterfaceProgressRect(play.posX, play.posY + play.sizeY / 2 - 5, play.sizeX * 32 / 35, 3, Math.max(Game.startTime / 400, 0));

					if (Game.superGraphics)
					{
						Drawing.drawing.fillInterfaceGlow(play.posX + ((Game.startTime / 400 - 0.5) * (play.sizeX * 32 / 35)), play.posY + play.sizeY / 2 - 5, 20, 20);
					}
				}
			}
		}

		if (!paused && Game.game.window.touchscreen && !shopScreen)
		{
			pause.draw();
			Drawing.drawing.drawInterfaceImage("pause.png", pause.posX, pause.posY, 40, 40);

			if (Drawing.drawing.enableMovingCamera)
			{
				zoom.draw();

				if (Drawing.drawing.movingCamera)
					Drawing.drawing.drawInterfaceImage("zoom_out.png", zoom.posX, zoom.posY, 40, 40);
				else
					Drawing.drawing.drawInterfaceImage("zoom_in.png", zoom.posX, zoom.posY, 40, 40);
			}
		}

		Game.player.hotbar.draw();

		if (paused && !screenshotMode)
		{
			Drawing.drawing.setColor(127, 178, 228, 64);
			Game.game.window.fillRect(0, 0, Game.game.window.absoluteWidth + 1, Game.game.window.absoluteHeight + 1);

			if (ScreenPartyLobby.isClient)
			{
				closeMenuClient.draw();
				exitParty.draw();
			}
			else if (ScreenPartyHost.isServer)
			{
				if (ScreenInterlevel.fromSavedLevels)
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
			else if (ScreenInterlevel.fromSavedLevels)
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

			if (Level.currentColorR + Level.currentColorG + Level.currentColorB < 127 * 3)
				Drawing.drawing.setColor(255, 255, 255);

			if (!ScreenPartyHost.isServer && !ScreenPartyLobby.isClient)
				Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, "Game paused");
			else
				Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, "Game menu");
		}

		if (this.overlay != null)
			this.overlay.draw();

		Drawing.drawing.setInterfaceFontSize(this.textSize);
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

	public void drawSpinny(double x, double y, double z, int max, int parts, double speed, double size, double dotSize, boolean invert)
	{
		for (int i = 0; i < max; i++)
		{
			double frac = (System.currentTimeMillis() / 1000.0 * speed + i * 1.0 / max) % 1;
			double s = Math.max(Math.abs((i % (max * 1.0 / parts)) / 10.0 * parts), 0);

			if (invert)
			{
				frac = -frac;
			}

			double v = size * Math.cos(frac * Math.PI * 2);
			double v1 = size * Math.sin(frac * Math.PI * 2);

			if (Game.enable3d)
				Drawing.drawing.fillOval(x + v, y + v1, z, s * dotSize, s * dotSize, false, false);
			else
				Drawing.drawing.fillOval(x + v, y + v1, s * dotSize, s * dotSize);
		}
	}
}
