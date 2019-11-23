package tanks.obstacle;

import tanks.Drawing;

public class ObstacleText extends Obstacle
{
	String text;
	
	public ObstacleText(String name, String text, double posX, double posY) 
	{
		super(name, posX, posY);
		
		this.drawLevel = 1;
		this.destructible = false;
		this.bulletCollision = false;
		this.tankCollision = false;
		this.colorR = 0;
		this.colorG = 0;
		this.colorB = 0;
		this.colorA = 0;
		this.text = text;

		this.description = "A piece of text used---to instruct the player";
	}
	
	@Override
	public void draw()
	{	
		Drawing.drawing.setFontSize(16);
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.drawText(this.posX, this.posY, this.text);
	}
	
	@Override
	public void drawForInterface(double x, double y)
	{	
		Drawing.drawing.setFontSize(16);
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.drawInterfaceText(this.posX, this.posY, this.text);
	}

}
