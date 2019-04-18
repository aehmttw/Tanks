package tanks;

import org.lwjgl.glfw.GLFW;

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
			int y = (int) (Drawing.drawing.interfaceSizeY - 15 + bottomOffset);
			Drawing.drawing.setColor(0, 0, 0, 128);
			Drawing.drawing.fillInterfaceRect(x, y, 350, 5);
			Drawing.drawing.setColor(255, 128, 0);

			double lives = Game.player.lives % 1.0;
			if (lives == 0 && Game.player.lives > 0)
				lives = 1;

			if (Game.player.destroy && Game.player.lives < 1)
				lives = 0;

			int shields = (int) (Game.player.lives - lives);
		
			Drawing.drawing.fillInterfaceProgressRect(x, y, 350, 5, lives);
			
			if (shields > 0)
			{
				Drawing.drawing.setColor(255, 0 , 0);
				Drawing.drawing.fillInterfaceOval(x - 175, y, 18, 18);
				Drawing.drawing.setFontSize(12);
				Drawing.drawing.setColor(255, 255, 255);
				Drawing.drawing.drawInterfaceText(x - 175, y, shields + "");
			}
		}
		
		if (this.enabledCoins)
		{
			Drawing.drawing.setFontSize(18);
			Drawing.drawing.setColor(0, 0, 0);
			Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - 85 + bottomOffset, "Coins: " + currentCoins.coins);
		}
	}
}
