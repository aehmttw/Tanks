package tanks;

import java.util.ArrayList;

import javax.swing.Timer;

import tanks.event.IEvent;
import tanks.event.INetworkEvent;

public class Panel
{
	Timer timer;
	int height = Drawing.drawing.sizeY;
	int width = Drawing.drawing.sizeX;
	boolean resize = true;
	double zoomTimer = 0;

	public static double windowWidth = 1400;
	public static double windowHeight = 900;

	public static double restrictedWindowMouseOffsetX = 0;
	public static double restrictedWindowMouseOffsetY = 0;


	static boolean showMouseTarget = true;

	ArrayList<Long> framesList = new ArrayList<Long>();

	public static Panel panel;

	public static String winlose = "";
	public static boolean win = false;

	public static double darkness = 0;

	/** Important value used in calculating game speed. Larger values are set when the frames are lower, and game speed is increased to compensate.*/
	public static double frameFrequency = 1;

	public Hotbar hotbar = new Hotbar(); 

	//ArrayList<Double> frameFrequencies = new ArrayList<Double>();

	int frames = 0;

	double frameSampling = 1;

	long lastFrame = System.currentTimeMillis(); 

	long firstFrameSec = (long) (System.currentTimeMillis() / 1000.0 * frameSampling);
	long lastFrameSec = (long) (System.currentTimeMillis() / 1000.0 * frameSampling);

	long startTime = System.currentTimeMillis();

	int lastFPS = 0;

	public static boolean pausePressed = false;
	protected static boolean initialized = false;

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

		if (Panel.panel.hotbar.currentCoins.coins < 0)
			Panel.panel.hotbar.currentCoins.coins = 0;

		Panel.windowWidth = Game.game.window.absoluteWidth;
		Panel.windowHeight = Game.game.window.absoluteHeight;

		Drawing.drawing.scale = Math.min(Panel.windowWidth * 1.0 / Game.currentSizeX, (Panel.windowHeight * 1.0 - 40) / Game.currentSizeY) / 50.0;
		Drawing.drawing.interfaceScale = Math.min(Panel.windowWidth * 1.0 / 28, (Panel.windowHeight * 1.0 - 40) / 18) / 50.0;

		Drawing.drawing.unzoomedScale = Drawing.drawing.scale;

		this.zoomTimer -= 0.02 * Panel.frameFrequency;

		if (Game.player != null && Game.screen instanceof ScreenGame && !ScreenGame.finished && Drawing.drawing.unzoomedScale < Drawing.drawing.interfaceScale 
				&& Game.screen instanceof ScreenGame && ((ScreenGame)(Game.screen)).playing)
		{
			Drawing.drawing.enableMovingCamera = true;

			if (Drawing.drawing.movingCamera)
			{
				Drawing.drawing.playerX = Game.player.posX;
				Drawing.drawing.playerY = Game.player.posY;
				this.zoomTimer += 0.04 * Panel.frameFrequency;

				if (Drawing.drawing.unzoomedScale < Drawing.drawing.interfaceScale)
				{
					Drawing.drawing.enableMovingCamera = true;
					//Drawing.drawing.scale = Drawing.drawing.interfaceScale;
				}
				else
				{
					Drawing.drawing.enableMovingCamera = false;
				}
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
			Panel.restrictedWindowMouseOffsetX = 0;
		}

		if (Panel.windowHeight - 40 > Game.currentSizeY * Game.tank_size * Drawing.drawing.scale)
			Drawing.drawing.enableMovingCameraY = false;
		else
		{
			Drawing.drawing.enableMovingCameraY = true;
			Panel.restrictedWindowMouseOffsetY = 0;
		}

		Game.screen.update();

		if (ScreenPartyHost.isServer && ScreenPartyHost.server != null)
		{
			for (int i = 0; i < Game.events.size(); i++)
			{
				//synchronized(ScreenPartyHost.server.connections)
				{
					for (int j = 0; j < ScreenPartyHost.server.connections.size(); j++)
					{
						IEvent e = Game.events.get(i);

						if (e instanceof INetworkEvent)
							ScreenPartyHost.server.connections.get(j).events.add((INetworkEvent) e);
					}
				}
			}

			Game.events.clear();
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
			Drawing.drawing.setColor(255, 255, 255);
			Drawing.drawing.drawInterfaceImage("/tanks/resources/loading.png", Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, Drawing.drawing.interfaceSizeX, Drawing.drawing.interfaceSizeY);

			if (System.currentTimeMillis() - startTime > introTime)
			{
				Game.screen.drawDefaultBackground((System.currentTimeMillis() - startTime - introTime) / introAnimationTime);
				drawBar(40 - ((System.currentTimeMillis() - startTime - introTime) / introAnimationTime) * 40);
			}

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

		drawBar();

		double mx = Drawing.drawing.getInterfaceMouseX();
		double my = Drawing.drawing.getInterfaceMouseY();

		//double mx2 = Drawing.drawing.getMouseX();
		//double my2 = Drawing.drawing.getMouseY();

		if (showMouseTarget)
		{
			Drawing.drawing.setColor(0, 0, 0);
			Drawing.drawing.drawInterfaceOval(mx, my, 8, 8);
			Drawing.drawing.drawInterfaceOval(mx, my, 4, 4);

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
		Drawing.drawing.setColor(87, 46, 8);
		Game.game.window.fillRect(0, offset + (int) (Panel.windowHeight - 40), (int) (Panel.windowWidth), 40);

		Drawing.drawing.setColor(255, 227, 186);

		Drawing.drawing.setFontSize(12);

		Game.game.window.fontRenderer.drawString(2, offset + (int) (Panel.windowHeight - 40 + 6), 0.4, 0.4, Game.version);
		Game.game.window.fontRenderer.drawString(2, offset + (int) (Panel.windowHeight - 40 + 22), 0.4, 0.4, "FPS: " + lastFPS);

		Game.game.window.fontRenderer.drawString(600, offset + (int) (Panel.windowHeight - 40 + 10), 0.6, 0.6, Game.screen.screenHint);
	}
}