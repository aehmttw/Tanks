package tanks;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;

import tanks.tank.Tank;

public class ScreenGame extends Screen
{
	public boolean playing = false;
	public boolean paused = false;

	public boolean shopScreen = false;
	public int shopPage = 0;
	public int rows = 6;
	public int yoffset = -150;

	public static boolean finished = false;
	public static double finishTimer = 100;
	public static double finishTimerMax = 100;

	public boolean cancelCountdown = false;
	public String name = null;

	public ArrayList<Item> shop = new ArrayList<Item>();
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

	Button enterShop = new Button(Drawing.interfaceSizeX-200, Drawing.interfaceSizeY-110, 350, 40, "Shop", new Runnable()
	{
		@Override
		public void run() 
		{
			cancelCountdown = true;
			shopScreen = true;
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

	Button resumeCrusade = new Button(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 - 30, 350, 40, "Continue playing", new Runnable()
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

	Button quitCrusade = new Button(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 + 30, 350, 40, "Quit to title", new Runnable()
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

	Button quitCrusadeFinalLife = new Button(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 + 30, 350, 40, "Quit to title", new Runnable()
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

	Button exitShop = new Button(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 + 300, 350, 40, "Exit shop", new Runnable()
	{
		@Override
		public void run() 
		{
			shopScreen = false;
		}
	}
			);

	Button next = new Button(Drawing.interfaceSizeX / 2 + 190, Drawing.interfaceSizeY / 2 + 240, 350, 40, "Next page", new Runnable()
	{
		@Override
		public void run() 
		{
			shopPage++;
		}
	}
			);

	Button previous = new Button(Drawing.interfaceSizeX / 2 - 190, Drawing.interfaceSizeY / 2 + 240, 350, 40, "Previous page", new Runnable()
	{
		@Override
		public void run() 
		{
			shopPage--;
		}
	}
			);

	ArrayList<Button> shopItemButtons = new ArrayList<Button>();

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

	public ScreenGame(ArrayList<Item> shop)
	{
		this();
		this.shop = shop;
		for (int i = 0; i < this.shop.size(); i++)
		{
			final int j = i;
			Item item = this.shop.get(j);

			String price = "Price: " + item.price + " ";
			if (item.price == 0)
				price = "Price: Free!";
			else if (item.price == 1)
				price += "coin";
			else
				price += "coins";

			this.shopItemButtons.add(new Button(0, 0, 350, 40, item.name, new Runnable()
			{
				@Override
				public void run() 
				{
					int pr = shop.get(j).price;
					if (Panel.panel.hotbar.currentCoins.coins >= pr)
					{
						if (Panel.panel.hotbar.currentItemBar.addItem(shop.get(j)))
							Panel.panel.hotbar.currentCoins.coins -= pr;
					}
				}
			}, price
					));
		}

		for (int i = 0; i < shopItemButtons.size(); i++)
		{
			int page = i / (rows * 3);
			int offset = 0;

			if (page * rows * 3 + rows < shopItemButtons.size())
				offset = -190;

			if (page * rows * 3 + rows * 2 < shopItemButtons.size())
				offset = -380;

			shopItemButtons.get(i).posY = Drawing.interfaceSizeY / 2 + yoffset + (i % rows) * 60;

			if (i / rows % 3 == 0)
				shopItemButtons.get(i).posX = Drawing.interfaceSizeX / 2 + offset;
			else if (i / rows % 3 == 1)
				shopItemButtons.get(i).posX = Drawing.interfaceSizeX / 2 + offset + 380;
			else
				shopItemButtons.get(i).posX = Drawing.interfaceSizeX / 2 + offset + 380 * 2;
		}
	}

	@Override
	public void update()
	{
		Panel.panel.hotbar.update();

		if (InputKeyboard.keys.contains(KeyEvent.VK_ESCAPE))
		{
			if (!Panel.pausePressed)
			{
				if (shopScreen)
					shopScreen = false;
				else
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
			{
				if (!Crusade.crusadeMode)
					newLevel.update();
			}
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

			if (!Crusade.crusadeMode)
				resume.update();
			else
				resumeCrusade.update();

			return;
		}

		if (!playing && Game.startTime >= 0)
		{
			if (shopScreen)
			{
				Panel.panel.hotbar.hidden = false;
				Panel.panel.hotbar.hideTimer = 100;

				this.exitShop.update();

				if (shopItemButtons.size() > (1 + shopPage) * rows * 3)
					next.update();

				if (shopPage > 0)
					this.previous.update();

				for (int i = 0; i < this.shopItemButtons.size(); i++)
					this.shopItemButtons.get(i).update();
			}
			else
			{
				if (!this.shop.isEmpty())
					enterShop.update();

				if (Game.autostart && !cancelCountdown)
					Game.startTime -= Panel.frameFrequency;

				play.update();
			}

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

			Panel.panel.hotbar.update();


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
			if (!m.drawBelow && ! m.drawAbove)
				m.draw(g);
		}

		for (int i = 0; i < Game.obstacles.size(); i++)
		{
			Obstacle o = Game.obstacles.get(i);
			if (!o.drawBelow)
				o.draw(g);
		}

		for (int i = 0; i < Game.movables.size(); i++)
		{
			Movable m = Game.movables.get(i);
			if (m.drawAbove)
				m.draw(g);
		}

		for (int i = 0; i < Game.effects.size(); i++)
			((Effect)Game.effects.get(i)).draw(g);

		
		if (!playing) 
		{
			if (Crusade.crusadeMode)
			{
				g.setColor(new Color(0, 0, 0, 127));
				Drawing.setFontSize(g, 100);
				Drawing.window.drawInterfaceText(g, Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2, "Battle " + (Crusade.currentCrusade.currentLevel + 1));
			}
			
			if (shopScreen)
			{				
				this.exitShop.draw(g);

				if (shopItemButtons.size() > (1 + shopPage) * rows * 3)
					next.update();

				if (shopPage > 0)
					this.previous.update();

				for (int i = 0; i < this.shopItemButtons.size(); i++)
					this.shopItemButtons.get(i).draw(g);
			}
			else
			{
				if (!this.shop.isEmpty())
					enterShop.draw(g);

				play.draw(g);

				if (Game.autostart && !cancelCountdown)
				{
					g.setColor(Color.gray);
					Drawing.window.fillInterfaceRect(g, Drawing.interfaceSizeX-200, Drawing.interfaceSizeY-27.5, 350, 5);
					g.setColor(Color.orange);
					Drawing.window.fillInterfaceProgressRect(g, Drawing.interfaceSizeX-200, Drawing.interfaceSizeY-27.5, 350, 5, Game.startTime / 400);
				}
			}
		}

		Panel.panel.hotbar.draw(g);

		if (paused && !screenshotMode)
		{
			g.setColor(new Color(127, 178, 228, 64));
			g.fillRect(0, 0, (int) (Game.window.getSize().getWidth()) + 1, (int) (Game.window.getSize().getHeight()) + 1);

			if (name == null)
			{
				if (!Crusade.crusadeMode)
					newLevel.draw(g);
			}
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

			if (!Crusade.crusadeMode)
				resume.draw(g);
			else
				resumeCrusade.draw(g);

			g.setColor(Color.black);
			Drawing.window.drawInterfaceText(g, Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 - 150, "Game paused");
		}

	}

}
