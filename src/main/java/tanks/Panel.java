package tanks;

import tanks.event.INetworkEvent;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenParty;
import tanks.gui.screen.ScreenPartyHost;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.hotbar.Coins;
import tanks.hotbar.Hotbar;
import tanks.network.ClientHandler;
import tanks.tank.Tank;
import tanks.tank.TankDummyLoadingScreen;
import tanks.tank.TankPlayerRemote;

public class Panel
{
	double zoomTimer = 0;

	public static double windowWidth = 1400;
	public static double windowHeight = 900;

	public static boolean showMouseTarget = true;

	public static Panel panel;

	public static String winlose = "";
	public static boolean win = false;
	public static boolean levelPassed = false;

	public static double darkness = 0;

	/** Important value used in calculating game speed. Larger values are set when the frames are lower, and game speed is increased to compensate.*/
	public static double frameFrequency = 1;

	public Hotbar hotbar = new Hotbar(); 

	//ArrayList<Double> frameFrequencies = new ArrayList<Double>();

	public int frames = 0;

	public double frameSampling = 1;

	public long firstFrameSec = (long) (System.currentTimeMillis() / 1000.0 * frameSampling);
	public long lastFrameSec = (long) (System.currentTimeMillis() / 1000.0 * frameSampling);

	public long startTime = System.currentTimeMillis();

	public int lastFPS = 0;

	protected static boolean initialized = false;

	public Tank dummySpin = new TankDummyLoadingScreen(Drawing.drawing.sizeX / 2, Drawing.drawing.sizeY / 2);

	public static void initialize()
	{
		if (!initialized)
			panel = new Panel();

		initialized = true;
	}

	private Panel()
	{		
		this.hotbar.enabledItemBar = false;
		this.hotbar.currentCoins = new Coins();
	}

