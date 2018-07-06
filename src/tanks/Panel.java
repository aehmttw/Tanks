package tanks;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.Timer;

@SuppressWarnings("serial")
public class Panel extends JPanel
{
	Timer timer;
	int height = Screen.sizeY;
	int width = Screen.sizeX;
	boolean resize = true;

	boolean showMouseTarget = true;

	ArrayList<Long> framesList = new ArrayList<Long>();

	String winlose = "";
	boolean win = false;

	double darkness = 0;
	
	/** Important value used in calculating game speed. Larger values are set when the frames are lower, and game speed is increased to compensate.*/
	static double frameFrequency = 1;

	ArrayList<Double> frameFrequencies = new ArrayList<Double>();
	
	int firstTimer = (int) (100 / frameFrequency);

	int frames = 0;

	double frameSampling = 1;

	long lastFrame = System.currentTimeMillis();

	long firstFrameSec = (long) (System.currentTimeMillis() / 1000.0 * frameSampling);
	long lastFrameSec = (long) (System.currentTimeMillis() / 1000.0 * frameSampling);

	int lastFPS = (int) (100 / frameFrequency);

	ArrayList<Firework> fireworks = new ArrayList<Firework>();
	ArrayList<Firework> removeFireworks = new ArrayList<Firework>();
	
