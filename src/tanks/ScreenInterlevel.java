package tanks;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

public class ScreenInterlevel extends Screen
{
	ArrayList<Firework> fireworks = new ArrayList<Firework>();
	ArrayList<Firework> removeFireworks = new ArrayList<Firework>();
	
	Button replay = new Button(Window.interfaceSizeX / 2, Window.interfaceSizeY / 2 - 30, 350, 40, "Replay the level", new Runnable()
	{
		@Override
		public void run() 
		{
			Level level = new Level(Game.currentLevel);
			level.loadLevel();
			Game.screen = new ScreenGame();
		}
	}
			);
	
	Button save = new Button(Window.interfaceSizeX / 2, Window.interfaceSizeY / 2 + 30, 350, 40, "Save this level", new Runnable()
	{
		@Override
		public void run() 
		{
			ScreenLevelBuilder s = new ScreenLevelBuilder(System.currentTimeMillis() + ".tanks", false);
			Level level = new Level(Game.currentLevel);
			level.loadLevel(s);
			Game.screen = s;
		}
	}
			);
	
	Button newLevel = new Button(Window.interfaceSizeX / 2, Window.interfaceSizeY / 2 - 90, 350, 40, "Generate a new level", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.reset();
			Game.screen = new ScreenGame();
		}
	}
			);
	
	Button quit = new Button(Window.interfaceSizeX / 2, Window.interfaceSizeY / 2 + 90, 350, 40, "Quit to title", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.exitToTitle();
		}
	}
			);
	
	@Override
	public void update()
	{
		newLevel.update();
		replay.update();
		save.update();		
		quit.update();		
	}
	
	public ScreenInterlevel()
	{
		if (Panel.win)
		{
			Window.playSound("resources/win.wav");
			for (int i = 0; i < 5; i++)
			{
				Firework f = new Firework(Firework.FireworkType.rocket, (Math.random() * 0.6 + 0.2) * Window.sizeX, Window.sizeY, fireworks, removeFireworks);
				f.setRandomColor();
				f.vY = - Math.random() * 3 - 6;
				f.vX = Math.random() * 5 - 2.5;
				fireworks.add(f);
			}
		}
		else
			Window.playSound("resources/lose.wav");

	}

	@Override
	public void draw(Graphics g)
	{
		this.drawDefaultBackground(g);

		if (Panel.win && Game.graphicalEffects)
		{	
			if (Math.random() < 0.01)
			{
				Firework f = new Firework(Firework.FireworkType.rocket, (Math.random() * 0.6 + 0.2) * Window.sizeX, Window.sizeY, fireworks, removeFireworks);
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

		newLevel.draw(g);
		replay.draw(g);
		save.draw(g);
		quit.draw(g);
		
		if (Panel.win && Game.graphicalEffects)
			g.setColor(Color.white);
		Window.drawInterfaceText(g, Window.interfaceSizeX / 2, Window.interfaceSizeY / 2 - 150, Panel.winlose);	
		
		if (Panel.win && Game.graphicalEffects)
			Panel.darkness = Math.min(Panel.darkness + Panel.frameFrequency * 1.5, 191);
	}

}
