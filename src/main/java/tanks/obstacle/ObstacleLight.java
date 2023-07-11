package tanks.obstacle;

import tanks.Drawing;
import tanks.Game;
import tanks.IDrawableLightSource;

public class ObstacleLight extends Obstacle implements IDrawableLightSource
{
	public double[] lightInfo;

	public ObstacleLight(String name, double posX, double posY)
	{
		super(name, posX, posY);

		this.lightInfo = new double[]{0, 0, 0, 0, 255, 250, 235};

		this.draggable = false;
		this.destructible = false;
		this.bulletCollision = false;
		this.tankCollision = false;
		this.colorR = 255;
		this.colorG = 250;
		this.colorB = 235;
		this.glow = 1.0;
		this.batchDraw = false;
		this.replaceTiles = false;

		this.drawLevel = 9;

		for (int i = 0; i < default_max_height; i++)
		{
			this.stackColorR[i] = 255;
			this.stackColorG[i] = 250;
			this.stackColorB[i] = 235;
		}

		this.description = "A light to illuminate dark levels";
	}

	@Override
	public void draw()
	{
		Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA, this.glow);

		if (Game.enable3d)
			Drawing.drawing.fillBox(this, this.posX, this.posY, 0, Obstacle.draw_size / 2, Obstacle.draw_size / 2, Obstacle.draw_size / 2);
		else
			Drawing.drawing.fillRect(this, this.posX, this.posY, Obstacle.draw_size / 2, Obstacle.draw_size / 2);

		double frac = Obstacle.draw_size / Game.tile_size;
		Drawing.drawing.setColor(this.colorR * frac, this.colorG * frac, this.colorB * frac, this.colorA, this.glow);

		//double s = this.stackHeight * Game.tile_size * 4;
		//Drawing.drawing.fillForcedGlow(this.posX, this.posY, 0, s * 3, s * 3, false, false, false, true);

	}

	@Override
	public void drawGlow()
	{
		double s = this.stackHeight * Game.tile_size * 4;
		double frac = Obstacle.draw_size / Game.tile_size * 0.75;
		Drawing.drawing.setColor(this.colorR * frac, this.colorG * frac, this.colorB * frac, this.colorA, this.glow);
		Drawing.drawing.fillForcedGlow(this.posX, this.posY, 0, s, s, false, false, false, false);
	}

	@Override
	public boolean isGlowEnabled()
	{
		return true;
	}

	@Override
	public void drawForInterface(double x, double y)
	{
		Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA);
		Drawing.drawing.fillInterfaceRect(x, y, draw_size / 2, draw_size / 2);
	}

	public double getTileHeight()
	{
		return 0;
	}

	@Override
	public boolean lit()
	{
		return false;
	}

	@Override
	public double[] getLightInfo()
	{
		this.lightInfo[3] = Math.pow(this.stackHeight, 3) / 4 * Obstacle.draw_size / Game.tile_size;
		return this.lightInfo;
	}
}