	Button resume = new Button(350, 40, "Continue playing", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.menu = Game.Menu.none;
			Game.paused = false;
			Game.player.cooldown = 20;
		}
	}
			);

	Button newLevel = new Button(350, 40, "Generate a new level", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.reset();
			Game.menu = Game.Menu.none;
			Game.paused = false;
		}
	}
			);

	Button graphics = new Button(350, 40, "Graphics: fancy", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.graphicalEffects = !Game.graphicalEffects;

			if (Game.graphicalEffects)
				graphics.text = "Graphics: fancy";
			else
				graphics.text = "Graphics: fast";
		}
	}
			);

	Button mouseTarget = new Button(350, 40, "Mouse target: enabled", new Runnable()
	{
		@Override
		public void run() 
		{
			showMouseTarget = !showMouseTarget;

			if (showMouseTarget)
				mouseTarget.text = "Mouse target: enabled";
			else
				mouseTarget.text = "Mouse target: disabled";
		}
	}
			);

	Button scale = new Button(350, 40, "Scale: 100%", new Runnable()
	{
		@Override
		public void run() 
		{
			if (KeyInputListener.keys.contains(KeyEvent.VK_SHIFT))
				Screen.scale -= 0.1;
			else
				Screen.scale += 0.1;

			if (Screen.scale < 0.45)
				Screen.scale = 2;

			if (Screen.scale > 2.05)
				Screen.scale = 0.5;

			Screen.scale = Math.round(Screen.scale * 10) / 10.0;

			scale.text = "Scale: " + (int)Math.round(Screen.scale * 100) + "%";
			Game.gamescreen.setSize((int)(Screen.sizeX * Screen.scale), (int) ((Screen.sizeY) * Screen.scale ));
		}
	}
	, "Click to increase scale by 10%---Hold shift while clicking to decrease scale by 10%");

	Button quit = new Button(350, 40, "Quit to title", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.exitToTitle();
			//System.exit(0);
		}
	}
			);

	Button back = new Button(350, 40, "Back", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.menu = Game.Menu.title;
		}
	}
			);

	Button exit = new Button(350, 40, "Exit the game", new Runnable()
	{
		@Override
		public void run() 
		{
			System.exit(0);
		}
	}
			);

	Button insanity = new Button(350, 40, "Insanity mode: disabled", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.insanity = !Game.insanity;

			if (Game.insanity)
				insanity.text = "Insanity mode: enabled";
			else
				insanity.text = "Insanity mode: disabled";
		}
	}
			);

	Button options = new Button(350, 40, "Options...", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.menu = Game.Menu.options;
		}
	}
			);

	Button replay = new Button(350, 40, "Replay the level", new Runnable()
	{
		@Override
		public void run() 
		{
			Level level = new Level(Game.currentLevel);
			level.loadLevel();
			Game.menu = Game.Menu.none;
			Game.paused = false;
		}
	}
			);

	boolean pausePressed = false;

	static int preGameTimer = 0;

	public Panel()
	{
		timer = new Timer((int) (10 * frameFrequency), new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e) 
			{	
				//long start = System.nanoTime();

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

				
				if (KeyInputListener.keys.contains(KeyEvent.VK_ESCAPE))
				{

					if (Game.menu == Game.Menu.paused || Game.menu == Game.Menu.none)
					{
						if (!pausePressed)
							Game.paused = !Game.paused;

						if (Game.paused)
							Game.menu = Game.Menu.paused;
						else
							Game.menu = Game.Menu.none;
					}

					pausePressed = true;
				}
				else
					pausePressed = false;

				if (!Game.paused)
				{
					if (firstTimer > 0)
					{
						firstTimer--;
						Obstacle.draw_size = Math.min(Game.tank_size, Obstacle.draw_size + Panel.frameFrequency);
					}
					else if (preGameTimer > 0)
					{
						preGameTimer -= frameFrequency;
						if (Game.movables.contains(Game.player))
						{
							Obstacle.draw_size = Math.min(Game.tank_size, Obstacle.draw_size + frameFrequency);
						}
					}
					else
					{
						Obstacle.draw_size = Math.min(Obstacle.obstacle_size, Obstacle.draw_size);
						int tanks = 0;
						for (int i = 0; i < Game.movables.size(); i++)
						{
							Movable m = Game.movables.get(i);
							m.update();
							if (m instanceof Tank)
								tanks++;
						}

						if (!Game.movables.contains(Game.player))
						{
							for (int m = 0; m < Game.movables.size(); m++)
							{
								Movable mo = Game.movables.get(m);
								if (mo instanceof Bullet)
									mo.destroy = true;
							}

							if (Game.effects.size() == 0)
							{
								Obstacle.draw_size = Math.max(0, Obstacle.draw_size - Panel.frameFrequency);
								for (int i = 0; i < Game.movables.size(); i++)
									Game.movables.get(i).destroy = true;
								
								if (Obstacle.draw_size <= 0)
								{
									winlose = "You were destroyed!";
									win = false;
									Game.exit();
								}
							}
							
						}

						/*for (int i = 0; i < Game.obstacles.size(); i++)
						{
							Game.obstacles.get(i).posX += (Game.obstacles.get(i).posX - Game.player.posX) / 1000;
							Game.obstacles.get(i).posY += (Game.obstacles.get(i).posY - Game.player.posY) / 1000;
						}*/

						if (tanks <= 1 && !Game.player.destroy)
						{
							Game.bulletLocked = true;
							for (int m = 0; m < Game.movables.size(); m++)
							{
								Movable mo = Game.movables.get(m);
								if (mo instanceof Bullet || mo instanceof Mine)
									mo.destroy = true;
							}

							if (Game.effects.size() == 0)
							{
								Obstacle.draw_size = Math.max(0, Obstacle.draw_size - Panel.frameFrequency);

								if (Obstacle.draw_size <= 0)
								{
									winlose = "Level Cleared!";
									win = true;
									Game.exit();
								}
							}
						}
						else
							Game.bulletLocked = false;
					}

					for (int i = 0; i < Game.removeMovables.size(); i++)
						Game.movables.remove(Game.removeMovables.get(i));

					for (int i = 0; i < Game.removeObstacles.size(); i++)
						Game.obstacles.remove(Game.removeObstacles.get(i));

					for (int i = 0; i < Game.removeEffects.size(); i++)
						Game.effects.remove(Game.removeEffects.get(i));
					
					for (int i = 0; i < Game.removeBelowEffects.size(); i++)
						Game.effects.remove(Game.removeBelowEffects.get(i));

					Game.removeMovables.clear();
					Game.removeObstacles.clear();
					Game.removeEffects.clear();
					Game.removeBelowEffects.clear();
				}
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
			g.drawRect(15,15,(this.getWidth()-30), this.getHeight()-30);
		}

	}

	@Override
	public void paint(Graphics g)
	{	
		g.fillRect(0, 0, 1 + (int)(Game.gamescreen.getSize().getWidth()), 1+(int)(Game.gamescreen.getSize().getHeight()));
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

		if (Game.graphicalEffects)
		{
			for (int i = 0; i < Game.currentSizeX; i++)
			{
				for (int j = 0; j < Game.currentSizeY; j++)
				{
					int extra;
					if (Screen.scale * 10 == Math.round(Screen.scale * 10))
						extra = 0;
					else
						extra = 0;

					g.setColor(Game.tiles[i][j]);
					Screen.fillRect(g, (i + 0.5) / Game.bgResMultiplier * Obstacle.obstacle_size, (j + 0.5) / Game.bgResMultiplier * Obstacle.obstacle_size, extra + Obstacle.obstacle_size / Game.bgResMultiplier, extra + Obstacle.obstacle_size / Game.bgResMultiplier);
				}
			}
		}
		else
		{
			g.setColor(Level.currentColor);
			Screen.fillRect(g, Screen.sizeX / 2, Screen.sizeY / 2, Screen.sizeX, Screen.sizeX);
		}

		if (!Game.paused)
		{
			for (int i = 0; i < Game.belowEffects.size(); i++)
				Game.belowEffects.get(i).draw(g);
		}
		else
		{
			for (int i = 0; i < Game.belowEffects.size(); i++)
				((Effect)Game.belowEffects.get(i)).drawWithoutUpdate(g);
		}
		
		for (int n = 0; n < Game.movables.size(); n++)
			Game.movables.get(n).draw(g);

		for (int i = 0; i < Game.obstacles.size(); i++)
			Game.obstacles.get(i).draw(g);

		if (!Game.paused)
		{
			for (int i = 0; i < Game.effects.size(); i++)
				Game.effects.get(i).draw(g);
		}
		else
		{
			for (int i = 0; i < Game.effects.size(); i++)
				((Effect)Game.effects.get(i)).drawWithoutUpdate(g);
		}

		double mx = Screen.screen.getMouseX();
		double my = Screen.screen.getMouseY();

		Screen.scale = Math.min(Game.gamescreen.getSize().getWidth() * 1.0 / Game.currentSizeX, (Game.gamescreen.getSize().getHeight() * 1.0 - 40 - Screen.yOffset) / Game.currentSizeY) / 50.0;

		//System.out.println(Game.gamescreen.getSize().getWidth() * 1.0 / Game.currentSizeX + " " + (Game.gamescreen.getSize().getHeight() * 1.0 - 40 - Screen.offset) / Game.currentSizeY);

		//System.out.println(Screen.scale);

		if (Game.menu.equals(Game.Menu.interlevel) && win && Game.graphicalEffects)
			darkness = Math.min(darkness + Panel.frameFrequency * 1.5, 191);
		else
			darkness = Math.max(darkness - Panel.frameFrequency * 3, 0);
		
		
		g.setColor(new Color(0, 0, 0, (int) darkness));
		Screen.fillRect(g, Screen.sizeX / 2, Screen.sizeY / 2, Screen.sizeX, Screen.sizeY);
		
		if (Game.menu.equals(Game.Menu.title))
		{	
			Game.paused = true;
			g.setColor(Color.black);
			g.setFont(g.getFont().deriveFont(Font.BOLD, (float) (60 * Screen.scale)));
			Screen.drawText(g, Screen.sizeX / 2, Screen.sizeY / 2 - 200, "Tanks");
			newLevel.drawUpdate(g, Screen.sizeX / 2, Screen.sizeY / 2);
			exit.drawUpdate(g, Screen.sizeX / 2, Screen.sizeY / 2 + 120);
			options.drawUpdate(g, Screen.sizeX / 2, Screen.sizeY / 2 + 60);
		}
		else if (Game.menu.equals(Game.Menu.paused))
		{
			g.setColor(new Color(127, 178, 228, 64));
			g.fillRect(0, 0, (int) (Game.gamescreen.getSize().getWidth()) + 1, (int) (Game.gamescreen.getSize().getHeight()) + 1);
			newLevel.drawUpdate(g, Screen.sizeX / 2, Screen.sizeY / 2);
			quit.drawUpdate(g, Screen.sizeX / 2, Screen.sizeY / 2 + 60);
			resume.drawUpdate(g, Screen.sizeX / 2, Screen.sizeY / 2 - 60);
			g.setColor(Color.black);
			Screen.drawText(g, Screen.sizeX / 2, Screen.sizeY / 2 - 150, "Game paused");
		}
		else if (Game.menu.equals(Game.Menu.options))
		{
			insanity.drawUpdate(g, Screen.sizeX / 2, Screen.sizeY / 2 + 30);
			mouseTarget.drawUpdate(g, Screen.sizeX / 2, Screen.sizeY / 2 - 30);
			graphics.drawUpdate(g, Screen.sizeX / 2, Screen.sizeY / 2 - 90);
			back.drawUpdate(g, Screen.sizeX / 2, Screen.sizeY / 2 + 90);
			Screen.drawText(g, Screen.sizeX / 2, Screen.sizeY / 2 - 150, "Options");
			//scale.drawUpdate(g, Screen.sizeX / 2, Screen.sizeY / 2 + 0);
		}
		else if (Game.menu.equals(Game.Menu.interlevel))
		{
			if (win && Game.graphicalEffects)
			{	
				if (Math.random() < 0.01)
				{
					Firework f = new Firework(Firework.FireworkType.rocket, (Math.random() * 0.6 + 0.2) * Screen.sizeX, Screen.sizeY, fireworks, removeFireworks);
					f.setRandomColor();
					f.vY = - Math.random() * 3 - 6;
					f.vX = Math.random() * 5 - 2.5;
					fireworks.add(f);
				}
				
				for (int i = 0; i < fireworks.size(); i++)
				{
					fireworks.get(i).drawUpdate(g);
				}
				
				for (int i = 0; i < removeFireworks.size(); i++)
				{
					fireworks.remove(removeFireworks.get(i));
				}  
			}
			
			newLevel.drawUpdate(g, Screen.sizeX / 2, Screen.sizeY / 2 - 60);
			replay.drawUpdate(g, Screen.sizeX / 2, Screen.sizeY / 2);
			quit.drawUpdate(g, Screen.sizeX / 2, Screen.sizeY / 2 + 60);
			if (win && Game.graphicalEffects)
				g.setColor(Color.white);
			Screen.drawText(g, Screen.sizeX / 2, Screen.sizeY / 2 - 150, winlose);
		}



		g.setColor(new Color(87, 46, 8));
		g.fillRect(0, (int) (Game.gamescreen.getSize().getHeight() - 40 - Screen.yOffset), (int) (Game.gamescreen.getSize().getWidth()), 40);

		g.setColor(new Color(255, 227, 186));

		g.setFont(g.getFont().deriveFont(Font.BOLD, 12));

		g.drawString("Tanks v0.3.3a", 2, (int) (Game.gamescreen.getSize().getHeight() - 40 + 12 - Screen.yOffset));
		g.drawString("FPS: " + lastFPS, 2, (int) (Game.gamescreen.getSize().getHeight() - 40 + 24 - Screen.yOffset));
		g.drawString("Coins: " + Game.coins, 2, (int) (Game.gamescreen.getSize().getHeight() - 40 + 36 - Screen.yOffset));		

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

		if (showMouseTarget)
		{
			g.setColor(Color.black);
			Screen.drawOval(g, mx, my, 8, 8);
			Screen.drawOval(g, mx, my, 4, 4);
		}

	}
}