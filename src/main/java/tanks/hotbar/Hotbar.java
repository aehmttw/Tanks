package tanks.hotbar;

import tanks.*;
import tanks.gui.Button;
import tanks.gui.screen.ScreenGame;
import tanks.item.ItemBullet;
import tanks.item.ItemMine;
import tanks.obstacle.Obstacle;
import tanks.tank.Tank;
import tanks.tank.TankModels;
import tanks.tank.TankPlayerController;
import tanks.translation.Translation;

public class Hotbar
{
	public ItemBar itemBar;
	public int coins;

	public boolean enabledAmmunitionBar = true;
	public boolean enabledItemBar = true;
	public boolean enabledHealthBar = true;
	public boolean enabledCoins = false;
	public boolean enabledRemainingEnemies = true;

	public boolean hidden = true;
	public boolean persistent = false;

	public double percentHidden = 100;
	public double verticalOffset = 0;

	public double hideTimer = 0;

	public static double bar_width = 410;

	public static Button toggle;

	public double timeSinceBulletChange = 0;
	public double timeSinceMineChange = 0;
	public double timeSinceHealthChange = 0;
	public boolean ignoreInitialStats = false;

	public double healthFadeTime = 200;

	public double lastLiveBullets = 0;
	public double lastMines = 0;
	public double lastHealth = 0;
	public double lastCooldownFrac = 0;
	public double lastCooldownFrac2 = 0;

	public double circleVisibility = 0;
	public double circleVisibilityMax = 50;

	public double circlePersistenceVisibility = 0;

	public static boolean circular = true;

	public Hotbar(Player p)
	{
		this.itemBar = new ItemBar(p);
	}

	public void update()
	{
		Hotbar.toggle.posX = Drawing.drawing.interfaceSizeX / 2;
		Hotbar.toggle.posY = (int) (Drawing.drawing.getInterfaceEdgeY(true) - 12);

		if (Game.game.window.touchscreen)
		{
			this.verticalOffset = 20;
			toggle.update();
		}
		else
			this.verticalOffset = 0;

		if (this.persistent && !Hotbar.circular)
			this.hidden = false;

		if (Hotbar.circular && Game.screen instanceof ScreenGame && !(((ScreenGame) Game.screen).shopScreen || ((ScreenGame) Game.screen).buildsScreen))
			this.hidden = true;

		if (ScreenGame.finished)
			this.hidden = true;

		if (Game.screen instanceof ScreenGame)
		{
			ScreenGame s = (ScreenGame) Game.screen;
			if (s.paused || s.shopScreen || s.buildsScreen)
				circleVisibility -= Panel.frameFrequency;
			else
				circleVisibility += Panel.frameFrequency;
		}

		circleVisibility = Math.min(circleVisibility, Obstacle.draw_size / Game.tile_size * circleVisibilityMax);
		circleVisibility = Math.max(0, Math.min(circleVisibility, circleVisibilityMax));

		if (this.persistent)
			this.circlePersistenceVisibility += Panel.frameFrequency / 50;
		else
			this.circlePersistenceVisibility -= Panel.frameFrequency / 50;

		this.circlePersistenceVisibility = Math.max(0, Math.min(1, this.circlePersistenceVisibility));

		this.hideTimer = Math.max(0, this.hideTimer - Panel.frameFrequency);

		if (this.hideTimer <= 0 && !this.persistent)
			this.hidden = true;

		if (this.hidden)
			this.percentHidden = Math.min(100, this.percentHidden + Panel.frameFrequency);
		else
			this.percentHidden = Math.max(0, this.percentHidden - 4 * Panel.frameFrequency);

		if (Game.game.input.hotbarToggle.isValid())
		{
			Game.game.input.hotbarToggle.invalidate();
			this.persistent = !this.persistent;
		}

		timeSinceBulletChange += Panel.frameFrequency;
		timeSinceMineChange += Panel.frameFrequency;
		timeSinceHealthChange += Panel.frameFrequency;

		this.itemBar.update();
	}

