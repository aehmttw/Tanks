package tanks;

import basewindow.BaseFile;
import basewindow.IBatchRenderableObject;
import basewindow.InputCodes;
import basewindow.transformation.Translation;
import tanks.gui.TerrainRenderer;
import tanks.gui.TrackRenderer;
import tanks.network.event.EventBeginLevelCountdown;
import tanks.network.event.online.IOnlineServerEvent;
import tanks.extension.Extension;
import tanks.gui.IFixedMenu;
import tanks.gui.TextBox;
import tanks.gui.screen.*;
import tanks.gui.screen.leveleditor.ScreenLevelEditor;
import tanks.hotbar.Hotbar;
import tanks.network.Client;
import tanks.network.ClientHandler;
import tanks.network.MessageReader;
import tanks.obstacle.Obstacle;
import tanks.obstacle.ObstacleLight;
import tanks.tank.*;

import java.util.ArrayList;
import java.util.Arrays;

public class Panel
{
	public static boolean onlinePaused;

	public double zoomTimer = 0;
	public static double zoomTarget = -1;
	public static boolean autoZoom = true;
	public static double lastAutoZoomSpeed = 0;

	public static double windowWidth = 1400;
	public static double windowHeight = 900;

	public final long splash_duration = 4000;
	public boolean playedTutorialIntroMusic = false;

	public static boolean showMouseTarget = true;
	public static boolean showMouseTargetHeight = false;

	public static Panel panel;

	public static boolean forceRefreshMusic;

	public static String winlose = "";
	public static boolean win = false;
	public static boolean levelPassed = false;

	public static double darkness = 0;

	public static TextBox selectedTextBox;

	public Translation zoomTranslation = new Translation(Game.game.window, 0, 0, 0);

	/** Important value used in calculating game speed. Larger values are set when the frames are lower, and game speed is increased to compensate.*/
	public static double frameFrequency = 1;

	//ArrayList<Double> frameFrequencies = new ArrayList<Double>();

	public int frames = 0;

	public double frameSampling = 1;

	public long firstFrameSec = (long) (System.currentTimeMillis() / 1000.0 * frameSampling);
	public long lastFrameSec = (long) (System.currentTimeMillis() / 1000.0 * frameSampling);

	public long startTime = System.currentTimeMillis();
	public long frameStartTime = System.currentTimeMillis();

	public long lastFrameNano = 0;

	public int lastFPS = 0;

	public ScreenOverlayOnline onlineOverlay;

	protected static boolean initialized = false;

	public Tank dummySpin;

	public boolean firstFrame = true;
	public boolean firstDraw = true;
	public boolean introFinished = false;
	public boolean splashFinished = false;

	public boolean startMusicPlayed = false;

	public long introMusicEnd;

	public ArrayList<Double> pastPlayerX = new ArrayList<>();
	public ArrayList<Double> pastPlayerY = new ArrayList<>();
	public ArrayList<Double> pastPlayerTime = new ArrayList<>();

	public double age = 0;
	public long ageFrames = 0;

	public boolean started = false;
	public boolean settingUp = true;

	public ArrayList<double[]> lights = new ArrayList<>();

	public static void initialize()
	{
		if (!initialized)
			panel = new Panel();

		initialized = true;
	}

	private Panel()
	{

	}

