package tanks.obstacle;

import tanks.Drawing;
import tanks.Game;

public class ObstacleText extends Obstacle
{
	public String text;
	public double fontSize = 16;
	
	public ObstacleText(String name, String text, double posX, double posY) 
	{
		super(name, posX, posY);

		this.replaceTiles = false;
		this.drawLevel = 1;
		this.destructible = false;
		this.bulletCollision = false;
		this.tankCollision = false;
		this.colorR = 0;
		this.colorG = 0;
		this.colorB = 0;
		this.colorA = 0;
		this.text = text;
		this.enableStacking = false;

		this.description = "A piece of text used---to instruct the player";

		if (Game.framework == Game.Framework.libgdx)
			this.fontSize = 24;
	}
	
	@Override
	public void draw()
	{
		Drawing.drawing.setFontSize(this.fontSize);
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.drawText(this.posX, this.posY, this.text);
	}
	
	@Override
	public void drawForInterface(double x, double y)
	{	
		Drawing.drawing.setFontSize(this.fontSize);
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.drawInterfaceText(this.posX, this.posY, this.text);
	}

}
