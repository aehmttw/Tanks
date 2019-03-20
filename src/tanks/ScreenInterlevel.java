package tanks;

import java.util.ArrayList;

public class ScreenInterlevel extends Screen
{
	ArrayList<Firework> fireworks = new ArrayList<Firework>();
	ArrayList<Firework> removeFireworks = new ArrayList<Firework>();

	Button replay = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 30, 350, 40, "Replay the level", new Runnable()
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
	
	Button replayCrusade = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 30, 350, 40, "Try again", new Runnable()
	{
		@Override
		public void run() 
		{
			Level level = new Level(Game.currentLevel);
			level.loadLevel();
			Game.screen = new ScreenGame(Crusade.currentCrusade.getShop());
		}
	}
			);
	
	Button replayCrusadeWin = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 30, 350, 40, "Replay the level", new Runnable()
	{
		@Override
		public void run() 
		{
			Level level = new Level(Game.currentLevel);
			level.loadLevel();
			Game.screen = new ScreenGame(Crusade.currentCrusade.getShop());
			Crusade.currentCrusade.replay = true;
		}
	}
			, "You will not gain extra live---"
					+ "from replaying a level you've already beaten.---"
					+ "However, you can still earn coins!---"
					+ "You will still lose a life if you die.");

	Button save = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, 350, 40, "Save this level", new Runnable()
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

	Button newLevel = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 90, 350, 40, "Generate a new level", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.reset();
			Game.screen = new ScreenGame();
		}
	}
			);

	Button nextLevel = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 90, 350, 40, "Next level", new Runnable()
	{
		@Override
		public void run() 
		{
			Crusade.currentCrusade.currentLevel++;
			Crusade.currentCrusade.replay = false;
			Crusade.currentCrusade.loadLevel();
			Game.screen = new ScreenGame(Crusade.currentCrusade.getShop());
		}
	}
			);

	Button quit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 90, 350, 40, "Quit to title", new Runnable()
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
	
	Button quitCrusade = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 90, 350, 40, "Quit to title", new Runnable()
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
				{
					nextLevel.update();
					replayCrusadeWin.update();
				}
				else
					replayCrusade.update();
				
				quitCrusade.update();
			}
			else
			{
				replay.update();
				newLevel.update();
			}
		}
		
		if (skip || !Crusade.crusadeMode)
			quit.update();		

		save.update();		
	}

	public ScreenInterlevel()
	{
		Panel.panel.hotbar.bottomOffset = 100;
		if (Crusade.crusadeMode)
		{
			Crusade.currentCrusade.levelFinished(Panel.win);
		}

		if (Panel.win)
		{
			Drawing.drawing.playSound("resources/win.wav");
			for (int i = 0; i < 5; i++)
			{
				Firework f = new Firework(Firework.FireworkType.rocket, (Math.random() * 0.6 + 0.2) * Drawing.drawing.sizeX, Drawing.drawing.sizeY, fireworks, removeFireworks);
				f.setRandomColor();
				f.vY = - Math.random() * 3 - 6;
				f.vX = Math.random() * 5 - 2.5;
				fireworks.add(f);
			}
		}
		else
			Drawing.drawing.playSound("resources/lose.wav");

	}

	@Override
	public void draw()
	{
		this.drawDefaultBackground();

		if (Panel.win && Game.fancyGraphics)
		{	
			if (Math.random() < 0.01)
			{
				Firework f = new Firework(Firework.FireworkType.rocket, (Math.random() * 0.6 + 0.2) * Drawing.drawing.sizeX, Drawing.drawing.sizeY, fireworks, removeFireworks);
				f.setRandomColor();
				f.vY = - Math.random() * 3 - 6;
				f.vX = Math.random() * 5 - 2.5;
				fireworks.add(f);
			}

			for (int i = 0; i < fireworks.size(); i++)
			{
				fireworks.get(i).drawUpdate();
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

		save.draw();
		
		if (!Crusade.crusadeMode || skip)
			quit.draw();
		
		if (!skip)
		{
			if (Crusade.crusadeMode)
			{
				if (Panel.win)
				{
					nextLevel.draw();
					replayCrusadeWin.draw();
				}
				else
					replayCrusade.draw();
				
				quitCrusade.draw();
			}
			else
			{
				replay.draw();
				newLevel.draw();
			}
		}
		
		

		if (Panel.win && Game.fancyGraphics)
			Drawing.drawing.setColor(255, 255, 255);
		else
			Drawing.drawing.setColor(0, 0, 0);
		
		Drawing.drawing.setFontSize(24);

		if (Crusade.crusadeMode)
		{
			if (Crusade.currentCrusade.win)
				Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 190, "You finished the crusade!");	
			else if (Crusade.currentCrusade.lose)
				Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 190, "Game over!");	
			else
				Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 190, Panel.winlose);	
		}
		else
			Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 150, Panel.winlose);	

		if (Crusade.crusadeMode)
			Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 150, "Lives remaining: " + Crusade.currentCrusade.remainingLives);	

		if (Panel.win && Game.fancyGraphics)
			Panel.darkness = Math.min(Panel.darkness + Panel.frameFrequency * 1.5, 191);
	}

}