	public void setUp()
	{
		Drawing.drawing.defaultRenderer = new Drawing.LevelRenderer();
		Drawing.drawing.terrainRenderer = Drawing.drawing.defaultRenderer.terrainRenderer;
		Drawing.drawing.terrainRendererTransparent = Drawing.drawing.defaultRenderer.terrainRendererTransparent;
		Drawing.drawing.terrainRendererShrubbery = Drawing.drawing.defaultRenderer.terrainRendererShrubbery;
		Drawing.drawing.terrainRenderer2 = new TerrainRenderer();
		Drawing.drawing.trackRenderer = new TrackRenderer();

		ModAPI.setUp();

		Game.resetTiles();

		if (Game.game.fullscreen)
			Game.game.window.setFullscreen(Game.game.fullscreen);

		Game.game.window.setIcon("/images/icon64.png");

		if (Game.game.window.soundPlayer == null)
		{
			Game.soundsEnabled = false;
			Game.musicEnabled = false;
		}

		double scale = 1;
		if (Game.game.window.touchscreen && Game.game.window.pointHeight > 0 && Game.game.window.pointHeight <= 500)
		{
			scale = 1.25;

			Drawing.drawing.objWidth *= 1.4;
			Drawing.drawing.objHeight *= 1.4;
			Drawing.drawing.objXSpace *= 1.4;
			Drawing.drawing.objYSpace *= 1.4;

			Drawing.drawing.textSize = Drawing.drawing.objHeight * 0.6;
			Drawing.drawing.titleSize = Drawing.drawing.textSize * 1.25;
		}

		Drawing.drawing.setInterfaceScaleZoom(scale);
		TankPlayer.setShootStick(TankPlayer.shootStickEnabled);
		TankPlayer.controlStick.mobile = TankPlayer.controlStickMobile;
		TankPlayer.controlStick.snap = TankPlayer.controlStickSnap;

		Hotbar.toggle.posX = Drawing.drawing.interfaceSizeX / 2;
		Hotbar.toggle.posY = Drawing.drawing.interfaceSizeY - 20;

		Game.createModels();

		Game.dummyTank = new TankDummy("dummy",0, 0, 0);
		Game.dummyTank.team = null;

		for (Extension e : Game.extensionRegistry.extensions)
			e.loadResources();

		zoomTranslation.window = Game.game.window;
		zoomTranslation.applyAsShadow = true;
		dummySpin = new TankDummyLoadingScreen(Drawing.drawing.sizeX / 2, Drawing.drawing.sizeY / 2);

		if (Game.usernameInvalid(Game.player.username))
			Game.screen = new ScreenUsernameInvalid();
		else
		{
			if (Game.cinematic)
				Game.screen = new ScreenCinematicTitle();
			else
				Game.screen = new ScreenTitle();
		}

		ScreenChangelog.Changelog.setupLogs();

		ScreenChangelog s = new ScreenChangelog();
		s.setup();

		if (!s.pages.isEmpty())
			Game.screen = s;

		Game.loadTankMusic();

		if (Game.game.window.soundsEnabled)
		{
			Game.game.window.soundPlayer.musicPlaying = true;

			for (int i = 1; i <= 5; i++)
			{
				Game.game.window.soundPlayer.registerCombinedMusic("/music/menu_" + i + ".ogg", "menu");
			}

			Game.game.window.soundPlayer.registerCombinedMusic("/music/menu_options.ogg", "menu");

			for (int i = 1; i <= 2; i++)
			{
				Game.game.window.soundPlayer.registerCombinedMusic("/music/ready_music_" + i + ".ogg", "ready");
			}

			Game.game.window.soundPlayer.registerCombinedMusic("/music/battle.ogg", "battle");
			Game.game.window.soundPlayer.registerCombinedMusic("/music/battle_paused.ogg", "battle");

			Game.game.window.soundPlayer.registerCombinedMusic("/music/battle_night.ogg", "battle_night");
			Game.game.window.soundPlayer.registerCombinedMusic("/music/battle_paused.ogg", "battle_night");

			Game.game.window.soundPlayer.registerCombinedMusic("/music/battle_timed.ogg", "battle_timed");
			Game.game.window.soundPlayer.registerCombinedMusic("/music/battle_timed_paused.ogg", "battle_timed");

			//Game.game.window.soundPlayer.registerCombinedMusic("/music/editor.ogg", "editor");
			//Game.game.window.soundPlayer.registerCombinedMusic("/music/editor_paused.ogg", "editor");
		}

		if (Game.game.window.soundsEnabled)
		{
			Game.game.window.soundPlayer.loadMusic("/music/ready_music_1.ogg");
			Game.game.window.soundPlayer.loadMusic("/music/ready_music_2.ogg");
			Game.game.window.soundPlayer.loadMusic("/music/battle.ogg");
			Game.game.window.soundPlayer.loadMusic("/music/battle_night.ogg");
			Game.game.window.soundPlayer.loadMusic("/music/battle_timed.ogg");
			Game.game.window.soundPlayer.loadMusic("/music/battle_paused.ogg");
			Game.game.window.soundPlayer.loadMusic("/music/battle_timed_paused.ogg");

			Game.game.window.soundPlayer.loadMusic("/music/battle.ogg");

			for (int i = 1; i <= 8; i++)
			{
				Game.game.window.soundPlayer.loadMusic("/music/arcade/rampage" + i + ".ogg");
			}
		}

		settingUp = false;
	}

