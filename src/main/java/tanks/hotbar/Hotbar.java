package tanks.hotbar;

import tanks.*;
import tanks.gui.Button;
import tanks.gui.screen.ScreenGame;
import tanks.item.Item;
import tanks.item.ItemBullet;
import tanks.item.ItemMine;
import tanks.item.ItemShield;
import tanks.minigames.Minigame;
import tanks.obstacle.Obstacle;
import tanks.tank.Tank;
import tanks.tank.TankModels;
import tanks.tank.TankPlayerController;
import tanks.tank.Turret;
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

	public double timeSinceCoinsChange = 0;
	public double timeSinceEnemiesChange = 0;
	public double timeSinceAlliesChange = 0;

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

	public int lastCoins = 0;
	public int lastAllies = 0;
	public int lastEnemies = 0;

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
			if (s.shopScreen || s.buildsScreen)
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
		timeSinceAlliesChange += Panel.frameFrequency;
		timeSinceEnemiesChange += Panel.frameFrequency;
		timeSinceCoinsChange += Panel.frameFrequency;

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

			TankModels.skinnedTankModel.base.setSkin(TankModels.tank.base);
			TankModels.skinnedTankModel.color.setSkin(TankModels.tank.color);
			TankModels.skinnedTankModel.turretBase.setSkin(TankModels.tank.turretBase);
			TankModels.skinnedTankModel.turret.setSkin(TankModels.tank.turret);

			Drawing.drawing.setColor(159, 32, 32, (100 - this.percentHidden) * 2.55);
			Drawing.drawing.drawInterfaceModel(TankModels.skinnedTankModel.base, x, y, Game.tile_size / 2, Game.tile_size / 2, 0);

			Drawing.drawing.setColor(255, 0, 0, (100 - this.percentHidden) * 2.55);
			Drawing.drawing.drawInterfaceModel(TankModels.skinnedTankModel.color, x, y, Game.tile_size / 2, Game.tile_size / 2, 0);

			Drawing.drawing.setColor(159, 32, 32, (100 - this.percentHidden) * 2.55);

			Drawing.drawing.drawInterfaceModel(TankModels.skinnedTankModel.turret, x, y, Game.tile_size / 2, Game.tile_size / 2, 0);

			Drawing.drawing.setColor(207, 16, 16, (100 - this.percentHidden) * 2.55);
			Drawing.drawing.drawInterfaceModel(TankModels.skinnedTankModel.turretBase, x, y, Game.tile_size / 2, Game.tile_size / 2, 0);

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

        if (Game.currentLevel instanceof Minigame)
            ((Minigame) Game.currentLevel).drawHotbar();
	}

	public void drawCircle()
	{
        this.itemBar.drawCircle();

        if (this.enabledAmmunitionBar)
        {
            drawBullets();
            drawMines();
            drawShields();
        }

        if (Game.playerTank == null || Game.game.window.drawingShadow)
			return;

        Game.game.window.transformations.add(((ScreenGame) Game.screen).slantTranslation);
        Game.game.window.transformations.add(((ScreenGame) Game.screen).slantRotation);
        Game.game.window.loadPerspective();

		double mx = Drawing.drawing.getInterfaceMouseX();
		double my = Drawing.drawing.getInterfaceMouseY();

        double z = Game.tile_size / 2 + Game.playerTank.posZ;
        if (!Game.enable3d)
            z = 0;

		double healthSize = 120;

		double thickness = 10;

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

			Drawing.drawing.fillPartialRing(Game.playerTank.posX, Game.playerTank.posY, z, healthSize, thickness, 0, 1);
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

			Drawing.drawing.fillPartialRing(Game.playerTank.posX, Game.playerTank.posY, z, healthSize, thickness, 0, lives);

			if (shields > 0)
			{
				Drawing.drawing.setColor(255, 0, 0, 255 * opacity, 255);
				Drawing.drawing.fillOval(Game.playerTank.posX + healthSize / 2 - thickness / 4, Game.playerTank.posY, z, 18, 18, 0, false, false);
				Drawing.drawing.setFontSize(12);
				Drawing.drawing.setColor(255, 255, 255, 255 * opacity, 255);
				Drawing.drawing.drawText(Game.playerTank.posX + healthSize / 2 - thickness / 4, Game.playerTank.posY, z,  shields + "", false);
			}
		}

        if (Game.currentLevel instanceof Minigame)
            ((Minigame) Game.currentLevel).drawCircleHotbar();

        this.ignoreInitialStats = false;

		Item.ItemStack<?> bullet = this.itemBar.getSelectedAction(false);
		int stackCount = 0;

		if (bullet != null)
			stackCount = bullet.stackSize;

		if (stackCount > 0 && Game.screen instanceof ScreenGame && ((ScreenGame) Game.screen).playing && !((ScreenGame) Game.screen).paused && !Game.playerTank.destroy)
		{
			if (Level.isDark())
				Drawing.drawing.setColor(255, 255, 255, 255 * (1 - itemBar.lastCircularOpacity));
			else
				Drawing.drawing.setColor(0, 0, 0, 255 * (1 - itemBar.lastCircularOpacity));

			Drawing.drawing.setInterfaceFontSize(10);
			Drawing.drawing.drawInterfaceText(mx + 17, my + 17, stackCount + "", true);
		}

		Game.game.window.transformations.remove(((ScreenGame) Game.screen).slantTranslation);
		Game.game.window.transformations.remove(((ScreenGame) Game.screen).slantRotation);
		Game.game.window.loadPerspective();

		int enemyCount = 0;
		int alliedCount = 0;
		for (Movable m : Game.movables)
		{
			if (m instanceof Tank && !m.destroy && ((Tank)m).mandatoryKill)
			{
				if (!Team.isAllied(Game.playerTank, m))
					enemyCount++;
				else
					alliedCount++;
			}
		}

		boolean playing = Game.screen instanceof ScreenGame && ((ScreenGame) Game.screen).playing;

		if (lastCoins != coins || !playing)
			timeSinceCoinsChange = 0;

		if ((lastAllies != alliedCount && !ScreenGame.finishedQuick) || (!playing && alliedCount > 1))
			timeSinceAlliesChange = 0;

		if (lastEnemies != enemyCount || !playing)
			timeSinceEnemiesChange = 0;

		lastCoins = coins;
		lastAllies = alliedCount;
		lastEnemies = enemyCount;

		double opacity1 = circleVisibility / circleVisibilityMax * Math.min(1,  this.circlePersistenceVisibility);
		double opacityCoins = circleVisibility / circleVisibilityMax * Math.min(1, this.circlePersistenceVisibility + Math.max(0, 20 - timeSinceCoinsChange / 50));
		double opacityAllies = circleVisibility / circleVisibilityMax * Math.min(1,((alliedCount > 1) ? this.circlePersistenceVisibility : 0) + Math.max(0, 20 - timeSinceAlliesChange / 50));
		double opacityEnemies = circleVisibility / circleVisibilityMax * Math.min(1, this.circlePersistenceVisibility + Math.max(0, 20 - timeSinceEnemiesChange / 50));

		Drawing.drawing.setInterfaceFontSize(24);

		double totalWidth = 0;

		double enemiesWidth = 0;
		double alliesWidth = 0;
		double coinsWidth = 0;

		double enemiesPos = 0;
		double alliesPos = 0;
		double coinsPos = 0;
		double livesPos = 0;

		double iconSize = 25;
		double iconSpace = 30;
		double space = 50;

		if (this.enabledRemainingEnemies)
		{
			enemiesWidth = Drawing.drawing.getInterfaceTextWidth("" + enemyCount);
			totalWidth += (space + enemiesWidth) * opacityEnemies;

			alliesPos = totalWidth;
			alliesWidth = Drawing.drawing.getInterfaceTextWidth("" + alliedCount);
			totalWidth += (space + alliesWidth) * opacityAllies;
		}

		if (this.enabledCoins)
		{
			coinsPos = totalWidth;
			coinsWidth = Drawing.drawing.getInterfaceTextWidth("" + coins);
			totalWidth += (space + coinsWidth) * opacityCoins;
		}