	public void draw()
	{
		if (Game.game.window.touchscreen)
		{
			int y = (int) (Drawing.drawing.getInterfaceEdgeY(true) - 12);

			Drawing.drawing.setColor(255, 255, 255, 64);

			if (!this.persistent)
				Drawing.drawing.drawInterfaceImage("icons/widearrow.png", Drawing.drawing.interfaceSizeX / 2, y, 64, 16);
			else
				Drawing.drawing.drawInterfaceImage("icons/widearrow.png", Drawing.drawing.interfaceSizeX / 2, y, 64, -16);
		}

		this.itemBar.draw();

		if (this.enabledHealthBar)
		{
			int x = (int) ((Drawing.drawing.interfaceSizeX / 2));
			int y = (int) (Drawing.drawing.getInterfaceEdgeY(true) - 25 + percentHidden - verticalOffset);

			Drawing.drawing.setColor(0, 0, 0, 128 * (100 - this.percentHidden) / 100.0);

			if (Level.isDark())
				Drawing.drawing.setColor(255, 255, 255, 128 * (100 - this.percentHidden) / 100.0);

			Drawing.drawing.fillInterfaceRect(x, y, bar_width, 5);
			Drawing.drawing.setColor(255, 128, 0, (100 - this.percentHidden) * 2.55);

			double lives = 0;
			int shields = 0;

			if (Game.playerTank != null)
			{
				lives = Game.playerTank.health % 1.0;
				if (lives == 0 && Game.playerTank.health > 0)
					lives = 1;

				if (Game.playerTank.destroy && Game.playerTank.health < 1)
					lives = 0;

				shields = (int) (Game.playerTank.health - lives);
			}

			Drawing.drawing.fillInterfaceProgressRect(x, y, bar_width, 5, lives);

			if (shields > 0)
			{
				Drawing.drawing.setColor(255, 0, 0, (100 - this.percentHidden) * 2.55);
				Drawing.drawing.fillInterfaceOval(x - bar_width / 2, y, 18, 18);
				//Drawing.drawing.drawImage("shield.png", x - bar_width / 2, y + 1, 14, 14);
				Drawing.drawing.setInterfaceFontSize(12);
				Drawing.drawing.setColor(255, 255, 255, (100 - this.percentHidden) * 2.55);
				Drawing.drawing.drawInterfaceText(x - bar_width / 2, y, shields + "");
			}
		/*	else
			{
				Drawing.drawing.setColor(0, 160, 0);
				Drawing.drawing.drawImage("emblems/medic.png", x - bar_width / 2, y, 14, 14);
			}*/
		}

		if (this.enabledAmmunitionBar)
		{
			int x = (int) ((Drawing.drawing.interfaceSizeX / 2));
			int y = (int) (Drawing.drawing.getInterfaceEdgeY(true) - 10 + percentHidden - verticalOffset);

			Drawing.drawing.setColor(0, 0, 0, 128 * (100 - this.percentHidden) / 100.0);

			if (Level.isDark())
				Drawing.drawing.setColor(255, 255, 255, 128 * (100 - this.percentHidden) / 100.0);

			Drawing.drawing.fillInterfaceRect(x, y, bar_width, 5);

			int hotbarSlots = 0;
			if (Game.playerTank != null)
				hotbarSlots = (this.itemBar.showItems ? ItemBar.item_bar_size : 0);

			int live = 1;
			int max = 1;
			double cooldownFrac = 0;

			if (Game.playerTank instanceof TankPlayerController)
			{
				TankPlayerController p = ((TankPlayerController) Game.playerTank);
				live = p.liveBullets;
				max = p.maxLiveBullets;
				cooldownFrac = p.bulletCooldown / p.bulletCooldownBase;
			}
			else
			{
				ItemBullet.ItemStackBullet ib = null;
				if (Game.playerTank != null && !Game.playerTank.destroy && Game.playerTank.getPrimaryAbility() instanceof ItemBullet.ItemStackBullet)
					ib = (ItemBullet.ItemStackBullet) Game.playerTank.getPrimaryAbility();

				if (this.enabledItemBar && this.itemBar.selected != -1 && this.itemBar.selected < hotbarSlots && this.itemBar.slots[this.itemBar.selected] instanceof ItemBullet.ItemStackBullet)
					ib = (ItemBullet.ItemStackBullet) this.itemBar.slots[this.itemBar.selected];

				if (ib != null)
				{
					live = ib.liveBullets;
					max = ib.item.bullet.maxLiveBullets;

					if (ib.stackSize > 0 && ib.stackSize < max)
						max = Math.min(max, ib.stackSize + live);

					if (ib.destroy)
					{
						max = 1;
						live = 1;
					}

					cooldownFrac = ib.cooldown / ib.item.cooldownBase;
				}
			}

			double ammo = live * 1.0 / max;
			double ammo2 = (live - cooldownFrac) / max;

			if (max <= 0)
			{
				max = 1;
				ammo = 0;
				ammo2 = -cooldownFrac;
			}

			if (Game.playerTank != null && Game.playerTank.destroy)
			{
				ammo = 1;
				ammo2 = 1;
				max = 1;
			}

			Drawing.drawing.setColor(0, 255, 255, (100 - this.percentHidden) * 2.55);
			Drawing.drawing.fillInterfaceProgressRect(x, y, bar_width, 5, Math.min(1, Math.max(0, 1 - ammo2)));

			Drawing.drawing.setColor(0, 200, 255, (100 - this.percentHidden) * 2.55);
			Drawing.drawing.fillInterfaceProgressRect(x, y, bar_width, 5,  Math.min(1, Math.max(0, 1 - ammo)));

			Drawing.drawing.setColor(0, 255, 255, (100 - this.percentHidden) * 2.55);
			Drawing.drawing.fillInterfaceProgressRect(x, y, bar_width, 5, Math.min(1, Math.max(0, -ammo2 * max)));

			Drawing.drawing.setColor(0, 0, 0, 128 * (100 - this.percentHidden) / 100.0);

			if (max <= 100)
			{
				for (int i = 1; i < max; i++)
				{
					double frac = i * 1.0 / max;
					Drawing.drawing.fillInterfaceRect(x - bar_width / 2 + frac * bar_width, y, 2, 5);
				}
			}

			if (Game.playerTank != null && !Game.playerTank.destroy)
			{
				int mines = 0;

				if (Game.playerTank instanceof TankPlayerController)
				{
					TankPlayerController p = ((TankPlayerController) Game.playerTank);
					mines = p.maxLiveMines - p.liveMines;
				}
				else
				{
					ItemMine.ItemStackMine m = null;

					if (Game.playerTank.getSecondaryAbility() instanceof ItemMine.ItemStackMine)
						m = (ItemMine.ItemStackMine) Game.playerTank.getSecondaryAbility();

					if (this.enabledItemBar && this.itemBar.selected != -1 && this.itemBar.selected < hotbarSlots && this.itemBar.slots[this.itemBar.selected] instanceof ItemMine.ItemStackMine)
						m = (ItemMine.ItemStackMine) this.itemBar.slots[this.itemBar.selected];

					if (m != null)
					{
						mines = m.item.mine.maxLiveMines - m.liveMines;

						if (m.stackSize > 0)
							mines = Math.min(m.stackSize, mines);

						if (m.destroy)
							mines = 0;
					}
				}

				if (mines > 0)
				{
					Drawing.drawing.setColor(255, 0, 0, (100 - this.percentHidden) * 2.55);
					Drawing.drawing.fillInterfaceOval(x + bar_width / 2, y, 18, 18);

					Drawing.drawing.setColor(255, 255, 0, (100 - this.percentHidden) * 2.55);
					Drawing.drawing.fillInterfaceOval(x + bar_width / 2, y, 14, 14);

					Drawing.drawing.setInterfaceFontSize(12);
					Drawing.drawing.setColor(0, 0, 0, (100 - this.percentHidden) * 2.55);

					Drawing.drawing.drawInterfaceText(x + bar_width / 2, y, mines + "");
				}
			}
		}

		if (this.enabledCoins)
		{
			Drawing.drawing.setInterfaceFontSize(18);
			Drawing.drawing.setColor(0, 0, 0, (100 - this.percentHidden) * 2.55);

			if (Level.isDark())
				Drawing.drawing.setColor(255, 255, 255, (100 - this.percentHidden) * 2.55);

			Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.getInterfaceEdgeY(true) - 100 + percentHidden - verticalOffset, "Coins: %d", coins);
		}

