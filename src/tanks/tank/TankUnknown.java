package tanks.tank;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import tanks.Game;
import tanks.Drawing;

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
		Drawing drawing = Drawing.window;
		drawing.fillRect(g, this.posX - this.size * 0.4, this.posY, this.size * 0.2, this.size);
		drawing.fillRect(g, this.posX + this.size * 0.4, this.posY, this.size * 0.2, this.size);
		drawing.fillRect(g, this.posX, this.posY - this.size * 0.4, this.size, this.size * 0.2);
		drawing.fillRect(g, this.posX, this.posY + this.size * 0.4, this.size, this.size * 0.2);
	
		g.setFont(g.getFont().deriveFont(Font.BOLD, (float) (24 * drawing.scale)));
		drawing.drawText(g, this.posX, this.posY + 5, "?");
		
		g.setFont(g.getFont().deriveFont(Font.BOLD, (float) (12 * drawing.scale)));
		drawing.drawText(g, this.posX, this.posY + 32, this.name);

	}

	@Override
	public void shoot()
	{
		
	}

}