	public boolean screenshot = false;
	public void update()
	{
		if (firstFrame)
			this.setUp();

		firstFrame = false;

		Game.prevScreen = Game.screen;
		Obstacle.lastDrawSize = Obstacle.draw_size;

		if (!started && (Game.game.window.validPressedKeys.contains(InputCodes.KEY_F) || !Game.cinematic))
		{
			started = true;

			//this.startTime = System.currentTimeMillis() + splash_duration;
			//Drawing.drawing.playSound("splash_jingle.ogg");
		}

		if (!started)
			this.startTime = System.currentTimeMillis();

		int maxFps = Game.maxFPS;
		if (Game.deterministicMode && Game.deterministic30Fps)
			maxFps = 30;
		else if (Game.deterministicMode)
			maxFps = 60;

		if (maxFps > 0)
		{
			int frameTime = 1000000000 / maxFps;
			while (System.nanoTime() - lastFrameNano < frameTime)
			{

			}
		}

		lastFrameNano = System.nanoTime();

		if (System.currentTimeMillis() - this.startTime < 0)
		{
			return;
		}

		if (!splashFinished)
		{
			splashFinished = true;

			boolean tutorial = false;

			BaseFile tutorialFile = Game.game.fileManager.getFile(Game.homedir + Game.tutorialPath);
			if (!tutorialFile.exists())
			{
				tutorial = true;
				Game.silentCleanUp();
				Game.lastVersion = Game.version;
				ScreenOptions.saveOptions(Game.homedir);
				new Tutorial().loadTutorial(true, Game.game.window.touchscreen);
				((ScreenGame) Game.screen).introBattleMusicEnd = 0;
			}

			introMusicEnd = System.currentTimeMillis() + Long.parseLong(Game.game.fileManager.getInternalFileContents("/music/intro_length.txt").get(0));

			introMusicEnd -= 40;

			if (Game.framework == Game.Framework.libgdx)
				introMusicEnd -= 100;

			if (!tutorial)
				Drawing.drawing.playMusic("menu_intro.ogg", Game.musicVolume, false, "intro", 0, false);
		}

		Game.game.window.constrainMouse = Game.constrainMouse && ((Game.screen instanceof ScreenGame && !((ScreenGame) Game.screen).paused && ((ScreenGame) Game.screen).playing && Game.playerTank != null && !Game.playerTank.destroy) || Game.screen instanceof ScreenLevelEditor);

		if (!Game.shadowsEnabled)
			Game.game.window.setShadowQuality(0);
		else
			Game.game.window.setShadowQuality(Game.shadowQuality / 10.0 * 1.25);

		Screen prevScreen = Game.screen;

		if (!startMusicPlayed && Game.game.window.soundsEnabled && (System.currentTimeMillis() > introMusicEnd || !("menu".equals(Game.screen.musicID))))
		{
			startMusicPlayed = true;
			this.playScreenMusic(0);
		}

		Panel.windowWidth = Game.game.window.absoluteWidth;
		Panel.windowHeight = Game.game.window.absoluteHeight;

		Drawing.drawing.scale = Math.min(Panel.windowWidth / Game.currentSizeX, (Panel.windowHeight - Drawing.drawing.statsHeight) / Game.currentSizeY) / 50.0;
		Drawing.drawing.unzoomedScale = Drawing.drawing.scale;
		Drawing.drawing.interfaceScale = Drawing.drawing.interfaceScaleZoom * Math.min(Panel.windowWidth / 28, (Panel.windowHeight - Drawing.drawing.statsHeight) / 18) / 50.0;
		Game.game.window.absoluteDepth = Drawing.drawing.interfaceScale * Game.absoluteDepthBase;

		if (Game.deterministicMode && Game.deterministic30Fps)
			Panel.frameFrequency = 100.0 / 30;
		else if (Game.deterministicMode)
			Panel.frameFrequency = 100.0 / 60;
		else
			Panel.frameFrequency = Game.game.window.frameFrequency;

		Game.game.window.showKeyboard = false;

		double introTime = 1000;
		double introAnimationTime = 500;

		if (Game.fancyTerrain && Game.enable3d)
			introAnimationTime = 1000;

		if (System.currentTimeMillis() - startTime < introTime + introAnimationTime)
		{
			if (ScreenInterlevel.tutorialInitial && System.currentTimeMillis() - startTime > introTime + introAnimationTime - 1500 && !playedTutorialIntroMusic)
			{
				playedTutorialIntroMusic = true;
				Drawing.drawing.playSound("battle_intro.ogg", Game.musicVolume, true);
				introMusicEnd = System.currentTimeMillis() + Long.parseLong(Game.game.fileManager.getInternalFileContents("/music/battle_intro_length.txt").get(0));
			}

			dummySpin.posX = Drawing.drawing.sizeX / 2;
			dummySpin.posY = Drawing.drawing.sizeY / 2;
			dummySpin.angle = Math.PI * 2 * (System.currentTimeMillis() - startTime) / (introTime + introAnimationTime);
			return;
		}

		if (settingUp)
			return;

		if (Game.screen instanceof ScreenGame)
		{
			for (IFixedMenu menu : ModAPI.menuGroup)
				menu.update();
		}

		synchronized (Game.eventsIn)
		{
			for (int i = 0; i < Game.eventsIn.size(); i++)
			{
				if (!(Game.eventsIn.get(i) instanceof IOnlineServerEvent))
					Game.eventsIn.get(i).execute();
			}

			Game.eventsIn.clear();
		}

		for (int i = 0; i < Game.game.heightGrid.length; i++)
		{
			Arrays.fill(Game.game.heightGrid[i], -1000);
			Arrays.fill(Game.game.groundHeightGrid[i], -1000);
		}

		if (ScreenPartyHost.isServer)
		{
			synchronized (ScreenPartyHost.disconnectedPlayers)
			{
				for (int i = 0; i < ScreenPartyHost.disconnectedPlayers.size(); i++)
				{
					for (int j = 0; j < Game.movables.size(); j++)
					{
						Movable m = Game.movables.get(j);
						if (m instanceof TankPlayerRemote && ((TankPlayerRemote) m).player.clientID.equals(ScreenPartyHost.disconnectedPlayers.get(i)))
							((TankPlayerRemote) m).health = 0;
					}

					ScreenPartyHost.includedPlayers.remove(ScreenPartyHost.disconnectedPlayers.get(i));

					for (Player p : Game.players)
					{
						if (p.clientID.equals(ScreenPartyHost.disconnectedPlayers.get(i)))
						{
							ScreenPartyHost.readyPlayers.remove(p);

							if (Crusade.currentCrusade != null)
							{
								if (Crusade.currentCrusade.crusadePlayers.containsKey(p))
								{
									Crusade.currentCrusade.crusadePlayers.get(p).coins = p.hotbar.coins;
									Crusade.currentCrusade.disconnectedPlayers.add(Crusade.currentCrusade.crusadePlayers.remove(p));
								}
							}
						}
					}

					Game.removePlayer(ScreenPartyHost.disconnectedPlayers.get(i));
				}

				if (ScreenPartyHost.readyPlayers.size() >= ScreenPartyHost.includedPlayers.size() && Game.screen instanceof ScreenGame && ((ScreenGame) Game.screen).cancelCountdown)
				{
					Game.eventsOut.add(new EventBeginLevelCountdown());
					((ScreenGame) Game.screen).cancelCountdown = false;
				}

				ScreenPartyHost.disconnectedPlayers.clear();
			}
		}

		if (Game.player.hotbar.coins < 0)
			Game.player.hotbar.coins = 0;

		if (!(Game.screen instanceof ScreenInfo))
		{
			if (!(Game.screen instanceof ScreenGame) || Panel.zoomTarget < 0 ||
					((Game.playerTank == null || Game.playerTank.destroy) && (((ScreenGame) Game.screen).spectatingTank == null)) || !((ScreenGame) Game.screen).playing)
				this.zoomTimer -= 0.02 * Panel.frameFrequency;
		}

		if (((Game.playerTank != null && !Game.playerTank.destroy) || (Game.screen instanceof ScreenGame && ((ScreenGame) Game.screen).spectatingTank != null)) && !ScreenGame.finished
				&& (Drawing.drawing.unzoomedScale < Drawing.drawing.interfaceScale || Game.followingCam)
				&& Game.screen instanceof ScreenGame && (((ScreenGame) (Game.screen)).playing || ((ScreenPartyHost.isServer || ScreenPartyLobby.isClient) && Game.startTime < Game.currentLevel.startTime)))
		{
			Drawing.drawing.enableMovingCamera = Drawing.drawing.unzoomedScale < Drawing.drawing.interfaceScale;

			if (Game.playerTank == null || Game.playerTank.destroy)
			{
				Drawing.drawing.playerX = ((ScreenGame) Game.screen).spectatingTank.posX;
				Drawing.drawing.playerY = ((ScreenGame) Game.screen).spectatingTank.posY;
			}
			else
			{
				Drawing.drawing.playerX = Game.playerTank.posX;
				Drawing.drawing.playerY = Game.playerTank.posY;

				if (Game.playerTank instanceof TankPlayerController)
				{
					Drawing.drawing.playerX = ((TankPlayerController) Game.playerTank).interpolatedPosX;
					Drawing.drawing.playerY = ((TankPlayerController) Game.playerTank).interpolatedPosY;
				}
			}

			this.pastPlayerX.add(Drawing.drawing.playerX);
			this.pastPlayerY.add(Drawing.drawing.playerY);
			this.pastPlayerTime.add(this.age);

			while (Panel.panel.pastPlayerTime.size() > 1 && Panel.panel.pastPlayerTime.get(1) < Panel.panel.age - Drawing.drawing.getTrackOffset())
			{
				Panel.panel.pastPlayerX.remove(0);
				Panel.panel.pastPlayerY.remove(0);
				Panel.panel.pastPlayerTime.remove(0);
			}

			if (Drawing.drawing.movingCamera)
			{
				if (!(Game.screen instanceof ScreenGame) || Panel.zoomTarget < 0 ||
						((Game.playerTank == null || Game.playerTank.destroy) && (((ScreenGame) Game.screen).spectatingTank == null)) ||
						!((ScreenGame) Game.screen).playing)
					this.zoomTimer += 0.04 * Panel.frameFrequency;

				double mul = Panel.zoomTarget;
				if (mul < 0)
					mul = 1;

				if (Game.startTime > 0 && (ScreenPartyHost.isServer || ScreenPartyLobby.isClient))
					this.zoomTimer = Math.min(this.zoomTimer, mul * (1 - Game.startTime / Game.currentLevel.startTime));
			}
		}
		else
		{
			Drawing.drawing.enableMovingCamera = false;
		}

		this.zoomTimer = Math.min(Math.max(this.zoomTimer, 0), 1);

		if (Game.screen instanceof ScreenGame && Drawing.drawing.enableMovingCamera && Panel.zoomTarget >= 0 && (((ScreenGame) Game.screen).spectatingTank != null || (Game.playerTank != null && !Game.playerTank.destroy)) && ((ScreenGame) Game.screen).playing)
		{
			double speed = 0.3 * Drawing.drawing.unzoomedScale;
			double accel = 0.0003 * Drawing.drawing.unzoomedScale;
			double distDampen = 2;

			if (Panel.autoZoom)
			{
				speed /= 4;

				if (speed - Panel.lastAutoZoomSpeed > accel * Panel.frameFrequency)
					speed = Panel.lastAutoZoomSpeed + accel * Panel.frameFrequency;

				if (-speed + Panel.lastAutoZoomSpeed > accel * Panel.frameFrequency)
					speed = Panel.lastAutoZoomSpeed - accel * Panel.frameFrequency;

				double dist = Math.abs(this.zoomTimer - Panel.zoomTarget) / Drawing.drawing.unzoomedScale;
				if (dist < distDampen)
					speed *= Math.pow(dist / distDampen, Panel.frameFrequency / 20);

				Panel.lastAutoZoomSpeed = speed;

				if (Math.abs(Panel.zoomTarget - this.zoomTimer) < speed)
				{
					this.zoomTimer = Panel.zoomTarget;
				}
				else
				{
					speed *= Math.signum(Panel.zoomTarget - this.zoomTimer);
					this.zoomTimer = this.zoomTimer + speed * Panel.frameFrequency;
				}
			}
			else
			{
				if (this.zoomTimer > Panel.zoomTarget)
					speed = -0.02;
				else
					speed = 0.02;

				if (this.zoomTimer > Panel.zoomTarget)
					this.zoomTimer = Math.max(this.zoomTimer + speed * Panel.frameFrequency, Panel.zoomTarget);
				else
					this.zoomTimer = Math.min(this.zoomTimer + speed * Panel.frameFrequency, Panel.zoomTarget);
			}
		}

		Drawing.drawing.scale = Game.screen.getScale();

		Drawing.drawing.enableMovingCameraX = (Panel.windowWidth < Game.currentSizeX * Game.tile_size * Drawing.drawing.scale);
		Drawing.drawing.enableMovingCameraY = ((Panel.windowHeight - Drawing.drawing.statsHeight) < Game.currentSizeY * Game.tile_size * Drawing.drawing.scale);

		if (Game.connectedToOnline && Panel.selectedTextBox == null)
		{
			if (Game.game.window.validPressedKeys.contains(InputCodes.KEY_ESCAPE))
			{
				Game.game.window.validPressedKeys.remove((Integer) InputCodes.KEY_ESCAPE);

				onlinePaused = !onlinePaused;
			}
		}
		else
			onlinePaused = false;

		ScreenOverlayChat.update(!(Game.screen instanceof IHiddenChatboxScreen));

		if (Game.screen.interfaceScaleZoomOverride > 0)
			Drawing.drawing.interfaceScaleZoom = Game.screen.interfaceScaleZoomOverride;
		else
			Drawing.drawing.interfaceScaleZoom = Drawing.drawing.interfaceScaleZoomDefault;

		Drawing.drawing.interfaceSizeX = Drawing.drawing.baseInterfaceSizeX / Drawing.drawing.interfaceScaleZoom;
		Drawing.drawing.interfaceSizeY = Drawing.drawing.baseInterfaceSizeY / Drawing.drawing.interfaceScaleZoom;

		if (!onlinePaused)
			Game.screen.update();
		else
			this.onlineOverlay.update();

		if (Game.game.input.fullscreen.isValid())
		{
			Game.game.input.fullscreen.invalidate();
			Game.game.window.setFullscreen(!Game.game.window.fullscreen);
		}

		if (Game.steamNetworkHandler.initialized)
			Game.steamNetworkHandler.update();

		if (ScreenPartyHost.isServer && ScreenPartyHost.server != null)
		{
			synchronized (ScreenPartyHost.server.connections)
			{
				for (int j = 0; j < ScreenPartyHost.server.connections.size(); j++)
				{
					synchronized (ScreenPartyHost.server.connections.get(j).events)
					{
						ScreenPartyHost.server.connections.get(j).events.addAll(Game.eventsOut);
					}

					ScreenPartyHost.server.connections.get(j).reply();
				}
			}

			Game.eventsOut.clear();
		}

		if (prevScreen != Game.screen)
		{
			Drawing.drawing.interfaceSizeX = Drawing.drawing.baseInterfaceSizeX / Drawing.drawing.interfaceScaleZoom;
			Drawing.drawing.interfaceSizeY = Drawing.drawing.baseInterfaceSizeY / Drawing.drawing.interfaceScaleZoom;

			Panel.selectedTextBox = null;
		}

		if (ScreenPartyLobby.isClient)
		{
			Client.handler.reply();
		}

		if (forceRefreshMusic || (prevScreen != null && prevScreen != Game.screen && Game.screen != null && !Game.stringsEqual(prevScreen.music, Game.screen.music) && !(Game.screen instanceof IOnlineScreen)))
		{
			if (Game.stringsEqual(prevScreen.musicID, Game.screen.musicID))
				this.playScreenMusic(500);
			else
				this.playScreenMusic(0);
		}

		forceRefreshMusic = false;

		if (Game.game.window.validPressedKeys.contains(InputCodes.KEY_F12) && Game.game.window.validPressedKeys.contains(InputCodes.KEY_LEFT_ALT) && Game.debug)
		{
			Game.game.window.validPressedKeys.clear();
			Game.exitToCrash(new Exception("Manually initiated crash"));
		}

		if (!ScreenPartyHost.isServer && !ScreenPartyLobby.isClient)
			Game.eventsOut.clear();
	}