	public void update()
	{
		Panel.frameFrequency = Game.game.window.frameFrequency;

		double introTime = 1000;
		double introAnimationTime = 500;

		if (Game.fancyGraphics && Game.enable3d)
			introAnimationTime = 1000;

		if (System.currentTimeMillis() - startTime < introTime + introAnimationTime) 
		{
			dummySpin.angle += 0.02 * frameFrequency;
			return;
		}

		synchronized(Game.eventsIn)
		{
			for (int i = 0; i < Game.eventsIn.size(); i++)
			{
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
							((TankPlayerRemote) m).lives = 0;
					}

					ScreenPartyHost.includedPlayers.remove(ScreenPartyHost.disconnectedPlayers.get(i));
					Game.removePlayer(ScreenPartyHost.disconnectedPlayers.get(i));
				}

				ScreenPartyHost.disconnectedPlayers.clear();
			}
		}

		if (Panel.panel.hotbar.currentCoins.coins < 0)
			Panel.panel.hotbar.currentCoins.coins = 0;

		Panel.windowWidth = Game.game.window.absoluteWidth;
		Panel.windowHeight = Game.game.window.absoluteHeight;

		Drawing.drawing.scale = Math.min(Panel.windowWidth * 1.0 / Game.currentSizeX, (Panel.windowHeight * 1.0 - Drawing.drawing.statsHeight) / Game.currentSizeY) / 50.0;
		Drawing.drawing.interfaceScale = Math.min(Panel.windowWidth * 1.0 / 28, (Panel.windowHeight * 1.0 - Drawing.drawing.statsHeight) / 18) / 50.0;
		Game.game.window.absoluteDepth = Drawing.drawing.interfaceScale * Game.absoluteDepthBase;

		Drawing.drawing.unzoomedScale = Drawing.drawing.scale;

		this.zoomTimer -= 0.02 * Panel.frameFrequency;

		if (Game.playerTank != null && !ScreenGame.finished && Drawing.drawing.unzoomedScale < Drawing.drawing.interfaceScale
				&& Game.screen instanceof ScreenGame && ((ScreenGame) (Game.screen)).playing)
		{
			Drawing.drawing.enableMovingCamera = true;

			if (Drawing.drawing.movingCamera)
			{
				Drawing.drawing.playerX = Game.playerTank.posX;
				Drawing.drawing.playerY = Game.playerTank.posY;
				this.zoomTimer += 0.04 * Panel.frameFrequency;

				//Drawing.drawing.scale = Drawing.drawing.interfaceScale;
				Drawing.drawing.enableMovingCamera = Drawing.drawing.unzoomedScale < Drawing.drawing.interfaceScale;
			}
		}
		else
		{
			Drawing.drawing.enableMovingCamera = false;
		}

		this.zoomTimer = Math.min(Math.max(this.zoomTimer, 0), 1);

		Drawing.drawing.scale = Drawing.drawing.scale * (1 - zoomTimer) + Drawing.drawing.interfaceScale * zoomTimer; 

		if (Panel.windowWidth > Game.currentSizeX * Game.tank_size * Drawing.drawing.scale)
			Drawing.drawing.enableMovingCameraX = false;
		else
		{
			Drawing.drawing.enableMovingCameraX = true;
		}

		if (Panel.windowHeight - Drawing.drawing.statsHeight > Game.currentSizeY * Game.tank_size * Drawing.drawing.scale)
			Drawing.drawing.enableMovingCameraY = false;
		else
		{
			Drawing.drawing.enableMovingCameraY = true;
		}

		Game.screen.update();

		if (ScreenPartyHost.isServer && ScreenPartyHost.server != null)
		{
			for (int i = 0; i < Game.eventsOut.size(); i++)
			{
				//synchronized(ScreenPartyHost.server.connections)
				{
					for (int j = 0; j < ScreenPartyHost.server.connections.size(); j++)
					{
						INetworkEvent e = Game.eventsOut.get(i);

						ScreenPartyHost.server.connections.get(j).events.add(e);
					}
				}
			}

			Game.eventsOut.clear();
		}

		//long end = System.nanoTime();
		//System.out.println("Updating took: " + (end - start));
		//System.out.println(Game.effects.size());
		//System.out.println(Game.recycleEffects.size());

		//repaint();
	}

	public void draw()
	{	
		double introTime = 1000;
		double introAnimationTime = 500;
		
		if (Game.fancyGraphics && Game.enable3d)
			introAnimationTime = 1000;
		
		if (System.currentTimeMillis() - startTime < introTime + introAnimationTime)
		{	
			Drawing.drawing.setColor(Level.currentColorR, Level.currentColorG, Level.currentColorB);
			Drawing.drawing.fillInterfaceRect(Drawing.drawing.sizeX / 2, Drawing.drawing.sizeY / 2, Drawing.drawing.sizeX * 1.2, Drawing.drawing.sizeY * 1.2);	
			
			/*Drawing.drawing.setColor(255, 255, 255);
			Drawing.drawing.drawInterfaceImage("/tanks//loading.png", Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, Drawing.drawing.interfaceSizeX, Drawing.drawing.interfaceSizeY);
			*/
			
			if (System.currentTimeMillis() - startTime > introTime)
			{
				Game.screen.drawDefaultBackground((System.currentTimeMillis() - startTime - introTime) / introAnimationTime);
				drawBar(80 - ((System.currentTimeMillis() - startTime - introTime) / introAnimationTime) * 40);
			}
			
			dummySpin.draw();
			drawMouseTarget();
			return;
		}

		Drawing.drawing.setColor(174, 92, 16);
		Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, Drawing.drawing.interfaceSizeX * 4, Drawing.drawing.interfaceSizeY * 4);				

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

		Game.screen.draw();

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
		double mx = Drawing.drawing.getInterfaceMouseX();
		double my = Drawing.drawing.getInterfaceMouseY();

		//double mx2 = Drawing.drawing.getMouseX();
		//double my2 = Drawing.drawing.getMouseY();

		if (showMouseTarget || force)
		{
			Drawing.drawing.setColor(0, 0, 0);
			/*Drawing.drawing.drawInterfaceOval(mx, my, 8, 8);
			Drawing.drawing.drawInterfaceOval(mx, my, 4, 4);*/

			Drawing.drawing.drawInterfaceImage("/cursor.png", mx, my, 48, 48);

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

		Game.game.window.fontRenderer.drawString(2, offset + (int) (Panel.windowHeight - 40 + 6), 0.4, 0.4, Game.version);
		Game.game.window.fontRenderer.drawString(2, offset + (int) (Panel.windowHeight - 40 + 22), 0.4, 0.4, "FPS: " + lastFPS);

		Game.game.window.fontRenderer.drawString(600, offset + (int) (Panel.windowHeight - 40 + 10), 0.6, 0.6, Game.screen.screenHint);

		long free = Runtime.getRuntime().freeMemory();
		long total = Runtime.getRuntime().totalMemory();
		long used = total - free;

		if (free < 1048576 * 5)
			Drawing.drawing.setColor(255, 127, 0);

		Game.game.window.fontRenderer.drawString(150, offset + (int) (Panel.windowHeight - 40 + 22), 0.4, 0.4, "Memory used: " +  used / 1048576 + "/" + total / 1048576 + "MB");

		if (ScreenPartyLobby.isClient)
		{
			double[] col = getLatencyColor(ClientHandler.lastLatencyAverage);
			Drawing.drawing.setColor(col[0], col[1], col[2]);
			Game.game.window.fontRenderer.drawString(150, offset + (int) (Panel.windowHeight - 40 + 6), 0.4, 0.4, "Latency: " + ClientHandler.lastLatencyAverage + "ms");
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