		if (this.enabledRemainingEnemies)
		{
			int count = 0;

			for (Movable m : Game.movables)
			{
				if (m instanceof Tank && !Team.isAllied(Game.playerTank, m) && !m.destroy && ((Tank)m).mandatoryKill)
					count++;
			}

			int x = (int) ((Drawing.drawing.interfaceSizeX / 2) - bar_width / 2 - 35);
			int y = (int) (Drawing.drawing.getInterfaceEdgeY(true) - 17.5 + percentHidden - verticalOffset);

			Drawing.drawing.setColor(159, 32, 32, (100 - this.percentHidden) * 2.55);
			Drawing.drawing.drawInterfaceModel(TankModels.tank.base, x, y, Game.tile_size / 2, Game.tile_size / 2, 0);

			Drawing.drawing.setColor(255, 0, 0, (100 - this.percentHidden) * 2.55);
			Drawing.drawing.drawInterfaceModel(TankModels.tank.color, x, y, Game.tile_size / 2, Game.tile_size / 2, 0);

			Drawing.drawing.setColor(159, 32, 32, (100 - this.percentHidden) * 2.55);

			Drawing.drawing.drawInterfaceModel(TankModels.tank.turret, x, y, Game.tile_size / 2, Game.tile_size / 2, 0);

			Drawing.drawing.setColor(207, 16, 16, (100 - this.percentHidden) * 2.55);
			Drawing.drawing.drawInterfaceModel(TankModels.tank.turretBase, x, y, Game.tile_size / 2, Game.tile_size / 2, 0);

			Drawing.drawing.setColor(255, 0, 0, (100 - this.percentHidden) * 2.55);
			Drawing.drawing.setInterfaceFontSize(24);
			Drawing.drawing.drawInterfaceText(x - 20, y, "" + count, true);
		}