	public void playScreenMusic(long fadeTime)
	{
		if (Game.screen instanceof IOnlineScreen)
			return;

		if (Game.screen.music == null)
			Drawing.drawing.stopMusic();
		else if (Panel.panel.startMusicPlayed)
			Drawing.drawing.playMusic(Game.screen.music, Game.musicVolume, true, Game.screen.musicID, fadeTime);
	}

	public void draw()
	{
		double introTime = 1000;
		double introAnimationTime = 500;

		if (Game.fancyTerrain && Game.enable3d)
			introAnimationTime = 1000;

		if (Game.cinematic)
			introAnimationTime = 4000;

		if (System.currentTimeMillis() - startTime < 0)
		{
			double frac = (startTime - System.currentTimeMillis() * 1.0) / splash_duration;

			double frac2 = Math.min(frac * 4, 1) * Math.min((1 - frac) * 4, 1);

			double[] col = Game.getRainbowColor((System.currentTimeMillis() % (1000)) / 1000.0);

			Drawing.drawing.scale = Math.min(Game.game.window.absoluteWidth / Game.currentSizeX, (Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Game.currentSizeY) / 50.0;
			Drawing.drawing.unzoomedScale = Drawing.drawing.scale;
			Drawing.drawing.scale = Game.screen.getScale();
			Drawing.drawing.interfaceScale = Drawing.drawing.interfaceScaleZoom * Math.min(Game.game.window.absoluteWidth / 28, (Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / 18) / 50.0;
			Game.game.window.absoluteDepth = Drawing.drawing.interfaceScale * Game.absoluteDepthBase;

			Drawing.drawing.setColor(255, 255, 255, 255 * frac2);
			Drawing.drawing.drawInterfaceImage( System.currentTimeMillis() / 2000.0,"opal.png", Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, 600 * (1 + (- frac + 0.5) * 2), 600 * (1 + (- frac + 0.5) * 2));

			Drawing.drawing.setColor(1 * frac2 * col[0], 1 * frac2 * col[1], 1 * frac2 * col[2]);
			Drawing.drawing.setInterfaceFontSize(100 * (1 + (- frac + 0.5) * 0.8));
			Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, "Opal Games :)");
			return;
		}

		if (this.frameStartTime - startTime < introTime + introAnimationTime)
		{
			this.frameStartTime += 100000;
			Drawing.drawing.forceRedrawTerrain();
			double frac = ((this.frameStartTime - startTime - introTime) / introAnimationTime);

			if (Game.enable3d && Game.fancyTerrain)
			{
				zoomTranslation.z = -0.08 * frac;
				Game.game.window.transformations.add(zoomTranslation);
				Game.game.window.loadPerspective();
			}

			Drawing.drawing.setColor(Level.currentColorR, Level.currentColorG, Level.currentColorB);
			Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, Game.game.window.absoluteWidth * 1.2 / Drawing.drawing.interfaceScale, Game.game.window.absoluteHeight * 1.2 / Drawing.drawing.interfaceScale);

			if (!Game.cinematic)
				dummySpin.draw();

			Game.game.window.transformations.clear();
			Game.game.window.loadPerspective();

			Game.game.window.shapeRenderer.setBatchMode(false, false, false, false);

			if (System.currentTimeMillis() - startTime > introTime)
			{
				Game.screen.drawDefaultBackground(frac);
				drawBar(40 - frac * 40);
			}

			Game.game.window.shapeRenderer.setBatchMode(false, false, false, false);
			Game.game.window.shapeRenderer.setBatchMode(false, false, true, false);

			if (Game.screen instanceof ISeparateBackgroundScreen)
			{
				zoomTranslation.z = (1 - frac) * 3;

				Game.game.window.transformations.add(zoomTranslation);
				Game.game.window.loadPerspective();

				((ISeparateBackgroundScreen) Game.screen).drawWithoutBackground();

				Game.game.window.transformations.clear();
				Game.game.window.loadPerspective();
			}

			drawMouseTarget();

			firstDraw = false;

			//A fix to some glitchiness on ios
			Drawing.drawing.setColor(0, 0, 0, 0);
			Drawing.drawing.fillInterfaceRect(0, 0, 0, 0);

			if (!Game.game.window.drawingShadow)
				this.frameStartTime = System.currentTimeMillis();

			return;
		}

		if (Drawing.drawing.terrainRenderer2 == null)
			Drawing.drawing.terrainRenderer2 = new TerrainRenderer();

		if (Drawing.drawing.trackRenderer == null)
			Drawing.drawing.trackRenderer = new TrackRenderer();

		if (!(Game.screen instanceof ScreenGame))
		{
			Drawing.drawing.scale = Math.min(Panel.windowWidth / Game.currentSizeX, (Panel.windowHeight - Drawing.drawing.statsHeight) / Game.currentSizeY) / 50.0;
			Drawing.drawing.unzoomedScale = Drawing.drawing.scale;
			Drawing.drawing.scale = Game.screen.getScale();
			Drawing.drawing.interfaceScale = Drawing.drawing.interfaceScaleZoom * Math.min(Panel.windowWidth / 28, (Panel.windowHeight - Drawing.drawing.statsHeight) / 18) / 50.0;
			Game.game.window.absoluteDepth = Drawing.drawing.interfaceScale * Game.absoluteDepthBase;
		}

		if (!this.introFinished)
		{
			Drawing.drawing.forceRedrawTerrain();
			this.introFinished = true;
		}

		if (!(Game.screen instanceof ScreenExit))
		{
			if (Game.screen instanceof ScreenGame && Game.currentLevel != null && Game.followingCam)
				Drawing.drawing.setColor(133 * (Level.currentLightIntensity * 0.7), 193 * (Level.currentLightIntensity * 0.7), 233 * (Level.currentLightIntensity * 0.7));
			else
				Drawing.drawing.setColor(174, 92, 16);

			Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale, Game.game.window.absoluteHeight / Drawing.drawing.interfaceScale);
		}

