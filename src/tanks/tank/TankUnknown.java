package tanks.tank;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import tanks.Game;
import tanks.Window;

/**
 * This is the tank that appears whenever an invalid ID is specified. It is useful for debugging purposes.
 * */
public class TankUnknown extends Tank
{
	public TankUnknown(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tank_size, Color.red);
	}
	
	@Override
	public void draw(Graphics g)
	{
		g.setColor(Color.red);
		Window.fillRect(g, this.posX - this.size * 0.4, this.posY, this.size * 0.2, this.size);
		Window.fillRect(g, this.posX + this.size * 0.4, this.posY, this.size * 0.2, this.size);
		Window.fillRect(g, this.posX, this.posY - this.size * 0.4, this.size, this.size * 0.2);
		Window.fillRect(g, this.posX, this.posY + this.size * 0.4, this.size, this.size * 0.2);
	
		g.setFont(g.getFont().deriveFont(Font.BOLD, (float) (24 * Window.scale)));
		Window.drawText(g, this.posX, this.posY + 5, "?");
		
		g.setFont(g.getFont().deriveFont(Font.BOLD, (float) (12 * Window.scale)));
		Window.drawText(g, this.posX, this.posY + 32, this.name);

	}

	@Override
	public void shoot()
	{
		
	}

}
