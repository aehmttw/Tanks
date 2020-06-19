package tanks.hotbar;

import basewindow.InputCodes;
import tanks.*;
import tanks.gui.Button;
import tanks.tank.Tank;
import tanks.tank.Turret;

public class Hotbar
{
	public ItemBar currentItemBar;
	public Coins currentCoins;

	public boolean enabledAmmunitionBar = true;
	public boolean enabledItemBar = false;
	public boolean enabledHealthBar = true;
	public boolean enabledCoins = false;
	public boolean enabledRemainingEnemies = true;

	public boolean hidden = true;
	public boolean persistent = false;

	public double percentHidden = 100;
	public double verticalOffset = 0;

	public double hideTimer = 0;

	public Button toggle = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - 20, 150, 40, "", new Runnable()
	{
		@Override
		public void run()
		{
			Panel.panel.hotbar.persistent = !Panel.panel.hotbar.persistent;
		}
	}
	);

	public void update()
	{
		if (Game.game.window.touchscreen)
		{
			this.verticalOffset = 20;
			this.toggle.update();
		}
		else
			this.verticalOffset = 0;

		if (this.persistent)
			this.hidden = false;

		if (Game.playerTank.destroy)
			this.hidden = true;

		this.hideTimer = Math.max(0, this.hideTimer - Panel.frameFrequency);

		if (this.hideTimer <= 0 && !this.persistent)
			this.hidden = true;

		if (this.hidden)
			this.percentHidden = Math.min(100, this.percentHidden + Panel.frameFrequency);
		else
			this.percentHidden = Math.max(0, this.percentHidden - 4 * Panel.frameFrequency);

		if (Game.game.window.validPressedKeys.contains(InputCodes.KEY_RIGHT_SHIFT) || Game.game.window.validPressedKeys.contains(InputCodes.KEY_LEFT_SHIFT))
		{
			Game.game.window.validPressedKeys.remove((Integer) InputCodes.KEY_RIGHT_SHIFT);
			Game.game.window.validPressedKeys.remove((Integer) InputCodes.KEY_LEFT_SHIFT);
			this.persistent = !this.persistent;
		}

		if (this.enabledItemBar)
			this.currentItemBar.update();
	}

	public void draw()
	{
		if (Game.game.window.touchscreen)
		{
			Drawing.drawing.setColor(255, 255, 255, 64);

			if (!this.persistent)
				Drawing.drawing.drawInterfaceImage("/widearrow.png", Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - 12, 64, 16);
			else
				Drawing.drawing.drawInterfaceImage("/widearrow.png", Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - 12, 64, -16);
		}

		if (this.enabledItemBar)
			this.currentItemBar.draw();

		if (this.enabledHealthBar)
		{
			int x = (int) ((Drawing.drawing.interfaceSizeX / 2));
			int y = (int) (Drawing.drawing.interfaceSizeY - 25 + percentHidden - verticalOffset);
			Drawing.drawing.setColor(0, 0, 0, 128 * (100 - this.percentHidden) / 100.0);
			Drawing.drawing.fillInterfaceRect(x, y, 350, 5);
			Drawing.drawing.setColor(255, 128, 0, (100 - this.percentHidden) * 2.55);

			double lives = Game.playerTank.health % 1.0;
			if (lives == 0 && Game.playerTank.health > 0)
				lives = 1;

			if (Game.playerTank.destroy && Game.playerTank.health < 1)
				lives = 0;

			int shields = (int) (Game.playerTank.health - lives);

			Drawing.drawing.fillInterfaceProgressRect(x, y, 350, 5, lives);

			if (shields > 0)
			{
				Drawing.drawing.setColor(255, 0 , 0, (100 - this.percentHidden) * 2.55);
				Drawing.drawing.fillInterfaceOval(x - 175, y, 18, 18);
				Drawing.drawing.setInterfaceFontSize(12);
				Drawing.drawing.setColor(255, 255, 255, (100 - this.percentHidden) * 2.55);
				Drawing.drawing.drawInterfaceText(x - 174, y, shields + "");
			}
		}

		if (this.enabledAmmunitionBar)
		{
			int x = (int) ((Drawing.drawing.interfaceSizeX / 2));
			int y = (int) (Drawing.drawing.interfaceSizeY - 10 + percentHidden - verticalOffset);

			Drawing.drawing.setColor(0, 0, 0, 128 * (100 - this.percentHidden) / 100.0);
			Drawing.drawing.fillInterfaceRect(x, y, 350, 5);
			Drawing.drawing.setColor(0, 200, 255, (100 - this.percentHidden) * 2.55);

			int live = Game.playerTank.liveBullets;
			int max = Game.playerTank.liveBulletMax;

			if (this.enabledItemBar && this.currentItemBar.selected != -1 && this.currentItemBar.slots[this.currentItemBar.selected] instanceof ItemBullet)
			{
				ItemBullet ib = (ItemBullet) this.currentItemBar.slots[this.currentItemBar.selected];
				live = ib.liveBullets;
				max = ib.maxAmount;
			}

			double ammo = live * 1.0 / max;

			if (max <= 0)
				ammo = 0;

			Drawing.drawing.fillInterfaceProgressRect(x, y, 350, 5, 1 - ammo);

			Drawing.drawing.setColor(0, 0, 0, 127 * (100 - this.percentHidden) / 100.0);

			for (int i = 1; i < max; i++)
			{
				double frac = i * 1.0 / max;
				Drawing.drawing.fillInterfaceRect(x - 175 + frac * 350, y, 2, 5);
			}

			if (Game.playerTank.liveMines < Game.playerTank.liveMinesMax)
			{
				Drawing.drawing.setColor(255, 0 , 0, (100 - this.percentHidden) * 2.55);
				Drawing.drawing.fillInterfaceOval(x + 175, y, 18, 18);

				Drawing.drawing.setColor(255, 255 , 0, (100 - this.percentHidden) * 2.55);
				Drawing.drawing.fillInterfaceOval(x + 175, y, 14, 14);

				Drawing.drawing.setInterfaceFontSize(12);
				Drawing.drawing.setColor(0, 0, 0, (100 - this.percentHidden) * 2.55);
				Drawing.drawing.drawInterfaceText(x + 176, y, Game.playerTank.liveMinesMax - Game.playerTank.liveMines + "");
			}
		}

		if (this.enabledCoins)
		{
			Drawing.drawing.setInterfaceFontSize(18);
			Drawing.drawing.setColor(0, 0, 0, (100 - this.percentHidden) * 2.55);
			Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - 100 + percentHidden - verticalOffset, "Coins: " + currentCoins.coins);
		}

		if (this.enabledRemainingEnemies)
		{
			int count = 0;

			for (Movable m: Game.movables)
			{
				if (m instanceof Tank && !Team.isAllied(Game.playerTank, m) && !m.destroy)
					count++;
			}

			int x = (int) ((Drawing.drawing.interfaceSizeX / 2) - 210);
			int y = (int) (Drawing.drawing.interfaceSizeY - 17.5 + percentHidden - verticalOffset);

			Drawing.drawing.setColor(191, 63, 63, (100 - this.percentHidden) * 2.55);
			Drawing.drawing.drawInterfaceModel(Tank.color_model, x, y, Game.tile_size / 2, Game.tile_size / 2, 0);

			Drawing.drawing.setColor(255, 0, 0, (100 - this.percentHidden) * 2.55);
			Drawing.drawing.drawInterfaceModel(Tank.base_model, x, y, Game.tile_size / 2, Game.tile_size / 2, 0);

			Drawing.drawing.setColor(191, 63, 63, (100 - this.percentHidden) * 2.55);

			if (Game.framework != Game.Framework.swing)
			{
				Drawing.drawing.drawInterfaceModel(Turret.turret_model, x, y, Game.tile_size / 2, Game.tile_size / 2, 0);

				Drawing.drawing.setColor(223, 31, 31, (100 - this.percentHidden) * 2.55);
				Drawing.drawing.drawInterfaceModel(Turret.base_model, x, y, Game.tile_size / 2, Game.tile_size / 2, 0);
			}
			else
				Drawing.drawing.fillInterfaceRect(x + Game.tile_size / 4, y, Game.tile_size / 2 + 4, 4);

			Drawing.drawing.setColor(255, 0, 0, (100 - this.percentHidden) * 2.55);
			Drawing.drawing.setInterfaceFontSize(24);
			Drawing.drawing.drawInterfaceText(x - 17, y + 1, "" + count, true);
		}
	}
}
