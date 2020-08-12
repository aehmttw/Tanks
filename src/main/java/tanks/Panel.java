package tanks;

import basewindow.BaseFile;
import basewindow.InputCodes;
import tanks.event.EventBeginLevelCountdown;
import tanks.event.online.IOnlineServerEvent;
import tanks.gui.TextBox;
import tanks.gui.screen.*;
import tanks.hotbar.Hotbar;
import tanks.network.Client;
import tanks.network.ClientHandler;
import tanks.tank.*;

public class Panel
{
	public static boolean onlinePaused;

	public double zoomTimer = 0;

	public static double windowWidth = 1400;
	public static double windowHeight = 900;

	public static boolean showMouseTarget = true;

	public static Panel panel;

	public static boolean forceRefreshMusic;

	public static String winlose = "";
	public static boolean win = false;
	public static boolean levelPassed = false;

	public static double darkness = 0;

	public static TextBox selectedTextBox;

	/** Important value used in calculating game speed. Larger values are set when the frames are lower, and game speed is increased to compensate.*/
	public static double frameFrequency = 1;

	//ArrayList<Double> frameFrequencies = new ArrayList<Double>();

	public int frames = 0;

	public double frameSampling = 1;

	public long firstFrameSec = (long) (System.currentTimeMillis() / 1000.0 * frameSampling);
	public long lastFrameSec = (long) (System.currentTimeMillis() / 1000.0 * frameSampling);

	public long startTime = System.currentTimeMillis();

	public int lastFPS = 0;

	public ScreenOverlayOnline onlineOverlay;

	protected static boolean initialized = false;

	public Tank dummySpin = new TankDummyLoadingScreen(Drawing.drawing.sizeX / 2, Drawing.drawing.sizeY / 2);

	public boolean firstFrame = true;
	public boolean firstDraw = true;

	public boolean startMusicPlayed = false;

	public long introMusicEnd;

	public static void initialize()
	{
		if (!initialized)
			panel = new Panel();

		initialized = true;
	}

	private Panel()
	{

	}