		if (Game.currentLevel != null && (Game.currentLevel.timed && Game.screen instanceof ScreenGame))
		{
			int secondsTotal = (int) (((ScreenGame) Game.screen).timeRemaining / 100 + 0.5);
			double secondsFrac = (((ScreenGame) Game.screen).timeRemaining / 100 + 0.5) - secondsTotal;

			int seconds60 = secondsTotal % 60;
			int minutes = secondsTotal / 60;

			double sizeMul = 1;
			double alpha = 127;
			double red = 0;

			if (((ScreenGame) Game.screen).playing)
			{
				if (secondsTotal == 60 || secondsTotal == 30 || secondsTotal <= 10)
				{
					sizeMul = 1.5;

					if (secondsFrac > 0.4 && secondsFrac <= 0.8 && secondsTotal > 9)
						alpha = 0;

					if (secondsTotal <= 9)
						red = Math.max(0, secondsFrac * 2 - 1) * 255;

					if (secondsTotal <= 5 && red == 0)
						red = Math.max(0, secondsFrac * 2) * 255;
				}
				else if (secondsTotal == 59 || secondsTotal == 29)
					sizeMul = 1.0 + Math.max(((((ScreenGame) Game.screen).timeRemaining / 100) - secondsTotal), 0);
			}

			String st = Translation.translate("Time: ");
			String s = st + minutes + ":" + seconds60;
			if (seconds60 < 10)
				s = st + minutes + ":0" + seconds60;

			Drawing.drawing.setInterfaceFontSize(32 * sizeMul);
			Drawing.drawing.setColor(red, 0, 0, (alpha + red / 2) * Obstacle.draw_size / Game.tile_size);

			if (Level.isDark())
				Drawing.drawing.setColor(255, 255 - red, 255 - red, (alpha + red / 2) * Obstacle.draw_size / Game.tile_size);

			double posX = Drawing.drawing.interfaceSizeX / 2;
			double posY = 50;

			if (ScreenGame.finishedQuick)
			{
				Drawing.drawing.setInterfaceFontSize(32);
				Drawing.drawing.setColor(0, 0, 0, 127 * Obstacle.draw_size / Game.tile_size);

				if (Level.isDark())
					Drawing.drawing.setColor(255, 255, 255, 127);
			}

			if (((ScreenGame) Game.screen).timeRemaining <= 0)
			{
				Drawing.drawing.setInterfaceFontSize(100);
				Drawing.drawing.setColor(127, 0, 0, 255 * Obstacle.draw_size / Game.tile_size);
				Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2 + 8, Drawing.drawing.interfaceSizeY / 2 + 8, "Out of time!");
				Drawing.drawing.setColor(255, 0, 0, 255 * Obstacle.draw_size / Game.tile_size);
				Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, "Out of time!");
			}
			else
				Drawing.drawing.displayInterfaceText(posX, posY, s);
		}
	}

	public void drawCircle()
	{
		if (Game.playerTank == null || Game.game.window.drawingShadow)
			return;

		double mx = Drawing.drawing.getInterfaceMouseX();
		double my = Drawing.drawing.getInterfaceMouseY();

		double px = Drawing.drawing.gameToInterfaceCoordsX(Game.playerTank.posX);
		double py = Drawing.drawing.gameToInterfaceCoordsY(Game.playerTank.posY);

		double healthSize = 120;

		double size = 60;
		double thickness = 10;
		int hotbarSlots = (this.itemBar.showItems ? ItemBar.item_bar_size : 0);

		this.itemBar.drawCircle();

		if (this.enabledHealthBar)
		{
			if (lastHealth != Game.playerTank.health)
			{
				lastHealth = Game.playerTank.health;
				if (lastHealth > 0 && !ignoreInitialStats)
					timeSinceHealthChange = 0;
			}

			double opacity = circleVisibility / circleVisibilityMax * Math.min(1, (Game.playerTank.health == 0 ? 0 : this.circlePersistenceVisibility) + Math.max(0, 2 - timeSinceHealthChange / healthFadeTime));
			Drawing.drawing.setColor(0, 0, 0, 128 * opacity);

			if (Level.isDark())
				Drawing.drawing.setColor(255, 255, 255, 128 * opacity);

			Drawing.drawing.fillPartialRing(Game.playerTank.posX, Game.playerTank.posY, Game.tile_size / 2 + Game.playerTank.posZ, healthSize, thickness, 0, 1);
			Drawing.drawing.setColor(255, 128, 0, 255 * opacity);

			double lives = 0;
			int shields = 0;

			if (Game.playerTank != null)
			{
				lives = Game.playerTank.health % 1.0;
				if (lives == 0 && Game.playerTank.health > 0)
					lives = 1;

				if (Game.playerTank.destroy && Game.playerTank.health < 1)
					lives = 0;

				shields = (int) (Game.playerTank.health - lives);
			}

			Drawing.drawing.fillPartialRing(Game.playerTank.posX, Game.playerTank.posY, Game.tile_size / 2 + Game.playerTank.posZ, healthSize, thickness, 0, lives);

			if (shields > 0)
			{
				Drawing.drawing.setColor(255, 0, 0, 255 * opacity, 255);
				Drawing.drawing.fillOval(Game.playerTank.posX + healthSize / 2 - thickness / 4, Game.playerTank.posY, Game.tile_size / 2 + Game.playerTank.posZ, 18, 18, 0, false, false);
				Drawing.drawing.setFontSize(12);
				Drawing.drawing.setColor(255, 255, 255, 255 * opacity, 255);
				Drawing.drawing.drawText(Game.playerTank.posX + healthSize / 2 - thickness / 4, Game.playerTank.posY, Game.tile_size / 2 + Game.playerTank.posZ,  shields + "", false);
			}
		}

		if (this.enabledAmmunitionBar)
		{
			int live = 1;
			int max = 1;

			double cooldownFrac = 0;
			double bcooldown = 50;

			double cooldownFrac2 = 0;
			double mcooldown = 50;

			if (Game.playerTank instanceof TankPlayerController)
			{
				TankPlayerController p = ((TankPlayerController) Game.playerTank);
				live = p.liveBullets;
				max = p.maxLiveBullets;
				cooldownFrac = p.bulletCooldown / p.bulletCooldownBase;

				if (p.bulletCooldownBase > bcooldown)
					bcooldown = p.bulletCooldownBase;
			}
			else
			{
				ItemBullet.ItemStackBullet ib = null;
				if (Game.playerTank != null && !Game.playerTank.destroy && Game.playerTank.getPrimaryAbility() instanceof ItemBullet.ItemStackBullet)
					ib = (ItemBullet.ItemStackBullet) Game.playerTank.getPrimaryAbility();

				if (this.enabledItemBar && this.itemBar.selected != -1 && this.itemBar.selected < hotbarSlots && this.itemBar.slots[this.itemBar.selected] instanceof ItemBullet.ItemStackBullet)
					ib = (ItemBullet.ItemStackBullet) this.itemBar.slots[this.itemBar.selected];

				if (ib != null)
				{
					live = ib.liveBullets;
					max = ib.item.bullet.maxLiveBullets;

					if (ib.stackSize > 0 && ib.stackSize < max)
						max = Math.min(max, ib.stackSize + live);

					if (ib.destroy)
					{
						max = 1;
						live = 1;
					}

					cooldownFrac = ib.cooldown / ib.item.cooldownBase;

					if (ib.item.cooldownBase > bcooldown)
						bcooldown = ib.item.cooldownBase;
				}
			}

			if (lastCooldownFrac < cooldownFrac && Game.playerTank != null && Game.playerTank.health > 0 && !ignoreInitialStats)
				timeSinceBulletChange = 0;

			lastCooldownFrac = cooldownFrac;

			if (lastLiveBullets != live && Game.playerTank != null && !Game.playerTank.destroy)
			{
				lastLiveBullets = live;

				if (!ignoreInitialStats)
					timeSinceBulletChange = 0;
			}
			double ammo = live * 1.0 / max;
			double ammo2 = (live - cooldownFrac) / max;

			if (max <= 0)
			{
				max = 1;
				ammo = 0;
				ammo2 = -cooldownFrac;
			}

			if (Game.playerTank != null && Game.playerTank.destroy)
			{
				ammo = 1;
				ammo2 = 1;
				max = 1;
			}

			double opacity = circleVisibility / circleVisibilityMax * Math.min(1, this.circlePersistenceVisibility + Math.max(0, 2 - timeSinceBulletChange / bcooldown));
			Drawing.drawing.setColor(0, 0, 0, opacity * 128);

			if (Level.isDark())
				Drawing.drawing.setColor(255, 255, 255, 128 * opacity);

			Drawing.drawing.fillInterfacePartialRing(mx, my, size, thickness, 1);

			Drawing.drawing.setColor(0, 255, 255, opacity * 255);
			Drawing.drawing.fillInterfacePartialRing(mx, my, size, thickness, Math.min(1, Math.max(0, 1 - ammo2)));

			Drawing.drawing.setColor(0, 200, 255, opacity * 255);
			Drawing.drawing.fillInterfacePartialRing(mx, my, size, thickness, Math.min(1, Math.max(0, 1 - ammo)));

			Drawing.drawing.setColor(0, 255, 255, opacity * 255);
			Drawing.drawing.fillInterfacePartialRing(mx, my, size, thickness, Math.min(1, Math.max(0, -ammo2 * max)));

			Drawing.drawing.setColor(0, 0, 0, 128 * opacity);

			if (max <= 100)
			{
				for (int i = 1; i <= max; i++)
				{
					double frac = i * 1.0 / max;
					Drawing.drawing.fillInterfacePartialRing(mx, my, size, thickness, frac, 0.005);
				}
			}

			if (Game.playerTank != null && !Game.playerTank.destroy)
			{
				int mines = 0;
				int remainingItems = 0;

				if (Game.playerTank instanceof TankPlayerController)
				{
					TankPlayerController p = ((TankPlayerController) Game.playerTank);
					mines = p.maxLiveMines - p.liveMines;

					cooldownFrac2 = p.mineCooldown / p.mineCooldownBase;
					if (p.mineCooldownBase > mcooldown)
						mcooldown = p.mineCooldownBase;
				}
				else
				{
					ItemMine.ItemStackMine m = null;

					if (Game.playerTank.getSecondaryAbility() instanceof ItemMine.ItemStackMine)
						m = (ItemMine.ItemStackMine) Game.playerTank.getSecondaryAbility();

					if (this.enabledItemBar && this.itemBar.selected != -1 && this.itemBar.selected < hotbarSlots && this.itemBar.slots[this.itemBar.selected] instanceof ItemMine.ItemStackMine)
						m = (ItemMine.ItemStackMine) this.itemBar.slots[this.itemBar.selected];

					if (m != null)
					{
						cooldownFrac2 = m.cooldown / m.item.cooldownBase;

						mines = m.item.mine.maxLiveMines - m.liveMines;

						if (m.stackSize > 0)
							mines = Math.min(m.stackSize, mines);

						if (m.destroy)
							mines = 0;

						remainingItems = m.stackSize;
					}
				}

				if (lastMines != mines)
				{
					lastMines = mines;

					if (!ignoreInitialStats)
						timeSinceMineChange = 0;
				}

				if (lastCooldownFrac2 < cooldownFrac2 && Game.playerTank != null && !Game.playerTank.destroy && !ignoreInitialStats)
					timeSinceMineChange = 0;

				lastCooldownFrac = cooldownFrac;

				double opacity1 = circleVisibility / circleVisibilityMax * Math.min(1,  this.circlePersistenceVisibility + Math.max(0, 2 - timeSinceMineChange / mcooldown));

				double m = 2;
				Drawing.drawing.setColor(255, 0, 0, opacity1 * 255, 255);
				if (mines == 0)
					Drawing.drawing.setColor(80, 80, 80, opacity1 * 255, 255);

				Drawing.drawing.fillOval(Game.playerTank.posX, Game.playerTank.posY, Game.tile_size / 2 + Game.playerTank.posZ, 18 * m, 18 * m, false, false);

				Drawing.drawing.setColor(255, 180, 0, opacity1 * 255, 255);
				if (mines == 0)
					Drawing.drawing.setColor(140, 140, 140, opacity1 * 255, 255);

				Drawing.drawing.fillPartialRing(Game.playerTank.posX, Game.playerTank.posY, Game.tile_size / 2 + Game.playerTank.posZ, 18 * m, 8, -cooldownFrac2, cooldownFrac2);

				Drawing.drawing.setColor(255, 255, 0, opacity1 * 255, 255);
				if (mines == 0)
					Drawing.drawing.setColor(160, 160, 160, opacity1 * 255, 255);

				Drawing.drawing.fillOval(Game.playerTank.posX, Game.playerTank.posY, Game.tile_size / 2 + Game.playerTank.posZ, 14 * m, 14 * m, false, false);

				Drawing.drawing.setFontSize(12 * m);
				Drawing.drawing.setColor(0, 0, 0, opacity1 * 255, 255);

				Drawing.drawing.drawText(Game.playerTank.posX, Game.playerTank.posY, Game.tile_size / 2 + Game.playerTank.posZ, mines + "", false);

				Drawing.drawing.setFontSize(6 * m);
				if (remainingItems > 0)
					Drawing.drawing.drawText(Game.playerTank.posX + 15, Game.playerTank.posY + 15, Game.tile_size / 2 + Game.playerTank.posZ, remainingItems + "", false);
			}
		}

		this.ignoreInitialStats = false;

		int stackCount = this.itemBar.getSelectedAction(false).stackSize;
		if (stackCount > 0 && Game.screen instanceof ScreenGame && ((ScreenGame) Game.screen).playing && !((ScreenGame) Game.screen).paused && !Game.playerTank.destroy)
		{
			if (Level.isDark())
				Drawing.drawing.setColor(255, 255, 255, 255 * (1 - itemBar.lastCircularOpacity));
			else
				Drawing.drawing.setColor(0, 0, 0, 255 * (1 - itemBar.lastCircularOpacity));

			Drawing.drawing.setInterfaceFontSize(10);
			Drawing.drawing.drawInterfaceText(mx + 17, my + 17, stackCount + "", true);
		}
	}

	public void resetTimers()
	{
		this.timeSinceBulletChange = Double.MAX_VALUE;
		this.timeSinceMineChange = Double.MAX_VALUE;
		this.timeSinceHealthChange = Double.MAX_VALUE;
		this.itemBar.timeSinceSwitch = Double.MAX_VALUE;
		this.ignoreInitialStats = true;
	}
}
