package tanks.tank;

import tanks.Game;
import tanks.Drawing;

/**
 * This is the tank that appears whenever an invalid ID is specified. It is useful for debugging purposes.
 * */
public class TankUnknown extends Tank
{
	public TankUnknown(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tank_size, 255, 0, 0);
	}
	
	@Override
	public void draw()
	{
		Drawing drawing = Drawing.drawing;
		drawing.setColor(255, 0, 0);
		drawing.fillRect(this.posX - this.size * 0.4, this.posY, this.size * 0.2, this.size);
		drawing.fillRect(this.posX + this.size * 0.4, this.posY, this.size * 0.2, this.size);
		drawing.fillRect(this.posX, this.posY - this.size * 0.4, this.size, this.size * 0.2);
		drawing.fillRect(this.posX, this.posY + this.size * 0.4, this.size, this.size * 0.2);
	
		drawing.setFontSize(24);
		drawing.drawText(this.posX, this.posY, "?");
		
		drawing.setFontSize(12);
		drawing.drawText(this.posX, this.posY - 40, this.name);

	}

	@Override
	public void shoot()
	{
		
	}

}