	public void update()
	{
		if (firstFrame)
		{
			boolean tutorial = false;

			Game.game.window.setIcon("/images/icon.png");

			double scale = 1;
			if (Game.game.window.touchscreen && Game.game.window.pointHeight > 0 && Game.game.window.pointHeight <= 500)
				scale = 1.25;

			Drawing.drawing.setInterfaceScaleZoom(scale);
			TankPlayer.setShootStick(TankPlayer.shootStickEnabled);
			TankPlayer.controlStick.mobile = TankPlayer.controlStickMobile;
			TankPlayer.controlStick.snap = TankPlayer.controlStickSnap;

			Hotbar.toggle.posX = Drawing.drawing.interfaceSizeX / 2;
			Hotbar.toggle.posY = Drawing.drawing.interfaceSizeY - 20;

			if (Game.usernameInvalid(Game.player.username))
				Game.screen = new ScreenUsernameInvalid();
			else
				Game.screen = new ScreenTitle();

			BaseFile tutorialFile = Game.game.fileManager.getFile(Game.homedir + Game.tutorialPath);
			if (!tutorialFile.exists())
			{
				tutorial = true;
				Game.silentCleanUp();
				new Tutorial().loadTutorial(true, Game.game.window.touchscreen);
			}

			if (Game.game.window.soundsEnabled)
			{
				Game.game.window.soundPlayer.musicPlaying = true;

				for (int i = 1; i <= 4; i++)
				{
					Game.game.window.soundPlayer.registerCombinedMusic("/music/tomato_feast_" + i + ".ogg", "menu");
				}

				for (int i = 1; i <= 2; i++)
				{
					Game.game.window.soundPlayer.registerCombinedMusic("/music/ready_music_" + i + ".ogg", "ready");
				}
			}

			introMusicEnd = System.currentTimeMillis() + Long.parseLong(Game.game.fileManager.getInternalFileContents("/music/intro_length.txt").get(0));

			introMusicEnd -= 30;

			if (Game.framework == Game.Framework.libgdx)
				introMusicEnd -= 70;

			if (!tutorial)
				Drawing.drawing.playMusic("tomato_feast_0.ogg", Game.musicVolume, false, "intro", 0, false);
		}

		firstFrame = false;

		Screen prevScreen = Game.screen;

		if (!startMusicPlayed && Game.game.window.soundsEnabled && System.currentTimeMillis() > introMusicEnd)
		{
			startMusicPlayed = true;
			this.playScreenMusic(0);
		}

		Panel.windowWidth = Game.game.window.absoluteWidth;
		Panel.windowHeight = Game.game.window.absoluteHeight;

		Drawing.drawing.scale = Math.min(Panel.windowWidth * 1.0 / Game.currentSizeX, (Panel.windowHeight * 1.0 - Drawing.drawing.statsHeight) / Game.currentSizeY) / 50.0;
		Drawing.drawing.interfaceScale = Drawing.drawing.interfaceScaleZoom * Math.min(Panel.windowWidth * 1.0 / 28, (Panel.windowHeight * 1.0 - Drawing.drawing.statsHeight) / 18) / 50.0;
		Game.game.window.absoluteDepth = Drawing.drawing.interfaceScale * Game.absoluteDepthBase;

		Drawing.drawing.unzoomedScale = Drawing.drawing.scale;

		Panel.frameFrequency = Game.game.window.frameFrequency;

		Game.game.window.showKeyboard = false;

		double introTime = 1000;
		double introAnimationTime = 500;

		if (Game.fancyGraphics && Game.enable3d)
			introAnimationTime = 1000;

		if (System.currentTimeMillis() - startTime < introTime + introAnimationTime)
		{
			dummySpin.posX = Drawing.drawing.sizeX / 2;
			dummySpin.posY = Drawing.drawing.sizeY / 2;
			dummySpin.angle = Math.PI * 2 * (System.currentTimeMillis() - startTime) / (introTime + introAnimationTime);
			return;
		}

		synchronized(Game.eventsIn)
		{
			for (int i = 0; i < Game.eventsIn.size(); i++)
			{
				if (!(Game.eventsIn.get(i) instanceof IOnlineServerEvent))
					Game.eventsIn.get(i).execute();
			}

			Game.eventsIn.clear();
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

					for (Player p: Game.players)
					{
						if (p.clientID.equals(ScreenPartyHost.disconnectedPlayers.get(i)))
						{
							ScreenPartyHost.readyPlayers.remove(p);

							if (Crusade.currentCrusade != null)
							{
								Crusade.currentCrusade.crusadeCoins.remove(p);
								Crusade.currentCrusade.crusadeItembars.remove(p);
							}
						}
					}

					Game.removePlayer(ScreenPartyHost.disconnectedPlayers.get(i));
				}

				if (ScreenPartyHost.readyPlayers.size() >= ScreenPartyHost.includedPlayers.size() && Game.screen instanceof ScreenGame)
				{
					Game.eventsOut.add(new EventBeginLevelCountdown());
					((ScreenGame) Game.screen).cancelCountdown = false;
				}

				ScreenPartyHost.disconnectedPlayers.clear();
			}
		}

		if (Game.player.hotbar.coins < 0)
			Game.player.hotbar.coins = 0;

		this.zoomTimer -= 0.02 * Panel.frameFrequency;

		if (Game.playerTank != null && !Game.playerTank.destroy && !ScreenGame.finished && Drawing.drawing.unzoomedScale < Drawing.drawing.interfaceScale
				&& Game.screen instanceof ScreenGame && ((ScreenGame) (Game.screen)).playing)
		{
			Drawing.drawing.enableMovingCamera = Drawing.drawing.unzoomedScale < Drawing.drawing.interfaceScale;

			Drawing.drawing.playerX = Game.playerTank.posX;
			Drawing.drawing.playerY = Game.playerTank.posY;

			if (Game.playerTank instanceof TankPlayerController)
			{
				Drawing.drawing.playerX = ((TankPlayerController) Game.playerTank).interpolatedPosX;
				Drawing.drawing.playerY = ((TankPlayerController) Game.playerTank).interpolatedPosY;
			}

			if (Drawing.drawing.movingCamera)
			{
				this.zoomTimer += 0.04 * Panel.frameFrequency;
				//Drawing.drawing.scale = Drawing.drawing.interfaceScale;
			}
		}
		else
		{
			Drawing.drawing.enableMovingCamera = false;
		}

		this.zoomTimer = Math.min(Math.max(this.zoomTimer, 0), 1);

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

		if (!onlinePaused)
			Game.screen.update();
		else
			this.onlineOverlay.update();

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
			Panel.selectedTextBox = null;

		if (ScreenPartyLobby.isClient)
		{
			Client.handler.reply();
		}

		if (forceRefreshMusic || (prevScreen != Game.screen && !Game.stringsEqual(prevScreen.music, Game.screen.music) && !(Game.screen instanceof IOnlineScreen)))
		{
			if (Game.stringsEqual(prevScreen.musicID, Game.screen.musicID))
				this.playScreenMusic(500);
			else
				this.playScreenMusic(0);
		}

		forceRefreshMusic = false;
	}

	public void playScreenMusic(long fadeTime)
	{
		if (Game.screen.music == null)
			Drawing.drawing.stopMusic();
		else if (Panel.panel.startMusicPlayed)
			Drawing.drawing.playMusic(Game.screen.music, Game.musicVolume, true, Game.screen.musicID, fadeTime);
	}

	public void draw()
	{
		double introTime = 1000;
		double introAnimationTime = 500;

		if (Game.fancyGraphics && Game.enable3d)
			introAnimationTime = 1000;

		/*for (int i = 0; i < Game.tilesR.length; i++)
		{
			for (int j = 0; j < Game.tilesR[i].length; j++)
			{
				double[] col = Game.getRainbowColor((((i + j + Game.tilesDepth[i][j]) * 70 + System.currentTimeMillis()) / 10000.0) % 1);
				Game.tilesR[i][j] = col[0];
				Game.tilesG[i][j] = col[1];
				Game.tilesB[i][j] = col[2];
			}
		}*/

		if (System.currentTimeMillis() - startTime < introTime + introAnimationTime)
		{
			double frac = ((System.currentTimeMillis() - startTime - introTime) / introAnimationTime);

			Drawing.drawing.setColor(Level.currentColorR, Level.currentColorG, Level.currentColorB);
			Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, Game.game.window.absoluteWidth * 1.2 / Drawing.drawing.interfaceScale, Game.game.window.absoluteHeight * 1.2 / Drawing.drawing.interfaceScale);

			/*Drawing.drawing.setColor(255, 255, 255);
			Drawing.drawing.drawInterfaceImage("/tanks//loading.png", Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, Drawing.drawing.interfaceSizeX, Drawing.drawing.interfaceSizeY);
			*/

			if (System.currentTimeMillis() - startTime > introTime)
			{
				Game.screen.drawDefaultBackground(frac);
				drawBar(40 - frac * 40);
			}

			dummySpin.draw();
			drawMouseTarget();

			firstDraw = false;

			return;
		}

		if (!(Game.screen instanceof ScreenExit))
		{
			Drawing.drawing.setColor(174, 92, 16);
			Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale, Game.game.window.absoluteHeight / Drawing.drawing.interfaceScale);
		}

		long time = (long) (System.currentTimeMillis() * frameSampling / 1000);
		if (lastFrameSec < time && lastFrameSec != firstFrameSec)
		{
			lastFPS = (int) (frames * 1.0 * frameSampling);
			frames = 0;
		}


		lastFrameSec = time;
		frames++;

		//g.setColor(new Color(255, 227, 186));
		//g.fillRect(0, 0, (int) (Screen.sizeX * Screen.scale), (int) (Screen.sizeY * Screen.scale));


		if (onlinePaused)
			this.onlineOverlay.draw();
		else
			Game.screen.draw();

		if (!(Game.screen instanceof ScreenExit))
			this.drawBar();

		if (Game.screen.showDefaultMouse)
			this.drawMouseTarget();

		Game.screen.drawPostMouse();
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

		//double mx2 = Drawing.drawing.getMouseX();
		//double my2 = Drawing.drawing.getMouseY();

		if (showMouseTarget || force)
		{
			if (Game.superGraphics)
			{
				Drawing.drawing.setColor(255, 255, 255, 128);
				Drawing.drawing.fillInterfaceGlow(mx, my, 64, 64);
			}

			Drawing.drawing.setColor(0, 0, 0);
			/*Drawing.drawing.drawInterfaceOval(mx, my, 8, 8);
			Drawing.drawing.drawInterfaceOval(mx, my, 4, 4);*/

			Drawing.drawing.drawInterfaceImage("cursor.png", mx, my, 48, 48);

			//Drawing.drawing.setColor(255, 0, 0);
			//Drawing.drawing.drawOval(mx2, my2, 8, 8);
			//Drawing.drawing.drawOval(mx2, my2, 4, 4);
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
		Game.game.window.fillRect(0, offset + (int) (Panel.windowHeight - 40), (int) (Panel.windowWidth), 40);

		Drawing.drawing.setColor(255, 227, 186);

		Drawing.drawing.setFontSize(12);

		double boundary = Game.game.window.getEdgeBounds();

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