package tanks.obstacle;

import basewindow.IBatchRenderableObject;
import tanks.Drawing;
import tanks.Game;

public class ObstacleHole extends Obstacle
{
	protected double size = 0.70;

	public ObstacleHole(String name, double posX, double posY) 
	{
		super(name, posX, posY);

		this.drawLevel = 1;
		this.destructible = false;
		this.bulletCollision = false;
		this.replaceTiles = Game.fancyTerrain;

		this.enableStacking = false;

		this.colorR = 0;
		this.colorG = 0;
		this.colorB = 0;
		this.colorA = 127;

		this.description = "A hole which only bullets can pass over";
	}

	@Override
	public void draw()
	{	
		if (!Game.enable3d || !Game.fancyTerrain)
		{
			Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA);
			Drawing.drawing.fillRect(this, this.posX, this.posY, draw_size * size, draw_size * size);
		}
	}

	@Override
	public void drawForInterface(double x, double y)
	{	
		Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA);
		Drawing.drawing.fillInterfaceRect(x, y, draw_size * size, draw_size * size);
	}

	@Override
	public void drawTile(IBatchRenderableObject o, double r, double g, double b, double d, double extra)
	{
		if (Game.fancyTerrain)
		{
			double s = this.size * Obstacle.draw_size / Game.tile_size;

			double mul = 0.4 + 0.6 * (1 - draw_size / Game.tile_size);
			Drawing.drawing.setColor(r * mul, g * mul, b * mul);

			Drawing.drawing.fillBox(o, this.posX, this.posY, -draw_size / 2 + d, Game.tile_size, Game.tile_size, 0, (byte) 61);

			Drawing.drawing.setColor(r, g, b);

			Drawing.drawing.fillBox(o, this.posX, this.posY, -draw_size / 2 + d, Game.tile_size, Game.tile_size, draw_size / 2, (byte) 3);
			
			Drawing.drawing.fillBox(o, this.posX - Game.tile_size * (0.5 - (1 - s) / 4), this.posY, -Game.tile_size / 2, Game.tile_size / 2 * (1 - s), Game.tile_size, Game.tile_size / 2 + d, (byte) 17);
			Drawing.drawing.fillBox(o, this.posX + Game.tile_size * (0.5 - (1 - s) / 4), this.posY, -Game.tile_size / 2, Game.tile_size / 2 * (1 - s), Game.tile_size, Game.tile_size / 2 + d, (byte) 33);

			Drawing.drawing.fillBox(o, this.posX, this.posY - Game.tile_size * (0.5 - (1 - s) / 4), -Game.tile_size / 2, Game.tile_size, Game.tile_size / 2 * (1 - s), Game.tile_size / 2 + d, (byte) 9);
			Drawing.drawing.fillBox(o, this.posX, this.posY + Game.tile_size * (0.5 - (1 - s) / 4), -Game.tile_size / 2, Game.tile_size, Game.tile_size / 2 * (1 - s), Game.tile_size / 2 + d, (byte) 5);

			Drawing.drawing.fillBox(o, this.posX - Game.tile_size * (0.5 - (1 - s) / 4), this.posY, -Game.tile_size / 2 + 0.2, Game.tile_size / 2 * (1 - s), Game.tile_size, Game.tile_size / 2 + d, (byte) 61);
			Drawing.drawing.fillBox(o, this.posX + Game.tile_size * (0.5 - (1 - s) / 4), this.posY, -Game.tile_size / 2 + 0.2, Game.tile_size / 2 * (1 - s), Game.tile_size, Game.tile_size / 2 + d, (byte) 61);

			Drawing.drawing.fillBox(o, this.posX, this.posY - Game.tile_size * (0.5 - (1 - s) / 4), -Game.tile_size / 2 + 0.2, Game.tile_size, Game.tile_size / 2 * (1 - s), Game.tile_size / 2 + d, (byte) 61);
			Drawing.drawing.fillBox(o, this.posX, this.posY + Game.tile_size * (0.5 - (1 - s) / 4), -Game.tile_size / 2 + 0.2, Game.tile_size, Game.tile_size / 2 * (1 - s), Game.tile_size / 2 + d, (byte) 61);
		}
		else
		{
			Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA);
			Drawing.drawing.fillRect(this, this.posX, this.posY, draw_size * size, draw_size * size);
		}
	}

	public double getTileHeight()
	{
		return -draw_size / 2;
	}
}
