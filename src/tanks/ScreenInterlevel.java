package tanks;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

public class ScreenInterlevel extends Screen
{
	ArrayList<Firework> fireworks = new ArrayList<Firework>();
	ArrayList<Firework> removeFireworks = new ArrayList<Firework>();

	Button replay = new Button(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 - 30, 350, 40, "Replay the level", new Runnable()
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

	Button save = new Button(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 + 30, 350, 40, "Save this level", new Runnable()
	{
		@Override
		public void run() 
		{
			Crusade.crusadeMode = false;
			
			if (Crusade.currentCrusade != null)
			{
				if (Crusade.currentCrusade.remainingLives <= 0)
					Crusade.currentCrusade = null;
			}
			
			ScreenLevelBuilder s = new ScreenLevelBuilder(System.currentTimeMillis() + ".tanks", false);
			Level level = new Level(Game.currentLevel);
			level.loadLevel(s);
			Game.screen = s;
		}
	}
			);

	Button newLevel = new Button(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 - 90, 350, 40, "Generate a new level", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.reset();
			Game.screen = new ScreenGame();
		}
	}
			);

	Button nextLevel = new Button(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 - 90, 350, 40, "Next level", new Runnable()
	{
		@Override
		public void run() 
		{
			Crusade.currentCrusade.loadLevel();
			Game.screen = new ScreenGame();
		}
	}
			);

	Button quit = new Button(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 + 90, 350, 40, "Quit to title", new Runnable()
	{
		@Override
		public void run() 
		{
			Crusade.crusadeMode = false;
			Crusade.currentCrusade = null;
			Game.exitToTitle();
		}
	}
			);
	
	Button quitCrusade = new Button(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 + 90, 350, 40, "Quit to title", new Runnable()
	{
		@Override
		public void run() 
		{
			Crusade.crusadeMode = false;
			Game.exitToTitle();
		}
	}
			, "You will be able to return to the crusade---through the crusade button on---the play screen.");

	@Override
	public void update()
	{
		boolean skip = false;
		if (Crusade.crusadeMode)
			if (Crusade.currentCrusade.lose || Crusade.currentCrusade.win)
				skip = true;

		if (!skip)
		{
			if (Crusade.crusadeMode)
			{
				if (Panel.win)
					nextLevel.update();
				
				quitCrusade.update();
			}
			else
				newLevel.update();

			replay.update();
		}
		
		if (skip || !Crusade.crusadeMode)
			quit.update();		

		save.update();		
	}

	public ScreenInterlevel()
	{
		if (Crusade.crusadeMode)
		{
			Crusade.currentCrusade.levelFinished(Panel.win);
		}

		if (Panel.win)
		{
			Drawing.playSound("resources/win.wav");
			for (int i = 0; i < 5; i++)
			{
				Firework f = new Firework(Firework.FireworkType.rocket, (Math.random() * 0.6 + 0.2) * Drawing.sizeX, Drawing.sizeY, fireworks, removeFireworks);
				f.setRandomColor();
				f.vY = - Math.random() * 3 - 6;
				f.vX = Math.random() * 5 - 2.5;
				fireworks.add(f);
			}
		}
		else
			Drawing.playSound("resources/lose.wav");

	}

	@Override
	public void draw(Graphics g)
	{
		this.drawDefaultBackground(g);

		if (Panel.win && Game.graphicalEffects)
		{	
			if (Math.random() < 0.01)
			{
				Firework f = new Firework(Firework.FireworkType.rocket, (Math.random() * 0.6 + 0.2) * Drawing.sizeX, Drawing.sizeY, fireworks, removeFireworks);
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

		boolean skip = false;
		if (Crusade.crusadeMode)
			if (Crusade.currentCrusade.lose || Crusade.currentCrusade.win)
				skip = true;

		if (!skip)
		{
			if (Crusade.crusadeMode)
			{
				if (Panel.win)
					nextLevel.draw(g);
				
				quitCrusade.draw(g);
			}
			else
				newLevel.draw(g);

			replay.draw(g);
		}
		
		if (!Crusade.crusadeMode || skip)
			quit.draw(g);
		
		save.draw(g);

		if (Panel.win && Game.graphicalEffects)
			g.setColor(Color.white);

		if (Crusade.crusadeMode)
		{
			if (Crusade.currentCrusade.win)
				Drawing.window.drawInterfaceText(g, Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 - 190, "You finished the crusade!");	
			else if (Crusade.currentCrusade.lose)
				Drawing.window.drawInterfaceText(g, Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 - 190, "Game over!");	
			else
				Drawing.window.drawInterfaceText(g, Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 - 190, Panel.winlose);	
		}
		else
			Drawing.window.drawInterfaceText(g, Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 - 150, Panel.winlose);	

		if (Crusade.crusadeMode)
			Drawing.window.drawInterfaceText(g, Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 - 150, "Lives remaining: " + Crusade.currentCrusade.remainingLives);	

		if (Panel.win && Game.graphicalEffects)
			Panel.darkness = Math.min(Panel.darkness + Panel.frameFrequency * 1.5, 191);
	}

}