		Drawing.drawing.setLighting(Level.currentLightIntensity, Level.currentShadowIntensity);

		this.lights.clear();

		Game.screen.setupLights();

		Game.game.window.createLights(this.lights, Drawing.drawing.scale);

		if (!Game.game.window.drawingShadow)
		{
			long time = (long) (System.currentTimeMillis() * frameSampling / 1000);
			if (lastFrameSec < time && lastFrameSec != firstFrameSec)
			{
				lastFPS = (int) (frames * 1.0 * frameSampling);
				frames = 0;
			}

			lastFrameSec = time;
			frames++;
			ageFrames++;
		}

		if (onlinePaused)
			this.onlineOverlay.draw();
		else
			Game.screen.draw();

		if (Game.screen instanceof ScreenGame)
		{
			for (IFixedMenu menu : ModAPI.menuGroup)
				menu.draw();
		}

		for (Movable m : Game.movables)
		{
			if (m instanceof TankNPC && ((TankNPC) m).draw)
				((TankNPC) m).drawMessage();
		}

		ScreenOverlayChat.draw(!(Game.screen instanceof IHiddenChatboxScreen));

		if (!(Game.screen instanceof ScreenExit))
			this.drawBar();

		if (Game.screen.showDefaultMouse)
			this.drawMouseTarget();

