package tanks;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;

import tanks.item.ItemBar;
import tanks.tank.Tank;

public class ScreenGame extends Screen
{
	public boolean playing = false;
	public boolean paused = false;
	public static boolean finished = false;
	public static double finishTimer = 100;
	public static double finishTimerMax = 100;
	public String name = null;

	public boolean screenshotMode = false;

	Button play = new Button(Drawing.interfaceSizeX-200, Drawing.interfaceSizeY-50, 350, 40, "Play", new Runnable()
	{
		@Override
		public void run() 
		{
			playing = true;
			Game.player.cooldown = 20;
		}
	}
			);

	Button resume = new Button(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 - 60, 350, 40, "Continue playing", new Runnable()
	{
		@Override
		public void run() 
		{
			paused = false;
			Game.player.cooldown = 20;
		}
	}
			);

	Button newLevel = new Button(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2, 350, 40, "Generate a new level", new Runnable()
	{
		@Override
		public void run() 
		{
			playing = false;
			Game.startTime = 400;
			paused = false;
			Game.reset();
		}
	}
			);

	Button edit = new Button(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2, 350, 40, "Edit the level", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.exitToTitle();
			ScreenLevelBuilder s = new ScreenLevelBuilder(name);
			Game.loadLevel(new File(Game.homedir + ScreenSavedLevels.levelDir + "/" + name), s);
			Game.screen = s;
		}
	}
			);

	Button quit = new Button(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 + 60, 350, 40, "Quit to title", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.exitToTitle();
		}
	}
			);

	Button quitCrusade = new Button(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 + 60, 350, 40, "Quit to title", new Runnable()
	{
		@Override
		public void run() 
		{
			Crusade.crusadeMode = false;
			Crusade.currentCrusade.remainingLives--;
			Game.exitToTitle();
		}
	}
	, "Note! You will lose a life for quitting---in the middle of a level------You will be able to return to the crusade---through the crusade button on---the play screen.");

	Button quitCrusadeFinalLife = new Button(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 + 60, 350, 40, "Quit to title", new Runnable()
	{
		@Override
		public void run() 
		{
			Crusade.crusadeMode = false;
			Crusade.currentCrusade = null;
			Game.exitToTitle();
		}
	}
	, "Note! You will lose a life for quitting---in the middle of a level------Since you do not have any other lives left,---your progress will be lost!");
	
	private final ItemBar itemBar = new ItemBar();

	public ScreenGame()
	{
		Game.startTime = 400;
		ScreenGame.finishTimer = ScreenGame.finishTimerMax;
	}

	public ScreenGame(String s)
	{
		this();
		this.name = s;
	}

	@Override
	public void update()
	{
		if (InputKeyboard.keys.contains(KeyEvent.VK_ESCAPE))
		{
			if (!Panel.pausePressed)
			{
				this.paused = !this.paused;
			}

			Panel.pausePressed = true;
		}
		else
			Panel.pausePressed = false;

		if (InputKeyboard.validKeys.contains(KeyEvent.VK_F1))
		{
			this.screenshotMode = !this.screenshotMode;
			InputKeyboard.validKeys.remove((Integer)KeyEvent.VK_F1);
		}

		if (InputKeyboard.validKeys.contains(KeyEvent.VK_I))
		{
			Drawing.movingCamera = !Drawing.movingCamera ;
			InputKeyboard.validKeys.remove((Integer)KeyEvent.VK_I);
		}

		if (paused)
		{
			if (name == null)
				newLevel.update();
			else
				edit.update();

			if (!Crusade.crusadeMode)
				quit.update();
			else
			{
				if (Crusade.currentCrusade.remainingLives > 1)
					quitCrusade.update();
				else
					quitCrusadeFinalLife.update();
			}

			resume.update();
			return;
		}

		if (!playing && Game.startTime >= 0)
		{
			if (Game.autostart)
				Game.startTime -= Panel.frameFrequency;

			play.update();

			if (!finished)
			{
				Obstacle.draw_size = Math.min(Game.tank_size, Obstacle.draw_size + Panel.frameFrequency);
			}
		}
		else
		{
			playing = true;

			//System.out.println(Panel.frameFrequency);

			Obstacle.draw_size = Math.min(Obstacle.obstacle_size, Obstacle.draw_size);
			ArrayList<Team> aliveTeams = new ArrayList<Team>();

			for (int i = 0; i < Game.movables.size(); i++)
			{
				Movable m = Game.movables.get(i);
				m.update();

				if (m instanceof Tank)
				{
					if (m.team == null)
						aliveTeams.add(new Team("null"));
					else if (!aliveTeams.contains(m.team))
						aliveTeams.add(m.team);
				}
			}

			for (int i = 0; i < Game.effects.size(); i++)
			{
				Game.effects.get(i).update();
			}

			for (int i = 0; i < Game.belowEffects.size(); i++)
			{
				Game.belowEffects.get(i).update();
			}
			
			if (Crusade.crusadeMode)
			{
				Crusade.currentCrusade.itemBar.update();
			}

			if (aliveTeams.size() <= 1)
			{
				ScreenGame.finished = true;
				Game.bulletLocked = true;

				if (ScreenGame.finishTimer > 0)
				{
					ScreenGame.finishTimer -= Panel.frameFrequency;
					if (ScreenGame.finishTimer < 0)
						ScreenGame.finishTimer = 0;
				}
				else
				{
					boolean noMovables = true;

					for (int m = 0; m < Game.movables.size(); m++)
					{
						Movable mo = Game.movables.get(m);
						if (mo instanceof Bullet || mo instanceof Mine)
						{
							noMovables = false;
							mo.destroy = true;
						}
					}

					if (Game.effects.size() <= 0 && noMovables)
					{
						Obstacle.draw_size = Math.max(0, Obstacle.draw_size - Panel.frameFrequency);
						for (int i = 0; i < Game.movables.size(); i++)
							Game.movables.get(i).destroy = true;

						if (Obstacle.draw_size <= 0)
						{
							if (aliveTeams.contains(Game.player.team))
							{
								Panel.winlose = "Victory!";
								Panel.win = true;
							}
							else
							{
								Panel.winlose = "You were destroyed!";
								Panel.win = false;
							}

							if (name != null)
								Game.exit(name);
							else
								Game.exit();
						}
					}
				}
			}
			else
				Game.bulletLocked = false;
		}
		
		itemBar.update();

		for (int i = 0; i < Game.removeMovables.size(); i++)
			Game.movables.remove(Game.removeMovables.get(i));

		for (int i = 0; i < Game.removeObstacles.size(); i++)
			Game.obstacles.remove(Game.removeObstacles.get(i));

		for (int i = 0; i < Game.removeEffects.size(); i++)
		{
			Effect e = Game.removeEffects.get(i);
			Game.effects.remove(e);
			Game.recycleEffects.add(e);

		}

		for (int i = 0; i < Game.removeBelowEffects.size(); i++)
		{
			Effect e = Game.removeBelowEffects.get(i);
			Game.belowEffects.remove(e);
			Game.recycleEffects.add(e);
		}

		Game.removeMovables.clear();
		Game.removeObstacles.clear();
		Game.removeEffects.clear();
		Game.removeBelowEffects.clear();

	}

	@Override
	public void draw(Graphics g)
	{
		this.drawDefaultBackground(g);

		for (int i = 0; i < Game.obstacles.size(); i++)
		{
			Obstacle o = Game.obstacles.get(i);
			if (o.drawBelow)
				o.draw(g);
		}

		for (int i = 0; i < Game.belowEffects.size(); i++)
			Game.belowEffects.get(i).draw(g);

		for (int i = 0; i < Game.movables.size(); i++)
		{
			Movable m = Game.movables.get(i);
			if (m.drawBelow)
				m.draw(g);
		}
		
		for (int i = 0; i < Game.movables.size(); i++)
		{
			Movable m = Game.movables.get(i);
			if (!m.drawBelow)
				m.draw(g);
		}
		
		for (int i = 0; i < Game.obstacles.size(); i++)
		{
			Obstacle o = Game.obstacles.get(i);
			if (!o.drawBelow)
				o.draw(g);
		}
		
		for (int i = 0; i < Game.effects.size(); i++)
			((Effect)Game.effects.get(i)).draw(g);
		
		itemBar.draw(g);

		if (!playing) 
		{
			play.draw(g);

			if (Crusade.crusadeMode)
			{
				g.setColor(new Color(0, 0, 0, 127));
				Drawing.setFontSize(g, 100);
				Drawing.window.drawInterfaceText(g, Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2, "Battle " + (Crusade.currentCrusade.currentLevel + 1));
			}
		}

		if (paused && !screenshotMode)
		{
			g.setColor(new Color(127, 178, 228, 64));
			g.fillRect(0, 0, (int) (Game.window.getSize().getWidth()) + 1, (int) (Game.window.getSize().getHeight()) + 1);

			if (name == null)
				newLevel.draw(g);
			else
				edit.draw(g);

			if (!Crusade.crusadeMode)
				quit.draw(g);
			else
			{
				if (Crusade.currentCrusade.remainingLives > 1)
					quitCrusade.draw(g);
				else
					quitCrusadeFinalLife.draw(g);
			}			
			resume.draw(g);
			g.setColor(Color.black);
			Drawing.window.drawInterfaceText(g, Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 - 150, "Game paused");
		}

	}

}
