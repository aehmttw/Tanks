package tanks;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.Timer;

@SuppressWarnings("serial")
public class Panel extends JPanel
{
	Timer timer;
	int height = Window.sizeY;
	int width = Window.sizeX;
	boolean resize = true;

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

	ArrayList<Double> frameFrequencies = new ArrayList<Double>();

	int frames = 0;

	double frameSampling = 1;

	long lastFrame = System.currentTimeMillis();

	long firstFrameSec = (long) (System.currentTimeMillis() / 1000.0 * frameSampling);
	long lastFrameSec = (long) (System.currentTimeMillis() / 1000.0 * frameSampling);

	long startTime = System.currentTimeMillis();

	int lastFPS = 0;

	public static boolean pausePressed = false;

	public Panel()
	{
		Panel.panel = this;

		timer = new Timer(0, new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e) 
			{	
				//long start = System.nanoTime();

				try
				{		
					long milliTime = System.currentTimeMillis();

					framesList.add(milliTime);

					ArrayList<Long> removeList = new ArrayList<Long>();

					for (int i = 0; i < framesList.size(); i++)
					{
						if (milliTime - framesList.get(i) > 1000)
							removeList.add(framesList.get(i));
					}

					for (int i = 0; i < removeList.size(); i++)
					{
						framesList.remove(removeList.get(i));
					}

					if (Game.coins < 0)
						Game.coins = 0;

					Panel.windowWidth = Game.window.getSize().getWidth();
					Panel.windowHeight = Game.window.getSize().getHeight();

					Window.scale = Math.min(Panel.windowWidth * 1.0 / Game.currentSizeX, (Panel.windowHeight * 1.0 - 40 - Window.yOffset) / Game.currentSizeY) / 50.0;
					Window.interfaceScale = Math.min(Panel.windowWidth * 1.0 / 28, (Panel.windowHeight * 1.0 - 40 - Window.yOffset) / 18) / 50.0;

					Window.unzoomedScale = Window.scale;
					
					if (Game.player != null && Game.screen instanceof ScreenGame && !ScreenGame.finished)
					{
						Window.enableMovingCamera = true;

						if (Window.movingCamera)
						{
							Window.playerX = Game.player.posX;
							Window.playerY = Game.player.posY;

							if (Window.scale < Window.interfaceScale)
							{
								Window.enableMovingCamera = true;
								Window.scale = Window.interfaceScale;
							}
							else
							{
								Window.enableMovingCamera = false;
							}
						}
					}
					else
					{
						Window.enableMovingCamera = false;
					}
					
					if (Panel.windowWidth - Window.xOffset > Game.currentSizeX * Game.tank_size * Window.scale)
						Window.enableMovingCameraX = false;
					else
					{
						Window.enableMovingCameraX = true;
						Panel.restrictedWindowMouseOffsetX = 0;
					}
						
					if (Panel.windowHeight - Window.yOffset - 40 > Game.currentSizeY * Game.tank_size * Window.scale)
						Window.enableMovingCameraY = false;
					else
					{
						Window.enableMovingCameraY = true;
						Panel.restrictedWindowMouseOffsetY = 0;
					}
						
					Game.screen.update();
					
					//long end = System.nanoTime();
					//System.out.println("Updating took: " + (end - start));
					//System.out.println(Game.effects.size());
					//System.out.println(Game.recycleEffects.size());

					repaint();

					//frameFrequency = 100.0 / lastFPS;
					//timer.setDelay((int) (frameFrequency * 10));

					//long end = System.nanoTime();
					//System.out.println(end - start);

					//int wait = (int) ((end - start)/1000);
					//timer.setDelay(wait);

					long time = System.currentTimeMillis();
					long lastFrameTime = lastFrame;
					lastFrame = time;

					double freq =  (time - lastFrameTime) / 10.0;
					frameFrequencies.add(freq);

					if (frameFrequencies.size() > 5)
					{
						frameFrequencies.remove(0);
					}

					double totalFrequency = 0;
					for (int i = 0; i < frameFrequencies.size(); i++)
					{
						totalFrequency += frameFrequencies.get(i);
					}

					frameFrequency = totalFrequency / frameFrequencies.size();

					//System.out.println(frameFrequency);
					//frameFrequency = 100.0 / framesList.size();
				}
				catch (Exception exception)
				{
					Game.exitToCrash(exception);
				}

			}

		});
	}

	public void startTimer()
	{
		timer.start();
		new SoundThread().execute();
	}

	/*@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		int verticalCenter = this.getHeight()/2;
		int horizontalCenter = this.getWidth()/2;

		if(!resize)
		{
			int topLeftSquareCornerY = verticalCenter - (height/2);
			int topLeftSquareCornerX = horizontalCenter - (width/2);

			g.setColor(Color.BLUE);
			g.drawRect(topLeftSquareCornerX, topLeftSquareCornerY, width, height);
		}
		else
		{
			g.setColor(Color.MAGENTA);
			g.drawRect(15,15,(this.getWidth() - 30), this.getHeight() - 30);
		}

	}*/

	@Override
	public void paint(Graphics g)
	{		
		//long start = System.nanoTime();		
		try
		{
			if (System.currentTimeMillis() - startTime < 1000)
			{
				for (int i = 0; i < Game.currentSizeX; i++)
				{
					g.setColor(Level.currentColor);
					Window.fillInterfaceRect(g, Window.sizeX / 2, Window.sizeY / 2, Window.sizeX * 1.2, Window.sizeY * 1.2);				
					g.drawImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("resources/loading.png")), 0, 0, null);

				}
				return;
			}

			g.setColor(new Color(174, 92, 16));
			g.fillRect(0, 0, 1 + (int)(Panel.windowWidth), 1+(int)(Panel.windowHeight));

			long time = (long) (System.currentTimeMillis() * frameSampling / 1000 );
			if (lastFrameSec < time && lastFrameSec != firstFrameSec)
			{
				lastFPS = (int) (frames * 1.0 * frameSampling);
				frames = 0;
			}
			

			lastFrameSec = time;	
			frames++;

			//g.setColor(new Color(255, 227, 186));
			//g.fillRect(0, 0, (int) (Screen.sizeX * Screen.scale), (int) (Screen.sizeY * Screen.scale));

			Game.screen.draw(g);

			g.setColor(new Color(87, 46, 8));
			g.fillRect(0, (int) (Panel.windowHeight - 40 - Window.yOffset), (int) (Panel.windowWidth), 40);

			g.setColor(new Color(255, 227, 186));

			g.setFont(g.getFont().deriveFont(Font.BOLD, 12));

			g.drawString("Tanks v0.4.0", 2, (int) (Panel.windowHeight - 40 + 12 - Window.yOffset));
			g.drawString("FPS: " + lastFPS, 2, (int) (Panel.windowHeight - 40 + 24 - Window.yOffset));
			//g.drawString("Coins: " + Game.coins, 2, (int) (Panel.windowHeight - 40 + 36 - Window.yOffset));		

			/*int obstacles = Game.obstacles.size();
		int movables = Game.movables.size();
		int effects = Game.effects.size();

		int drawHeight = 23;
		int drawSize = 10;*/

			/*g.setColor(Color.red);
		g.fillRect(0, drawHeight, obstacles, drawSize);
		g.setColor(Color.green);
		g.fillRect(obstacles, drawHeight, movables, drawSize);
		g.setColor(Color.blue);
		g.fillRect(obstacles + movables, drawHeight, effects, drawSize);*/

			/*for (int i = 0; i < Game.obstacles.size(); i++)
		{
			//Game.obstacles.get(i).posX += (Game.obstacles.get(i).posX - Game.player.posX) / 1000;
			//Game.obstacles.get(i).posY += (Game.obstacles.get(i).posY - Game.player.posY) / 1000;

			Game.obstacles.get(i).posX += Math.random() * 4 - 2;
			Game.obstacles.get(i).posY += Math.random() * 4 - 2;
		}
		for (int i = 0; i < Game.movables.size(); i++)
		{
			Game.movables.get(i).posX += Math.random() * 4 - 2;
			Game.movables.get(i).posY += Math.random() * 4 - 2;
		}
		for (int i = 0; i < Game.effects.size(); i++)
		{
			Game.effects.get(i).posX += Math.random() * 4 - 2;
			Game.effects.get(i).posY += Math.random() * 4 - 2;
		}*/

			//g.setColor(Color.red);
			//g.fillRect(Game.gamescreen.getWidth() - 250, (int)(Game.gamescreen.getSize().getHeight() - 40 + 15 - Screen.offset), (int) (200 * (Runtime.getRuntime().totalMemory() * 1.0 / Runtime.getRuntime().maxMemory())), 10);
			//g.drawRect(Game.gamescreen.getWidth() - 250, (int)(Game.gamescreen.getSize().getHeight() - 40 + 15 - Screen.offset), 200, 10);

			double mx = Game.window.getInterfaceMouseX();
			double my = Game.window.getInterfaceMouseY();

			double mx2 = Game.window.getMouseX();
			double my2 = Game.window.getMouseY();
			if (showMouseTarget)
			{
				g.setColor(Color.black);
				Window.drawInterfaceOval(g, mx, my, 8, 8);
				Window.drawInterfaceOval(g, mx, my, 4, 4);
				
				g.setColor(Color.red);
				Window.drawOval(g, mx2, my2, 8, 8);
				Window.drawOval(g, mx2, my2, 4, 4);
			}
		}
		catch (Exception e)
		{
			Game.exitToCrash(e);
		}

		//long end = System.nanoTime();
		//System.out.println("Drawing took: " + (end - start));
	}
}