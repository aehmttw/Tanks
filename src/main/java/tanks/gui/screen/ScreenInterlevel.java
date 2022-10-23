package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.*;
import tanks.gui.Button;
import tanks.gui.Firework;
import tanks.gui.SpeedrunTimer;

import java.util.ArrayList;
import java.util.Date;

public class ScreenInterlevel extends Screen implements IDarkScreen
{
	public static boolean tutorialInitial = false;
	public static boolean fromSavedLevels = false;
	public static boolean fromModdedLevels = false;
	public static boolean tutorial = false;

	boolean odd = false;

	ArrayList<Firework> fireworks1 = new ArrayList<>();
	ArrayList<Firework> fireworks2 = new ArrayList<>();

	public static double firework_frequency = 0.08;

	Button replay = new Button(this.centerX, this.centerY, this.objWidth, this.objHeight, "Replay the level", () ->
	{
		Level level = new Level(Game.currentLevelString);
		level.loadLevel();
		Game.screen = new ScreenGame();
	}
	);

	Button replayCrusade = new Button(this.centerX, this.centerY - this.objYSpace / 2, this.objWidth, this.objHeight, "Try again", () ->
	{
		Crusade.currentCrusade.retry = true;
		Crusade.currentCrusade.loadLevel();
		Game.screen = new ScreenGame(Crusade.currentCrusade.getShop());
	}
	);

	Button replayModLevel = new Button(this.centerX, this.centerY - this.objYSpace / 2, this.objWidth, this.objHeight, "Replay the level", () ->
	{
		fromModdedLevels = false;
		try
		{
			assert Game.currentLevel != null;
			Game.currentLevel = Game.currentLevel.getClass().getConstructor().newInstance();
			Game.currentLevel.loadLevel();

		}
		catch (Exception e)
		{
			Game.exitToCrash(e.getCause());
		}
	});

	Button replayTutorial = new Button(this.centerX, this.centerY + this.objYSpace / 2, this.objWidth, this.objHeight, "Try again", () -> new Tutorial().loadTutorial(!Panel.win && tutorialInitial, Game.game.window.touchscreen)
	);

	Button replayTutorial2 = new Button(this.centerX, this.centerY, this.objWidth, this.objHeight, "Try again", () -> new Tutorial().loadTutorial(!Panel.win && tutorialInitial, Game.game.window.touchscreen)
	);

	Button quitTutorial = new Button(this.centerX, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "Back to title", () ->
	{
		tutorial = false;
		Game.exitToTitle();
	}
	);

	Button replayCrusadeWin = new Button(this.centerX, this.centerY, this.objWidth, this.objHeight, "Replay the level", () ->
	{
		Crusade.currentCrusade.loadLevel();
		Crusade.currentCrusade.replay = true;
		Game.screen = new ScreenGame(Crusade.currentCrusade.getShop());
	}
			, "You will not gain extra lives---"
			+ "from replaying a level you've already cleared.---"
			+ "However, you can still earn coins!---"
			+ "You will still lose a life if you're destroyed.");

	Button save = new Button(0, 0, this.objHeight * 1.5, this.objHeight * 1.5, "", () ->
	{
		ScreenSaveLevel sc = new ScreenSaveLevel(System.currentTimeMillis() + "", Game.currentLevelString, Game.screen);
		Level lev = new Level(Game.currentLevelString);

		if (Crusade.crusadeMode)
			lev.customTanks = Crusade.currentCrusade.customTanks;

		lev.preview = true;
		lev.loadLevel(sc);
		Game.screen = sc;

		sc.fromInterlevel = true;
		sc.music = music;
		sc.musicID = musicID;
		sc.updateDownloadButton();
	}
	);

	Button newLevel = new Button(this.centerX, this.centerY - this.objYSpace * 1, this.objWidth, this.objHeight, "Generate a new level", () ->
	{
		Game.cleanUp();
		Game.loadRandomLevel();
		Game.screen = new ScreenGame();
	}
	);

	Button nextLevel = new Button(this.centerX, this.centerY - this.objYSpace * 1, this.objWidth, this.objHeight, "Next level", () ->
	{
		Crusade.currentCrusade.currentLevel++;
		Crusade.currentCrusade.replay = false;
		Crusade.currentCrusade.retry = false;
		Crusade.currentCrusade.loadLevel();
		Game.screen = new ScreenGame(Crusade.currentCrusade.getShop());
	}
	);

