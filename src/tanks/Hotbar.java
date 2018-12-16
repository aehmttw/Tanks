package tanks;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

public class Hotbar
{
	public ItemBar currentItemBar = new ItemBar(this);
	public Coins currentCoins;

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
		
		if (Game.player.destroy)
			this.hidden = true;
		
		this.hideTimer = Math.max(0, this.hideTimer - Panel.frameFrequency);
			
		if (this.hideTimer <= 0 && !this.persistent)
			this.hidden = true;
		
		if (this.hidden)
			this.bottomOffset = Math.min(100, this.bottomOffset + Panel.frameFrequency);
		else
			this.bottomOffset = Math.max(0, this.bottomOffset - 4 * Panel.frameFrequency);
		
		if (InputKeyboard.validKeys.contains(KeyEvent.VK_SHIFT))
		{
			InputKeyboard.validKeys.remove((Integer)KeyEvent.VK_SHIFT);
			this.persistent = !this.persistent;
		}	
			
		if (this.enabledItemBar)
			this.currentItemBar.update();
	}

	public void draw(Graphics g)
	{
		if (this.enabledItemBar)
			this.currentItemBar.draw(g);

		if (this.enabledHealthBar)
		{
			int x = (int) ((Drawing.interfaceSizeX / 2));
			int y = (int) (Drawing.interfaceSizeY - 15 + bottomOffset);
			g.setColor(new Color(0, 0, 0, 128));
			Drawing.window.fillInterfaceRect(g, x, y, 350, 5);
			g.setColor(new Color(255, 128, 0));

			double lives = Game.player.lives % 1.0;
			if (lives == 0 && Game.player.lives > 0)
				lives = 1;

			if (Game.player.destroy && Game.player.lives < 1)
				lives = 0;

			int shields = (int) (Game.player.lives - lives);
		
			Drawing.window.fillInterfaceProgressRect(g, x, y, 350, 5, lives);
			
			if (shields > 0)
			{
				g.setColor(new Color(255, 0 , 0));
				Drawing.window.fillInterfaceOval(g, x - 175, y, 18, 18);
				Drawing.setFontSize(g, 15);
				g.setColor(Color.white);
				Drawing.window.drawInterfaceText(g, x - 174, y + 3, shields + "");
			}
		}
		
		if (this.enabledCoins)
		{
			Drawing.setFontSize(g, 18);
			g.setColor(Color.black);
			Drawing.window.drawInterfaceText(g, Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY - 85 + bottomOffset, "Coins: " + currentCoins.coins);
		}
	}
}
