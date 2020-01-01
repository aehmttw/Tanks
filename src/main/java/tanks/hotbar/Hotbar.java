package tanks.hotbar;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import org.lwjgl.glfw.GLFW;
import tanks.gui.screen.ScreenGame;

public class Hotbar
{
	public ItemBar currentItemBar;
	public Coins currentCoins;

	public boolean enabledAmmunitionBar = true;
	public boolean enabledItemBar = false;
	public boolean enabledHealthBar = true;
	public boolean enabledCoins = false;

	public boolean hidden = true;
	public boolean persistent = false;

	public double bottomOffset = 100;

	public double hideTimer = 0;

	public void update()
	{
		if (this.persistent)
			this.hidden = false;
		
		if (Game.playerTank.destroy)
			this.hidden = true;
		
		this.hideTimer = Math.max(0, this.hideTimer - Panel.frameFrequency);
			
		if (this.hideTimer <= 0 && !this.persistent)
			this.hidden = true;
		
		if (this.hidden)
			this.bottomOffset = Math.min(100, this.bottomOffset + Panel.frameFrequency);
		else
			this.bottomOffset = Math.max(0, this.bottomOffset - 4 * Panel.frameFrequency);
		
		if (Game.game.window.validPressedKeys.contains(GLFW.GLFW_KEY_RIGHT_SHIFT) || Game.game.window.validPressedKeys.contains(GLFW.GLFW_KEY_LEFT_SHIFT))
		{
			Game.game.window.validPressedKeys.remove((Integer)GLFW.GLFW_KEY_RIGHT_SHIFT);
			Game.game.window.validPressedKeys.remove((Integer)GLFW.GLFW_KEY_LEFT_SHIFT);
			this.persistent = !this.persistent;
		}	
			
		if (this.enabledItemBar)
			this.currentItemBar.update();
	}

	public void draw()
	{
		if (this.enabledItemBar)
			this.currentItemBar.draw();

		if (this.enabledHealthBar)
		{
			int x = (int) ((Drawing.drawing.interfaceSizeX / 2));
			int y = (int) (Drawing.drawing.interfaceSizeY - 25 + bottomOffset);
			Drawing.drawing.setColor(0, 0, 0, 128 * (100 - this.bottomOffset) / 100.0);
			Drawing.drawing.fillInterfaceRect(x, y, 350, 5);
			Drawing.drawing.setColor(255, 128, 0, (100 - this.bottomOffset) * 2.55);

			double lives = Game.playerTank.lives % 1.0;
			if (lives == 0 && Game.playerTank.lives > 0)
				lives = 1;

			if (Game.playerTank.destroy && Game.playerTank.lives < 1)
				lives = 0;

			int shields = (int) (Game.playerTank.lives - lives);
		
			Drawing.drawing.fillInterfaceProgressRect(x, y, 350, 5, lives);
			
			if (shields > 0)
			{
				Drawing.drawing.setColor(255, 0 , 0, (100 - this.bottomOffset) * 2.55);
				Drawing.drawing.fillInterfaceOval(x - 175, y, 18, 18);
				Drawing.drawing.setInterfaceFontSize(12);
				Drawing.drawing.setColor(255, 255, 255, (100 - this.bottomOffset) * 2.55);
				Drawing.drawing.drawInterfaceText(x - 174, y, shields + "");
			}
		}

		if (this.enabledAmmunitionBar)
		{
			int x = (int) ((Drawing.drawing.interfaceSizeX / 2));
			int y = (int) (Drawing.drawing.interfaceSizeY - 10 + bottomOffset);

			Drawing.drawing.setColor(0, 0, 0, 128 * (100 - this.bottomOffset) / 100.0);
			Drawing.drawing.fillInterfaceRect(x, y, 350, 5);
			Drawing.drawing.setColor(0, 200, 255, (100 - this.bottomOffset) * 2.55);

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

			Drawing.drawing.setColor(0, 0, 0, 127 * (100 - this.bottomOffset) / 100.0);

			for (int i = 1; i < max; i++)
			{
				double frac = i * 1.0 / max;
				Drawing.drawing.fillInterfaceRect(x - 175 + frac * 350, y, 2, 5);
			}

			if (Game.playerTank.liveMines < Game.playerTank.liveMinesMax)
			{
				Drawing.drawing.setColor(255, 0 , 0, (100 - this.bottomOffset) * 2.55);
				Drawing.drawing.fillInterfaceOval(x + 175, y, 18, 18);

				Drawing.drawing.setColor(255, 255 , 0, (100 - this.bottomOffset) * 2.55);
				Drawing.drawing.fillInterfaceOval(x + 175, y, 14, 14);

				Drawing.drawing.setInterfaceFontSize(12);
				Drawing.drawing.setColor(0, 0, 0, (100 - this.bottomOffset) * 2.55);
				Drawing.drawing.drawInterfaceText(x + 176, y, Game.playerTank.liveMinesMax - Game.playerTank.liveMines + "");
			}
		}
		
		if (this.enabledCoins)
		{
			Drawing.drawing.setInterfaceFontSize(18);
			Drawing.drawing.setColor(0, 0, 0, (100 - this.bottomOffset) * 2.55);
			Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - 100 + bottomOffset, "Coins: " + currentCoins.coins);
		}
	}
}
