package tanks;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JPanel;
import javax.swing.Timer;

@SuppressWarnings("serial")
public class Panel extends JPanel
{
	Timer timer;
	int height = Window.sizeY;
	int width = Window.sizeX;
	boolean resize = true;

	static boolean showMouseTarget = true;

	ArrayList<Long> framesList = new ArrayList<Long>();

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

					Game.screen.update();


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
					Game.exitToCrash();

					Game.logger.println(new Date().toString() + " (syserr) the game has crashed! below is a crash report, good luck:");
					exception.printStackTrace(Game.logger);
					Game.crashMessage = e.toString();
				}
			}

		});
	}

	public void startTimer()
	{
		timer.start();
	}

	@Override
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

	}

	@Override
	public void paint(Graphics g)
	{					
		try
		{
			if (System.currentTimeMillis() - startTime < 1000)
			{
				for (int i = 0; i < Game.currentSizeX; i++)
				{
					g.setColor(Level.currentColor);
					Window.fillRect(g, Window.sizeX / 2, Window.sizeY / 2, Window.sizeX * 1.2, Window.sizeY * 1.2);				
					g.drawImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("loading.png")), 0, 0, null);

				}
				return;
			}
			
			Window.scale = Math.min(Game.window.getSize().getWidth() * 1.0 / Game.currentSizeX, (Game.window.getSize().getHeight() * 1.0 - 40 - Window.yOffset) / Game.currentSizeY) / 50.0;


			g.fillRect(0, 0, 1 + (int)(Game.window.getSize().getWidth()), 1+(int)(Game.window.getSize().getHeight()));
			
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
			g.fillRect(0, (int) (Game.window.getSize().getHeight() - 40 - Window.yOffset), (int) (Game.window.getSize().getWidth()), 40);

			g.setColor(new Color(255, 227, 186));

			g.setFont(g.getFont().deriveFont(Font.BOLD, 12));

			g.drawString("Tanks v0.3.6", 2, (int) (Game.window.getSize().getHeight() - 40 + 12 - Window.yOffset));
			g.drawString("FPS: " + lastFPS, 2, (int) (Game.window.getSize().getHeight() - 40 + 24 - Window.yOffset));
			g.drawString("Coins: " + Game.coins, 2, (int) (Game.window.getSize().getHeight() - 40 + 36 - Window.yOffset));		

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

			double mx = Game.window.getMouseX();
			double my = Game.window.getMouseY();
			
			if (showMouseTarget)
			{
				g.setColor(Color.black);
				Window.drawOval(g, mx, my, 8, 8);
				Window.drawOval(g, mx, my, 4, 4);
			}
		}
		catch (Exception e)
		{
			Game.exitToCrash();

			Game.logger.println(new Date().toString() + " (syserr) the game has crashed! below is a crash report, good luck:");
			e.printStackTrace(Game.logger);
			Game.crashMessage = e.toString();
		}
	}
}