		Drawing.drawing.setColor(255, 255, 255);
		if (screenshot)
			Game.game.window.shapeRenderer.drawImage(100, 100, 500, 500, "screenshot", false);

		Drawing.drawing.setColor(0, 0, 0, 0);
		Drawing.drawing.fillInterfaceRect(0, 0, 0, 0);

		Game.screen.drawPostMouse();

		if (!Game.game.window.drawingShadow && (Game.screen instanceof ScreenGame && !(((ScreenGame) Game.screen).paused && !ScreenPartyHost.isServer && !ScreenPartyLobby.isClient)))
			this.age += Panel.frameFrequency;

//		if (!Game.game.window.drawingShadow)
//			Drawing.drawing.terrainRenderer2.draw();
	}

	public void drawMouseTarget()
	{
		drawMouseTarget(false);
	}

	public void drawMouseTarget(boolean force)
	{
		if (Game.game.window.touchscreen)
			return;

		double mx = Drawing.drawing.getInterfaceMouseX();
		double my = Drawing.drawing.getInterfaceMouseY();

		if (showMouseTarget || force)
		{
			if (Level.isDark())
			{
				if (Game.glowEnabled)
				{
					Drawing.drawing.setColor(0, 0, 0, 128);
					Drawing.drawing.fillInterfaceGlow(mx, my, 64, 64, true);
				}

				Drawing.drawing.setColor(255, 255, 255);
			}
			else
			{
				if (Game.glowEnabled)
				{
					Drawing.drawing.setColor(255, 255, 255, 128);
					Drawing.drawing.fillInterfaceGlow(mx, my, 64, 64);
				}

				Drawing.drawing.setColor(0, 0, 0);
			}

			Drawing.drawing.drawInterfaceImage("cursor.png", mx, my, 48, 48);
		}

		if (Game.enable3d && ((Game.screen instanceof ScreenGame && !((ScreenGame) Game.screen).paused && !((ScreenGame) Game.screen).shopScreen && Game.playerTank != null) || Game.screen instanceof ScreenLevelEditor) && Panel.showMouseTargetHeight)
		{
			double c = 127 * Obstacle.draw_size / Game.tile_size;

			double r = c;
			double g = c;
			double b = c;
			double a = 255;

			double r2 = 0;
			double g2 = 0;
			double b2 = 0;
			double a2 = 0;

			Drawing.drawing.setColor(r, g, b, a, 1);
			Game.game.window.shapeRenderer.setBatchMode(true, false, true, true, false);

			double size = 12 * Drawing.drawing.interfaceScale / Drawing.drawing.scale;
			double height = 100;
			double thickness = 2;

			double x = Drawing.drawing.toGameCoordsX(mx);
			double y = Drawing.drawing.toGameCoordsY(my);

			Game.game.window.shapeRenderer.setBatchMode(false, false, true, true, false);
			Game.game.window.shapeRenderer.setBatchMode(true, true, true, true, false);


			Drawing.drawing.setColor(r2, g2, b2, a2, 1);
			Drawing.drawing.addVertex(x - size, y - thickness, 0);
			Drawing.drawing.setColor(r, g, b, a, 1);
			Drawing.drawing.addVertex(x, y - thickness, 0);
			Drawing.drawing.addVertex(x, y, height);

			Drawing.drawing.setColor(r2, g2, b2, a2, 1);
			Drawing.drawing.addVertex(x + size, y - thickness, 0);
			Drawing.drawing.setColor(r, g, b, a, 1);
			Drawing.drawing.addVertex(x, y - thickness, 0);
			Drawing.drawing.addVertex(x, y, height);

			Drawing.drawing.setColor(r2, g2, b2, a2, 1);
			Drawing.drawing.addVertex(x - size, y + thickness, 0);
			Drawing.drawing.setColor(r, g, b, a, 1);
			Drawing.drawing.addVertex(x, y - thickness, 0);
			Drawing.drawing.addVertex(x, y, height);

			Drawing.drawing.setColor(r2, g2, b2, a2, 1);
			Drawing.drawing.addVertex(x + size, y + thickness, 0);
			Drawing.drawing.setColor(r, g, b, a, 1);
			Drawing.drawing.addVertex(x, y + thickness, 0);
			Drawing.drawing.addVertex(x, y, height);

			Drawing.drawing.setColor(r2, g2, b2, a2, 1);
			Drawing.drawing.addVertex(x - size, y - thickness, 0);
			Drawing.drawing.addVertex(x - size, y + thickness, 0);
			Drawing.drawing.setColor(r, g, b, a, 1);
			Drawing.drawing.addVertex(x, y, height);

			Drawing.drawing.setColor(r2, g2, b2, a2, 1);
			Drawing.drawing.addVertex(x + size, y - thickness, 0);
			Drawing.drawing.addVertex(x + size, y + thickness, 0);
			Drawing.drawing.setColor(r, g, b, a, 1);
			Drawing.drawing.addVertex(x, y, height);

			Drawing.drawing.setColor(r2, g2, b2, a2, 1);
			Drawing.drawing.addVertex(x - thickness, y - size, 0);
			Drawing.drawing.setColor(r, g, b, a, 1);
			Drawing.drawing.addVertex(x - thickness, y, 0);
			Drawing.drawing.addVertex(x, y, height);

			Drawing.drawing.setColor(r2, g2, b2, a2, 1);
			Drawing.drawing.addVertex(x - thickness, y + size, 0);
			Drawing.drawing.setColor(r, g, b, a, 1);
			Drawing.drawing.addVertex(x - thickness, y, 0);
			Drawing.drawing.addVertex(x, y, height);

			Drawing.drawing.setColor(r2, g2, b2, a2, 1);
			Drawing.drawing.addVertex(x + thickness, y - size, 0);
			Drawing.drawing.setColor(r, g, b, a, 1);
			Drawing.drawing.addVertex(x + thickness, y, 0);
			Drawing.drawing.addVertex(x, y, height);

			Drawing.drawing.setColor(r2, g2, b2, a2, 1);
			Drawing.drawing.addVertex(x + thickness, y + size, 0);
			Drawing.drawing.setColor(r, g, b, a, 1);
			Drawing.drawing.addVertex(x + thickness, y, 0);
			Drawing.drawing.addVertex(x, y, height);

			Drawing.drawing.setColor(r2, g2, b2, a2, 1);
			Drawing.drawing.addVertex(x - thickness, y - size, 0);
			Drawing.drawing.addVertex(x + thickness, y - size, 0);
			Drawing.drawing.setColor(r, g, b, a, 1);
			Drawing.drawing.addVertex(x, y, height);

			Drawing.drawing.setColor(r2, g2, b2, a2, 1);
			Drawing.drawing.addVertex(x - thickness, y + size, 0);
			Drawing.drawing.addVertex(x + thickness, y + size, 0);
			Drawing.drawing.setColor(r, g, b, a, 1);
			Drawing.drawing.addVertex(x, y, height);

			double res = 40;
			double height2 = height * 0.75;
			for (int i = 0; i < res; i++)
			{
				Drawing.drawing.setColor(r, g, b, a, 1);
				double x1 = Math.cos(i / res * Math.PI * 2) * size;
				double x2 = Math.cos((i + 1) / res * Math.PI * 2) * size;
				double y1 = Math.sin(i / res * Math.PI * 2) * size;
				double y2 = Math.sin((i + 1) / res * Math.PI * 2) * size;

				Drawing.drawing.addVertex(x + x1, y + y1, 0);
				Drawing.drawing.addVertex(x + x2, y + y2, 0);

				Drawing.drawing.setColor(r2, g2, b2, a2, 1);
				Drawing.drawing.addVertex(x + x2, y + y2, height2);
				Drawing.drawing.addVertex(x + x1, y + y1, height2);
			}

			Game.game.window.shapeRenderer.setBatchMode(false, true, false);
		}
	}

	public void drawBar()
	{
		drawBar(0);
	}

	public void drawBar(double offset)
	{
		if (!Drawing.drawing.enableStats)
			return;

		Drawing.drawing.setColor(87, 46, 8);
		Game.game.window.shapeRenderer.fillRect(0, offset + (int) (Panel.windowHeight - 40), (int) (Panel.windowWidth), 40);

		Drawing.drawing.setColor(255, 227, 186);

		Drawing.drawing.setInterfaceFontSize(12);

		double boundary = Game.game.window.getEdgeBounds();

		if (Game.framework == Game.Framework.libgdx)
			boundary += 40;

		Game.game.window.fontRenderer.drawString(boundary + 2, offset + (int) (Panel.windowHeight - 40 + 6), 0.4, 0.4, Game.version);
		Game.game.window.fontRenderer.drawString(boundary + 2, offset + (int) (Panel.windowHeight - 40 + 22), 0.4, 0.4, "FPS: " + lastFPS);

		Game.game.window.fontRenderer.drawString(boundary + 600, offset + (int) (Panel.windowHeight - 40 + 10), 0.6, 0.6, Game.screen.screenHint);

		long free = Runtime.getRuntime().freeMemory();
		long total = Runtime.getRuntime().totalMemory();
		long used = total - free;

		Game.game.window.fontRenderer.drawString(boundary + 150, offset + (int) (Panel.windowHeight - 40 + 22), 0.4, 0.4, "Memory used: " +  used / 1048576 + "/" + total / 1048576 + "MB");

		if (ScreenPartyLobby.isClient && !Game.connectedToOnline)
		{
			double[] col = getLatencyColor(ClientHandler.lastLatencyAverage);
			Drawing.drawing.setColor(col[0], col[1], col[2]);
			Game.game.window.fontRenderer.drawString(boundary + 150, offset + (int) (Panel.windowHeight - 40 + 6), 0.4, 0.4, "Latency: " + ClientHandler.lastLatencyAverage + "ms");
		}

		if (ScreenPartyLobby.isClient || ScreenPartyHost.isServer)
		{
			Drawing.drawing.setColor(255, 227, 186);
			Game.game.window.fontRenderer.drawString(boundary + 400, offset + (int) (Panel.windowHeight - 40 + 6), 0.4, 0.4, "Upstream: " + MessageReader.upstreamBytesPerSec / 1024 + "KB/s");
			Game.game.window.fontRenderer.drawString(boundary + 400, offset + (int) (Panel.windowHeight - 40 + 22), 0.4, 0.4, "Downstream: " + MessageReader.downstreamBytesPerSec / 1024 + "KB/s");
		}
	}

	public double[] getLatencyColor(long l)
	{
		double[] col = new double[3];

		if (l <= 40)
		{
			col[1] = 255;
			col[2] = 255 * (1 - l / 40.0);
		}
		else if (l <= 80)
		{
			col[0] = 255 * ((l - 40) / 40.0);
			col[1] = 255;
		}
		else if (l <= 160)
		{
			col[0] = 255;
			col[1] = 255 * (1 - (l - 80) / 80.0);
		}
		else
			col[0] = 255;

		return col;
	}
}