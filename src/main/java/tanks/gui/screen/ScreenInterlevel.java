package tanks.gui.screen;

import tanks.*;
import tanks.gui.Button;
import tanks.gui.Firework;

import java.io.File;
import java.util.ArrayList;

public class ScreenInterlevel extends Screen implements IDarkScreen
{
	public static boolean tutorialInitial = false;
	public static boolean fromSavedLevels = false;
	public static boolean tutorial = false;

	ArrayList<Firework> fireworks = new ArrayList<Firework>();
	ArrayList<Firework> removeFireworks = new ArrayList<Firework>();

	Button replay = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 30, 350, 40, "Replay the level", new Runnable()
	{
		@Override
		public void run() 
		{
			Level level = new Level(Game.currentLevelString);
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
			Level level = new Level(Game.currentLevelString);
			level.loadLevel();
			Game.screen = new ScreenGame(Crusade.currentCrusade.getShop());
		}
	}
			);

	Button replayTutorial = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, 350, 40, "Try again", new Runnable()
	{
		@Override
		public void run() 
		{
			Tutorial.loadTutorial(!Panel.win && tutorialInitial);
		}
	}
			);
	
	Button replayTutorial2 = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, 350, 40, "Try again", new Runnable()
	{
		@Override
		public void run() 
		{
			Tutorial.loadTutorial(!Panel.win && tutorialInitial);
		}
	}
			);

	Button quitTutorial = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 60, 350, 40, "Back to title", new Runnable()
	{
		@Override
		public void run() 
		{
			tutorial = false;
			Game.exitToTitle();
		}
	}
			);
	
	Button replayCrusadeWin = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 30, 350, 40, "Replay the level", new Runnable()
	{
		@Override
		public void run() 
		{
			Level level = new Level(Game.currentLevelString);
			level.loadLevel();
			Game.screen = new ScreenGame(Crusade.currentCrusade.getShop());
			Crusade.currentCrusade.replay = true;
		}
	}
	, "You will not gain extra lives---"
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
				if (Crusade.currentCrusade.lose)
					Crusade.currentCrusade = null;
			}

			ScreenLevelBuilder s = new ScreenLevelBuilder(System.currentTimeMillis() + ".tanks", false);
			Level level = new Level(Game.currentLevelString);
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

	Button quitCrusadeEnd = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 90, 350, 40, "Back to title", new Runnable()
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

	Button quit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 90, 350, 40, "Quit to title", new Runnable()
	{
		@Override
		public void run()
		{
			Game.exitToTitle();
		}
	}
	);

	Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, 350, 40, "Back to my levels", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.cleanUp();
			System.gc();
			Game.screen = new ScreenPlaySavedLevels();
			fromSavedLevels = false;
		}
	}
			);

	Button exitTutorial = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, 350, 40, "Let's go!", new Runnable()
	{
		@Override
		public void run() 
		{
			tutorialInitial = false;
			tutorial = false;

			Game.exitToTitle();
			try 
			{
				new File(Game.homedir + Game.tutorialPath).createNewFile();
			} 
			catch (Exception e)
			{
				Game.exitToCrash(e);
			}
		}
	}
			);

	Button quitCrusade = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 90, 350, 40, "Quit to title", new Runnable()
	{
		@Override
		public void run() 
		{
			if (Panel.win)
			{
				Crusade.currentCrusade.currentLevel++;
				Crusade.currentCrusade.replay = false;
			}
			
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

		if (tutorialInitial)
		{
			skip = true;
			if (Panel.win)
				exitTutorial.update();
			else
				replayTutorial.update();
		}
		else if (tutorial)
		{
			skip = true;
			
			quitTutorial.update();
			
			if (!Panel.win)
				replayTutorial2.update();
		}
		else if (fromSavedLevels)
		{
			skip = true;
			replay.update();
			back.update();
		}
		else
		{
			save.update();

			if (!Crusade.crusadeMode)
				quit.update();
			else if (skip)
				quitCrusadeEnd.update();
		}

		if (!skip)
		{
			if (Crusade.crusadeMode)
			{
				if (Panel.win || Crusade.currentCrusade.replay)
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
	}

	public ScreenInterlevel()
	{
		Panel.panel.hotbar.bottomOffset = 100;

		if (Crusade.crusadeMode)
		{
			Crusade.currentCrusade.levelFinished(Panel.win);
		}

		if (Panel.win)
			Drawing.drawing.playSound("win.ogg");
		else
			Drawing.drawing.playSound("lose.ogg");

		if (Panel.win && Game.fancyGraphics)
		{
			for (int i = 0; i < 5; i++)
			{
				Firework f = new Firework(Firework.FireworkType.rocket, (Math.random() * 0.6 + 0.2) * Drawing.drawing.sizeX, Drawing.drawing.sizeY, fireworks, removeFireworks);
				f.setRandomColor();
				f.vY = - Math.random() * 1.5 * Game.currentSizeY / 18 - 6;
				f.vX = Math.random() * 5 - 2.5;
				fireworks.add(f);
			}
		}

	}

	@Override
	public void draw()
	{
		this.drawDefaultBackground();

		boolean skip = false;
		if (Crusade.crusadeMode)
			if (Crusade.currentCrusade.lose || Crusade.currentCrusade.win)
				skip = true;

		if (tutorialInitial)
		{
			skip = true;
			if (Panel.win)
				exitTutorial.draw();
			else
				replayTutorial.draw();
		}
		else if (tutorial)
		{
			skip = true;
			
			quitTutorial.draw();
			
			if (!Panel.win)
				replayTutorial2.draw();
		}
		else if (fromSavedLevels)
		{
			skip = true;
			replay.draw();
			back.draw();
		}
		else
		{
			save.draw();

			if (!Crusade.crusadeMode)
				quit.draw();
			else if (skip)
				quitCrusadeEnd.draw();
		}

		if (!skip)
		{
			if (Crusade.crusadeMode)
			{
				if (Panel.win || Crusade.currentCrusade.replay)
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

		Drawing.drawing.setInterfaceFontSize(24);

		if (tutorialInitial)
		{
			if (Panel.win)
				Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 30, "Congratulations! You are now ready to play!");
			else
				Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 30, Panel.winlose);
		}
		else if (Crusade.crusadeMode)
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
		{
			Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 150, "Lives remaining: " + Game.player.remainingLives);

			if (Crusade.currentCrusade.lifeGained)
				Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 250, "You gained a life for clearing Battle " + (Crusade.currentCrusade.currentLevel + 1) + "!");
		}

		if (Panel.win && Game.fancyGraphics)
			Panel.darkness = Math.min(Panel.darkness + Panel.frameFrequency * 1.5, 191);
		
		if (Panel.win && Game.fancyGraphics)
		{	
			if (Math.random() < 0.02)
			{
				Firework f = new Firework(Firework.FireworkType.rocket, (Math.random() * 0.6 + 0.2) * Drawing.drawing.sizeX, Drawing.drawing.sizeY, fireworks, removeFireworks);
				f.setRandomColor();
				f.vY = - Math.random() * 1.5 * Game.currentSizeY / 18 - 6;
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
	}
}
