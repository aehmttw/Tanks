package tanks;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

public class ScreenInterlevel extends Screen
{
	ArrayList<Firework> fireworks = new ArrayList<Firework>();
	ArrayList<Firework> removeFireworks = new ArrayList<Firework>();
	
	Button replay = new Button(350, 40, "Replay the level", new Runnable()
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
	
	Button newLevel = new Button(350, 40, "Generate a new level", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.reset();
			Game.screen = new ScreenGame();
		}
	}
			);
	
	Button quit = new Button(350, 40, "Quit to title", new Runnable()
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
		newLevel.update(Window.sizeX / 2, Window.sizeY / 2 - 60);
		replay.update(Window.sizeX / 2, Window.sizeY / 2);
		quit.update(Window.sizeX / 2, Window.sizeY / 2 + 60);		
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

		newLevel.draw(g, Window.sizeX / 2, Window.sizeY / 2 - 60);
		replay.draw(g, Window.sizeX / 2, Window.sizeY / 2);
		quit.draw(g, Window.sizeX / 2, Window.sizeY / 2 + 60);
		
		if (Panel.win && Game.graphicalEffects)
			g.setColor(Color.white);
		Window.drawText(g, Window.sizeX / 2, Window.sizeY / 2 - 150, Panel.winlose);	
		
		if (Panel.win && Game.graphicalEffects)
			Panel.darkness = Math.min(Panel.darkness + Panel.frameFrequency * 1.5, 191);
	}

}