//		livesPos = totalWidth;
//		totalWidth += (30 + Drawing.drawing.getInterfaceTextWidth("" + Game.player.remainingLives)) * opacity1;

		if (this.enabledCoins)
		{
			double x = coinsPos - totalWidth / 2 + Drawing.drawing.interfaceSizeX / 2;
			double y = (Drawing.drawing.getInterfaceEdgeY(true) - 17.5 - verticalOffset);

			Drawing.drawing.setColor(255, 255, 255, opacityCoins * 255);
			Drawing.drawing.drawInterfaceImage("coin.png", x + (coinsWidth + iconSpace / 2) * opacityCoins, y, opacityCoins * iconSize, opacityCoins * iconSize);

			Drawing.drawing.setInterfaceFontSize(24 * opacityCoins);
			Drawing.drawing.setColor(255, 100, 0, opacityCoins * 255);
			Drawing.drawing.drawInterfaceText(x + 1, y + 1, coins + "", false);
			Drawing.drawing.setColor(255, 180, 0, opacityCoins * 255);
			Drawing.drawing.drawInterfaceText(x - 1, y - 1, coins + "", false);
		}

		if (this.enabledRemainingEnemies)
		{
			double x = enemiesPos - totalWidth / 2 + Drawing.drawing.interfaceSizeX / 2;
			double y = (Drawing.drawing.getInterfaceEdgeY(true) - 17.5 - verticalOffset);

			drawTankIcon(x + (enemiesWidth + iconSpace / 2 + 3) * opacityEnemies, y, 255, 0, 0, opacityEnemies, 22 * opacityEnemies);

			Drawing.drawing.setInterfaceFontSize(24 * opacityEnemies);
			Drawing.drawing.setColor(Turret.calculateSecondaryColor(255), Turret.calculateSecondaryColor(0), Turret.calculateSecondaryColor(0), opacityEnemies * 255);
			Drawing.drawing.drawInterfaceText(x + 1, y + 1, enemyCount + "", false);
			Drawing.drawing.setColor(255, 0, 0, opacityEnemies * 255);
			Drawing.drawing.drawInterfaceText(x - 1, y - 1, "" + enemyCount, false);
		}

		if (this.enabledRemainingEnemies)
		{
			double x = alliesPos - totalWidth / 2 + Drawing.drawing.interfaceSizeX / 2;
			double y = (Drawing.drawing.getInterfaceEdgeY(true) - 17.5 - verticalOffset);

			drawTankIcon(x + (alliesWidth + iconSpace / 2 + 3) * opacityAllies, y, 0, 150, 255, opacityAllies, 22 * opacityAllies);

			Drawing.drawing.setInterfaceFontSize(24 * opacityAllies);
			Drawing.drawing.setColor(Turret.calculateSecondaryColor(0), Turret.calculateSecondaryColor(150), Turret.calculateSecondaryColor(255), opacityAllies * 255);
			Drawing.drawing.drawInterfaceText(x + 1, y + 1, alliedCount + "", false);
			Drawing.drawing.setColor(0, 150, 255, opacityAllies * 255);
			Drawing.drawing.drawInterfaceText(x - 1, y - 1, "" + alliedCount, false);
		}

        if (Game.currentLevel instanceof Minigame)
            ((Minigame) Game.currentLevel).drawHotbar();
    }

	public void drawTankIcon(double x, double y, double r, double g, double b, double opacity1, double size)
	{
		double r2 = Turret.calculateSecondaryColor(r);
		double g2 = Turret.calculateSecondaryColor(g);
		double b2 = Turret.calculateSecondaryColor(b);

		double r3 = (r + r2) / 2;
		double g3 = (g + g2) / 2;
		double b3 = (b + b2) / 2;

		Drawing.drawing.setColor(r2, g2, b2, opacity1 * 255);
		Drawing.drawing.drawInterfaceModel(TankModels.plainTankModel.base, x, y, size, size, 0);

		Drawing.drawing.setColor(r, g, b, opacity1 * 255);
		Drawing.drawing.drawInterfaceModel(TankModels.plainTankModel.color, x, y, size, size, 0);

		Drawing.drawing.setColor(r2, g2, b2, opacity1 * 255);

		Drawing.drawing.drawInterfaceModel(TankModels.plainTankModel.turret, x, y, size, size, 0);

		Drawing.drawing.setColor(r3, g3, b3, opacity1 * 255);
		Drawing.drawing.drawInterfaceModel(TankModels.plainTankModel.turretBase, x, y, size, size, 0);
	}

	public void drawBullets()
	{
		double size = 60;
		double thickness = 10;
		int hotbarSlots = (this.itemBar.showItems ? ItemBar.item_bar_size : 0);

		double mx = Drawing.drawing.getInterfaceMouseX();
		double my = Drawing.drawing.getInterfaceMouseY();

		int live = 1;
		int max = 1;

		double cooldownFrac = 0;
		double bcooldown = 50;

		ItemBullet.ItemStackBullet ib = null;
		if (Game.playerTank != null && !Game.playerTank.destroy && Game.playerTank.getPrimaryAbility() instanceof ItemBullet.ItemStackBullet)
			ib = (ItemBullet.ItemStackBullet) Game.playerTank.getPrimaryAbility();

		if (this.enabledItemBar && this.itemBar.selected != -1 && this.itemBar.selected < hotbarSlots && this.itemBar.slots[this.itemBar.selected] instanceof ItemBullet.ItemStackBullet)
			ib = (ItemBullet.ItemStackBullet) this.itemBar.slots[this.itemBar.selected];

		if (Game.playerTank instanceof TankPlayerController && Game.screen instanceof ScreenGame && ((ScreenGame) Game.screen).playing)
		{
			TankPlayerController p = ((TankPlayerController) Game.playerTank);
			live = p.liveBullets;
			max = p.maxLiveBullets;
			cooldownFrac = p.bulletCooldown / p.bulletCooldownBase;

			if (ib != null && ib.stackSize > 0 && ib.stackSize < max)
				max = Math.min(max, ib.stackSize + live);

			if (p.bulletCooldownBase > bcooldown)
				bcooldown = p.bulletCooldownBase;
		}
		else
		{
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

		if (max <= 100 && ib != null)
		{
			for (int i = 1; i <= max; i++)
			{
				double frac = i * 1.0 / max;
				Drawing.drawing.fillInterfacePartialRing(mx, my, size, thickness, frac, 0.005);
			}
		}

		lastCooldownFrac = cooldownFrac;
	}

	public void drawMines()
	{
		int hotbarSlots = 0;
		if (Game.playerTank != null)
			hotbarSlots = (this.itemBar.showItems ? ItemBar.item_bar_size : 0);

		double cooldownFrac2 = 0;
		double mcooldown = 50;

		if (Game.playerTank != null && !Game.playerTank.destroy)
		{
			double z = Game.tile_size / 2 + Game.playerTank.posZ;
			if (!Game.enable3d)
				z = 0;

			int mines = 0;
			int remainingItems = 0;

			boolean clientPlaying = Game.playerTank instanceof TankPlayerController && Game.screen instanceof ScreenGame && ((ScreenGame) Game.screen).playing;

			if (clientPlaying)
			{
				TankPlayerController p = ((TankPlayerController) Game.playerTank);
				mines = p.maxLiveMines - p.liveMines;
				if (p.maxLiveMines == 0)
					mines = -1;

				cooldownFrac2 = p.mineCooldown / p.mineCooldownBase;
				if (p.mineCooldownBase > mcooldown)
					mcooldown = p.mineCooldownBase;
			}

			ItemMine.ItemStackMine m = null;

			if (Game.playerTank.getSecondaryAbility() instanceof ItemMine.ItemStackMine)
				m = (ItemMine.ItemStackMine) Game.playerTank.getSecondaryAbility();

			if (this.enabledItemBar && this.itemBar.selected != -1 && this.itemBar.selected < hotbarSlots && this.itemBar.slots[this.itemBar.selected] instanceof ItemMine.ItemStackMine)
				m = (ItemMine.ItemStackMine) this.itemBar.slots[this.itemBar.selected];

			if (m != null)
			{
				if (!clientPlaying)
				{
					cooldownFrac2 = m.cooldown / m.item.cooldownBase;

					mines = m.item.mine.maxLiveMines - m.liveMines;

					if (m.item.mine.maxLiveMines == 0)
						mines = -1;
				}

				if (m.stackSize > 0)
					mines = Math.min(m.stackSize, mines);

				if (m.destroy)
					mines = 0;

				remainingItems = m.stackSize;
			}
			else
				return;

			if (lastMines != mines)
			{
				lastMines = mines;

				if (!ignoreInitialStats)
					timeSinceMineChange = 0;
			}

			if (lastCooldownFrac2 < cooldownFrac2 && Game.playerTank != null && !Game.playerTank.destroy && !ignoreInitialStats)
				timeSinceMineChange = 0;

			lastCooldownFrac2 = cooldownFrac2;
			double opacity1 = circleVisibility / circleVisibilityMax * Math.min(1,  this.circlePersistenceVisibility + Math.max(0, 2 - timeSinceMineChange / mcooldown));

			double ms = 2;
			Drawing.drawing.setColor(255, 0, 0, opacity1 * 255, 255);
			if (mines == 0)
				Drawing.drawing.setColor(80, 80, 80, opacity1 * 255, 255);

			Drawing.drawing.fillOval(Game.playerTank.posX, Game.playerTank.posY, z, 18 * ms, 18 * ms, false, false);

			Drawing.drawing.setColor(255, 180, 0, opacity1 * 255, 255);
			if (mines == 0)
				Drawing.drawing.setColor(140, 140, 140, opacity1 * 255, 255);

			Drawing.drawing.fillPartialRing(Game.playerTank.posX, Game.playerTank.posY, z, 18 * ms, 8, -cooldownFrac2, cooldownFrac2);

			Drawing.drawing.setColor(255, 255, 0, opacity1 * 255, 255);
			if (mines == 0)
				Drawing.drawing.setColor(160, 160, 160, opacity1 * 255, 255);

			Drawing.drawing.fillOval(Game.playerTank.posX, Game.playerTank.posY, z, 14 * ms, 14 * ms, false, false);

			Drawing.drawing.setFontSize(12 * ms);
			Drawing.drawing.setColor(0, 0, 0, opacity1 * 255, 255);

			if (mines >= 0)
				Drawing.drawing.drawText(Game.playerTank.posX, Game.playerTank.posY, z, mines + "", false);

			Drawing.drawing.setFontSize(6 * ms);
			if (remainingItems > 0)
				Drawing.drawing.drawText(Game.playerTank.posX + 15, Game.playerTank.posY + 15, z, remainingItems + "", false);
		}
	}

	public void drawShields()
	{
		double cooldownFrac2 = 0;
		double mcooldown = 50;

		if (Game.playerTank != null && !Game.playerTank.destroy)
		{
			double z = Game.tile_size / 2 + Game.playerTank.posZ;
			if (!Game.enable3d)
				z = 0;

			int remainingItems = 0;

			boolean clientPlaying = Game.playerTank instanceof TankPlayerController && Game.screen instanceof ScreenGame && ((ScreenGame) Game.screen).playing;

			if (clientPlaying)
			{
				TankPlayerController p = ((TankPlayerController) Game.playerTank);

				cooldownFrac2 = p.mineCooldown / p.mineCooldownBase;
				if (p.mineCooldownBase > mcooldown)
					mcooldown = p.mineCooldownBase;
			}

			Item.ItemStack<?> st = Game.player.hotbar.itemBar.getSelectedAction(true);
			ItemShield.ItemStackShield s = null;
			if (st instanceof ItemShield.ItemStackShield)
				s = (ItemShield.ItemStackShield) st;

			if (s != null)
			{
				if (!clientPlaying)
					cooldownFrac2 = s.cooldown / s.item.cooldownBase;

				remainingItems = s.stackSize;
			}
			else
				return;

			if (lastCooldownFrac2 < cooldownFrac2 && !Game.playerTank.destroy && !ignoreInitialStats)
				timeSinceMineChange = 0;

			lastCooldownFrac2 = cooldownFrac2;

			double opacity1 = circleVisibility / circleVisibilityMax * Math.min(1,  this.circlePersistenceVisibility + Math.max(0, 2 - timeSinceMineChange / mcooldown));

			double ms = 2;

			double frac = 1 - cooldownFrac2;

			Drawing.drawing.setColor(140, 140, 140, opacity1 * 255, 255);
			Drawing.drawing.drawImage("shield_icon.png", Game.playerTank.posX, Game.playerTank.posY, z, 18 * ms, 18 * ms, false, 0, 0, 1, 1 - frac);

			Drawing.drawing.setColor(255, 255, 255, opacity1 * 255, 255);
			if (Game.playerTank.baseHealth + s.item.max <= Game.playerTank.health)
				Drawing.drawing.setColor(180, 180, 180, opacity1 * 255, 255);

			Drawing.drawing.drawImage("shield_icon.png", Game.playerTank.posX, Game.playerTank.posY - 18 * ms * (frac - 1), z, 18 * ms, 18 * ms, false, 0, 1 - frac, 1, 1);

			Drawing.drawing.setColor(0, 0, 0, opacity1 * 255, 255);
			Drawing.drawing.setFontSize(6 * ms);
			Drawing.drawing.drawText(Game.playerTank.posX, Game.playerTank.posY - 9, z, Game.playerTank.health + "", false);
			Drawing.drawing.fillRect(Game.playerTank.posX, Game.playerTank.posY - 2, z, 20, 2, false);
			Drawing.drawing.drawText(Game.playerTank.posX, Game.playerTank.posY + 5, z, "" + (Game.playerTank.baseHealth + s.item.max), false);

			Drawing.drawing.setFontSize(6 * ms);
			if (remainingItems > 0)
				Drawing.drawing.drawText(Game.playerTank.posX + 15, Game.playerTank.posY + 15, z, remainingItems + "", false);
		}
	}

	public void resetTimers()
	{
		this.timeSinceBulletChange = Double.MAX_VALUE;
		this.timeSinceMineChange = Double.MAX_VALUE;
		this.timeSinceHealthChange = Double.MAX_VALUE;
		this.timeSinceCoinsChange = Double.MAX_VALUE;
		this.timeSinceAlliesChange = Double.MAX_VALUE;
		this.timeSinceEnemiesChange = Double.MAX_VALUE;
		this.itemBar.timeSinceSwitch = Double.MAX_VALUE;
		lastAllies = 1;
		lastEnemies = 0;
		this.ignoreInitialStats = true;
	}
}