	Button quitCrusadeEnd = new Button(this.centerX, this.centerY + this.objYSpace * 0, this.objWidth, this.objHeight, "Continue", () ->
	{
		if (Panel.win)
			Crusade.currentCrusade.currentLevel++;

		Game.cleanUp();
		Game.screen = new ScreenCrusadeStats(Crusade.currentCrusade, Crusade.currentCrusade.getCrusadePlayer(Game.player), true);
	}
	);

	Button quit = new Button(this.centerX, this.centerY + this.objYSpace * 1, this.objWidth, this.objHeight, "Quit to title", Game::exitToTitle
	);

	Button back = new Button(this.centerX, this.centerY + this.objYSpace / 2, this.objWidth, this.objHeight, "Back to my levels", () ->
	{
		Game.cleanUp();
		System.gc();
		Game.screen = new ScreenPlaySavedLevels();
		fromSavedLevels = false;
	}
	);

	Button backMinigame = new Button(this.centerX, this.centerY + this.objYSpace / 2, this.objWidth, this.objHeight, "Back to minigames", () ->
	{
		Game.cleanUp();
		System.gc();
		Game.screen = new ScreenMinigames();
		fromModdedLevels = false;
	}
	);

	Button exitTutorial = new Button(this.centerX, this.centerY + this.objYSpace / 2, this.objWidth, this.objHeight, "Let's go!", () ->
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
	);

	Button quitCrusade = new Button(this.centerX, this.centerY + this.objYSpace * 1, this.objWidth, this.objHeight, "Quit to title", () ->
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
		else if (fromModdedLevels)
		{
			skip = true;
			replayModLevel.update();
			backMinigame.update();
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

					if (Crusade.currentCrusade.respawnTanks)
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
		if (Game.followingCam)
			Game.game.window.setCursorPos(Panel.windowWidth / 2, Panel.windowHeight / 2);

		Game.player.hotbar.percentHidden = 100;

		save.image = "icons/save.png";

		save.imageSizeX = this.objHeight;
		save.imageSizeY = this.objHeight;

		if (Crusade.crusadeMode)
			Crusade.currentCrusade.levelFinished(Panel.win);

		this.musicID = "interlevel";

		if (Panel.win)
		{
			//Drawing.drawing.playSound("win.ogg");
			this.music = "win_music.ogg";

			if (Crusade.crusadeMode && Crusade.currentCrusade.win)
				this.music = "win_crusade.ogg";

			if (Crusade.crusadeMode && !Crusade.currentCrusade.respawnTanks)
			{
				this.nextLevel.posY += this.objYSpace / 2;
				this.quitCrusade.posY -= this.objYSpace / 2;
			}
		}
		else
		{
			//Drawing.drawing.playSound("lose.ogg");
			this.music = "lose_music.ogg";

			if (!(Crusade.crusadeMode && Crusade.currentCrusade.replay))
				quitCrusade.posY -= this.objYSpace / 2;

			//if (Crusade.crusadeMode && Crusade.currentCrusade.lose)
			//	this.music = "lose_crusade.ogg";
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

		save.posX = (Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2
				+ Drawing.drawing.interfaceSizeX - 50 * Drawing.drawing.interfaceScaleZoom - Game.game.window.getEdgeBounds() / Drawing.drawing.interfaceScale;
		save.posY = ((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2
				+ Drawing.drawing.interfaceSizeY - 50 * Drawing.drawing.interfaceScaleZoom;

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
		else if (fromModdedLevels)
		{
			skip = true;
			replayModLevel.draw();
			backMinigame.draw();
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
				quitCrusade.draw();

				if (Panel.win || Crusade.currentCrusade.replay)
				{
					nextLevel.draw();

					if (Crusade.currentCrusade.respawnTanks)
						replayCrusadeWin.draw();
				}
				else
					replayCrusade.draw();
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
				Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace / 2, "Congratulations! You are now ready to play!");
			else
				Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace / 2, Panel.winlose);
		}
		else if (Crusade.crusadeMode)
		{
			if (Crusade.currentCrusade.win)
				Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 19 / 6, "You finished the crusade!");
			else if (Crusade.currentCrusade.lose)
				Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 19 / 6, "Game over!");
			else
				Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 19 / 6, Panel.winlose);
		}
		else
			Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, Panel.winlose);

		Drawing.drawing.setInterfaceFontSize(this.textSize);

		if (Crusade.crusadeMode)
		{
			Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, "Lives remaining: %d", Game.player.remainingLives);

			if (Crusade.currentCrusade.lifeGained)
			{
				double frac = 25.0 / 6;

				if (Drawing.drawing.interfaceScaleZoom > 1)
					frac = 23.0 / 6;

				Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * frac, "You gained a life for clearing Battle %d!", (Crusade.currentCrusade.currentLevel + 1));
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
