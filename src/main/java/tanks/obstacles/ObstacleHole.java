package tanks.obstacles;

import tanks.Drawing;
import tanks.Game;

public class ObstacleHole extends Obstacle
{
	public ObstacleHole(String name, double posX, double posY) 
	{
		super(name, posX, posY);

		this.drawLevel = 1;
		this.destructible = false;
		this.bulletCollision = false;
		this.replaceTiles = true;

		this.colorR = 0;
		this.colorG = 0;
		this.colorB = 0;
		this.colorA = 127;
	}

	@Override
	public void draw()
	{	
		if (!Game.enable3d)
		{
			Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA);
			Drawing.drawing.fillRect(this.posX, this.posY, draw_size / 2, draw_size / 2);
		}
	}

	@Override
	public void drawForInterface(double x, double y)
	{	
		Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA);
		Drawing.drawing.fillInterfaceRect(x, y, draw_size / 2, draw_size / 2);
	}

	@Override
	public void drawTile(double r, double g, double b, double d) 
	{
		if (Game.fancyGraphics)
		{
			double mul = 0.4 + 0.6 * (1 - draw_size / obstacle_size);
			Drawing.drawing.setColor(r * mul, g * mul, b * mul);

			Drawing.drawing.fillBox(this.posX, this.posY, -draw_size / 2 + d, obstacle_size, obstacle_size, 0, (byte) 61);

			Drawing.drawing.setColor(r, g, b);

			Drawing.drawing.fillBox(this.posX, this.posY, -draw_size / 2 + d, obstacle_size, obstacle_size, draw_size / 2, (byte) 3);

			
			Drawing.drawing.fillBox(this.posX - 3 * obstacle_size / 8, this.posY, -obstacle_size / 2 + d, obstacle_size / 4, obstacle_size, obstacle_size / 2, (byte) 17);
			Drawing.drawing.fillBox(this.posX + 3 * obstacle_size / 8, this.posY, -obstacle_size / 2 + d, obstacle_size / 4, obstacle_size, obstacle_size / 2, (byte) 33);

			Drawing.drawing.fillBox(this.posX, this.posY - 3 * obstacle_size / 8, -obstacle_size / 2 + d, obstacle_size, obstacle_size / 4, obstacle_size / 2, (byte) 9);
			Drawing.drawing.fillBox(this.posX, this.posY + 3 * obstacle_size / 8, -obstacle_size / 2 + d, obstacle_size, obstacle_size / 4, obstacle_size / 2, (byte) 5);


			Drawing.drawing.fillBox(this.posX - 3 * obstacle_size / 8, this.posY, -obstacle_size / 2 + d + 0.2, obstacle_size / 4, obstacle_size, obstacle_size / 2, (byte) 61);
			Drawing.drawing.fillBox(this.posX + 3 * obstacle_size / 8, this.posY, -obstacle_size / 2 + d + 0.2, obstacle_size / 4, obstacle_size, obstacle_size / 2, (byte) 61);

			Drawing.drawing.fillBox(this.posX, this.posY - 3 * obstacle_size / 8, -obstacle_size / 2 + d + 0.2, obstacle_size, obstacle_size / 4, obstacle_size / 2, (byte) 61);
			Drawing.drawing.fillBox(this.posX, this.posY + 3 * obstacle_size / 8, -obstacle_size / 2 + d + 0.2, obstacle_size, obstacle_size / 4, obstacle_size / 2, (byte) 61);
		}
		else
		{
			Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA);
			Drawing.drawing.fillRect(this.posX, this.posY, draw_size / 2, draw_size / 2);
		}
	}
}
