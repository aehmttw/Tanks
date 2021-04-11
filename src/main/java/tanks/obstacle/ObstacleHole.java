package tanks.obstacle;

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

		this.enableStacking = false;

		this.colorR = 0;
		this.colorG = 0;
		this.colorB = 0;
		this.colorA = 127;

		this.description = "A hole which only---bullets can pass over";
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
		if (Game.fancyTerrain)
		{
			double mul = 0.4 + 0.6 * (1 - draw_size / Game.tile_size);
			Drawing.drawing.setColor(r * mul, g * mul, b * mul);

			Drawing.drawing.fillBox(this.posX, this.posY, -draw_size / 2 + d, Game.tile_size, Game.tile_size, 0, (byte) 61);

			Drawing.drawing.setColor(r, g, b);

			Drawing.drawing.fillBox(this.posX, this.posY, -draw_size / 2 + d, Game.tile_size, Game.tile_size, draw_size / 2, (byte) 3);
			
			Drawing.drawing.fillBox(this.posX - 3 * Game.tile_size / 8, this.posY, -Game.tile_size / 2 + d, Game.tile_size / 4, Game.tile_size, Game.tile_size / 2, (byte) 17);
			Drawing.drawing.fillBox(this.posX + 3 * Game.tile_size / 8, this.posY, -Game.tile_size / 2 + d, Game.tile_size / 4, Game.tile_size, Game.tile_size / 2, (byte) 33);

			Drawing.drawing.fillBox(this.posX, this.posY - 3 * Game.tile_size / 8, -Game.tile_size / 2 + d, Game.tile_size, Game.tile_size / 4, Game.tile_size / 2, (byte) 9);
			Drawing.drawing.fillBox(this.posX, this.posY + 3 * Game.tile_size / 8, -Game.tile_size / 2 + d, Game.tile_size, Game.tile_size / 4, Game.tile_size / 2, (byte) 5);


			Drawing.drawing.fillBox(this.posX - 3 * Game.tile_size / 8, this.posY, -Game.tile_size / 2 + d + 0.2, Game.tile_size / 4, Game.tile_size, Game.tile_size / 2, (byte) 61);
			Drawing.drawing.fillBox(this.posX + 3 * Game.tile_size / 8, this.posY, -Game.tile_size / 2 + d + 0.2, Game.tile_size / 4, Game.tile_size, Game.tile_size / 2, (byte) 61);

			Drawing.drawing.fillBox(this.posX, this.posY - 3 * Game.tile_size / 8, -Game.tile_size / 2 + d + 0.2, Game.tile_size, Game.tile_size / 4, Game.tile_size / 2, (byte) 61);
			Drawing.drawing.fillBox(this.posX, this.posY + 3 * Game.tile_size / 8, -Game.tile_size / 2 + d + 0.2, Game.tile_size, Game.tile_size / 4, Game.tile_size / 2, (byte) 61);
		}
		else
		{
			Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA);
			Drawing.drawing.fillRect(this.posX, this.posY, draw_size / 2, draw_size / 2);
		}
	}

	public double getTileHeight()
	{
		return -draw_size / 2;
	}
}
