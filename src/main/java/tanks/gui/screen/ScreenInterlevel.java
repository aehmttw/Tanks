package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.*;
import tanks.gui.Button;
import tanks.gui.Firework;
import tanks.gui.SpeedrunTimer;
import tanks.gui.screen.levelbuilder.ScreenLevelEditor;

import java.util.ArrayList;
import java.util.Date;

public class ScreenInterlevel extends Screen implements IDarkScreen
{
	public static boolean tutorialInitial = false;
	public static boolean fromSavedLevels = false;
	public static boolean tutorial = false;

	boolean odd = false;

	ArrayList<Firework> fireworks1 = new ArrayList<Firework>();
	ArrayList<Firework> fireworks2 = new ArrayList<Firework>();

	public static double firework_frequency = 0.08;

	Button replay = new Button(this.centerX, this.centerY - this.objYSpace / 2, this.objWidth, this.objHeight, "Replay the level", new Runnable()
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

	Button replayCrusade = new Button(this.centerX, this.centerY - this.objYSpace / 2, this.objWidth, this.objHeight, "Try again", new Runnable()
	{
		@Override
		public void run()
		{
			Crusade.currentCrusade.loadLevel();
			Game.screen = new ScreenGame(Crusade.currentCrusade.getShop());
		}
	}
	);

	Button replayTutorial = new Button(this.centerX, this.centerY + this.objYSpace / 2, this.objWidth, this.objHeight, "Try again", new Runnable()
	{
		@Override
		public void run()
		{
			new Tutorial().loadTutorial(!Panel.win && tutorialInitial, Game.game.window.touchscreen);
		}
	}
	);

	Button replayTutorial2 = new Button(this.centerX, this.centerY, this.objWidth, this.objHeight, "Try again", new Runnable()
	{
		@Override
		public void run()
		{
			new Tutorial().loadTutorial(!Panel.win && tutorialInitial, Game.game.window.touchscreen);
		}
	}
	);

	Button quitTutorial = new Button(this.centerX, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "Back to title", new Runnable()
	{
		@Override
		public void run()
		{
			tutorial = false;
			Game.exitToTitle();
		}
	}
	);

	Button replayCrusadeWin = new Button(this.centerX, this.centerY - this.objYSpace / 2, this.objWidth, this.objHeight, "Replay the level", new Runnable()
	{
		@Override
		public void run()
		{
			Crusade.currentCrusade.loadLevel();
			Crusade.currentCrusade.replay = true;
			Game.screen = new ScreenGame(Crusade.currentCrusade.getShop());
		}
	}
			, "You will not gain extra lives---"
			+ "from replaying a level you've already beaten.---"
			+ "However, you can still earn coins!---"
			+ "You will still lose a life if you die.");

	Button save = new Button(this.centerX, this.centerY + this.objYSpace / 2, this.objWidth, this.objHeight, "Save this level", new Runnable()
	{
		@Override
		public void run()
		{
			Crusade.crusadeMode = false;

			if (Crusade.currentCrusade != null)
			{
				Crusade.currentCrusade.crusadePlayers.get(Game.player).saveCrusade();
				Crusade.currentCrusade = null;
			}

			ScreenLevelEditor s = new ScreenLevelEditor(System.currentTimeMillis() + ".tanks", Game.currentLevel);
			Level level = new Level(Game.currentLevelString);
			level.loadLevel(s);
			Game.screen = s;
		}
	}
	);

	Button newLevel = new Button(this.centerX, this.centerY - this.objYSpace * 1.5, this.objWidth, this.objHeight, "Generate a new level", new Runnable()
	{
		@Override
		public void run()
		{
			Game.reset();
			Game.screen = new ScreenGame();
		}
	}
	);

	Button nextLevel = new Button(this.centerX, this.centerY - this.objYSpace * 1.5, this.objWidth, this.objHeight, "Next level", new Runnable()
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

	Button quitCrusadeEnd = new Button(this.centerX, this.centerY + this.objYSpace * 1.5, this.objWidth, this.objHeight, "Continue", new Runnable()
	{
		@Override
		public void run()
		{
			Game.cleanUp();
			Game.screen = new ScreenCrusadeStats(Crusade.currentCrusade);
			//Crusade.crusadeMode = false;
			//Crusade.currentCrusade = null;
			//Game.exitToTitle();

		}
	}
	);

	Button quit = new Button(this.centerX, this.centerY + this.objYSpace * 1.5, this.objWidth, this.objHeight, "Quit to title", new Runnable()
	{
		@Override
		public void run()
		{
			Game.exitToTitle();
		}
	}
	);

	Button back = new Button(this.centerX, this.centerY + this.objYSpace / 2, this.objWidth, this.objHeight, "Back to my levels", new Runnable()
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

	Button exitTutorial = new Button(this.centerX, this.centerY + this.objYSpace / 2, this.objWidth, this.objHeight, "Let's go!", new Runnable()
	{
		@Override
		public void run()
		{
			tutorialInitial = false;
			tutorial = false;

			Game.exitToTitle();
			try
			{
				BaseFile f = Game.game.fileManager.getFile(Game.homedir + Game.tutorialPath);

				f.create();
				f.startWriting();
				f.println("Certificate of completion:");
				f.println("Tanks: The Crusades tutorial");
				f.println("Completed " + new Date().toString());
				f.stopWriting();
			}
			catch (Exception e)
			{
				Game.exitToCrash(e);
			}
		}
	}
	);

	Button quitCrusade = new Button(this.centerX, this.centerY + this.objYSpace * 1.5, this.objWidth, this.objHeight, "Quit to title", new Runnable()
	{
		@Override
		public void run()
		{
			if (Panel.win)
			{
				Crusade.currentCrusade.currentLevel++;
				Crusade.currentCrusade.replay = false;
			}

			Crusade.currentCrusade.crusadePlayers.get(Game.player).saveCrusade();
			Crusade.crusadeMode = false;
			Crusade.currentCrusade = null;
			Game.exitToTitle();
		}
	}
			, "Your crusade progress will be saved.");

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
		Game.player.hotbar.percentHidden = 100;

		if (Crusade.crusadeMode)
		{
			Crusade.currentCrusade.levelFinished(Panel.win);
		}

		if (Panel.win)
		{
			Drawing.drawing.playSound("win.ogg");
			this.music = "win_music.ogg";

			if (Crusade.crusadeMode && Crusade.currentCrusade.win)
				this.music = "win_crusade.ogg";
		}
		else
		{
			Drawing.drawing.playSound("lose.ogg");
			this.music = "lose_music.ogg";
		}

		if (Panel.win && Game.effectsEnabled)
		{
			for (int i = 0; i < 5; i++)
			{
				Firework f = new Firework(Firework.FireworkType.rocket, (Math.random() * 0.6 + 0.2) * Drawing.drawing.interfaceSizeX, Drawing.drawing.interfaceSizeY, getFireworkArray());
				f.setRandomColor();
				f.setVelocity();
				getFireworkArray().add(f);
			}
		}

	}

	@Override
	public void draw()
	{
		this.drawDefaultBackground();

		if (Panel.win && Game.effectsEnabled && !Game.game.window.drawingShadow)
		{
			ArrayList<Firework> fireworks = getFireworkArray();

			if (Math.random() < ScreenInterlevel.firework_frequency * Panel.frameFrequency * Game.effectMultiplier)
			{
				Firework f = new Firework(Firework.FireworkType.rocket, (Math.random() * 0.6 + 0.2) * Drawing.drawing.interfaceSizeX, Drawing.drawing.interfaceSizeY, fireworks);
				f.setRandomColor();
				f.setVelocity();
				getFireworkArray().add(f);
			}

			for (int i = 0; i < getFireworkArray().size(); i++)
			{
				fireworks.get(i).drawUpdate(fireworks, getOtherFireworkArray());
			}

			if (Game.glowEnabled)
			{
				for (int i = 0; i < getFireworkArray().size(); i++)
				{
					fireworks.get(i).drawGlow();
				}
			}

			//A fix to some glitchiness on ios
			Drawing.drawing.setColor(0, 0, 0, 0);
			Drawing.drawing.fillInterfaceRect(0, 0, 0, 0);

			fireworks.clear();
			odd = !odd;
		}

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

		if (Game.showSpeedrunTimer)
			SpeedrunTimer.draw();

		if ((Panel.win && Game.effectsEnabled) || Level.isDark())
			Drawing.drawing.setColor(255, 255, 255);
		else
			Drawing.drawing.setColor(0, 0, 0);

		Drawing.drawing.setInterfaceFontSize(this.titleSize);

		if (tutorialInitial)
		{
			if (Panel.win)
				Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace / 2, "Congratulations! You are now ready to play!");
			else
				Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace / 2, Panel.winlose);
		}
		else if (Crusade.crusadeMode)
		{
			if (Crusade.currentCrusade.win)
				Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 19 / 6, "You finished the crusade!");
			else if (Crusade.currentCrusade.lose)
				Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 19 / 6, "Game over!");
			else
				Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 19 / 6, Panel.winlose);
		}
		else
			Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, Panel.winlose);

		Drawing.drawing.setInterfaceFontSize(this.textSize);

		if (Crusade.crusadeMode)
		{
			Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, "Lives remaining: " + Game.player.remainingLives);

			if (Crusade.currentCrusade.lifeGained)
			{
				double frac = 25.0 / 6;

				if (Drawing.drawing.interfaceScaleZoom > 1)
					frac = 23.0 / 6;

				Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * frac, "You gained a life for clearing Battle " + (Crusade.currentCrusade.currentLevel + 1) + "!");
			}
		}

		if (Panel.win && Game.effectsEnabled)
			Panel.darkness = Math.min(Panel.darkness + Panel.frameFrequency * 1.5, 191);
	}

	public ArrayList<Firework> getFireworkArray()
	{
		if (odd)
			return fireworks2;
		else
			return fireworks1;
	}

	public ArrayList<Firework> getOtherFireworkArray()
	{
		if (odd)
			return fireworks1;
		else
			return fireworks2;
	}
}
