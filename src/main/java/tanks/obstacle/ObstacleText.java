package tanks.obstacle;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;

public class ObstacleText extends Obstacle
{
	public String text = "Text";
	public double fontSize = 16;
	public double age = 0;
	public double duration = -1;

	public ObstacleText(String name, double posX, double posY)
	{
		super(name, posX, posY);

		this.update = true;
		this.replaceTiles = false;
		this.drawLevel = 1;
		this.destructible = false;
		this.bulletCollision = false;
		this.tankCollision = false;
		this.colorR = 0;
		this.colorG = 0;
		this.colorB = 0;
		this.colorA = 0;
		this.batchDraw = false;
		this.type = ObstacleType.extra;

		this.description = "A piece of text used to instruct the player";

		if (Game.framework == Game.Framework.libgdx)
			this.fontSize = 24;
	}

	@Override
	public void update()
	{
		this.age += Panel.frameFrequency;

		if (this.duration >= 0 && this.age > this.duration)
			Game.removeObstacles.add(this);
	}
	
	@Override
	public void draw()
	{
		Drawing.drawing.setFontSize(this.fontSize);
		Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB);
		Drawing.drawing.drawText(this.posX, this.posY, this.text);
	}
	
	@Override
	public void drawForInterface(double x, double y)
	{	
		Drawing.drawing.setFontSize(this.fontSize);
		Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB);
		Drawing.drawing.drawInterfaceText(this.posX, this.posY, this.text);
	}

	@Override
	public void draw3dOutline(double r, double g, double b, double a)
	{

	}

	public double getTileHeight()
	{
		return 0;
	}
}
