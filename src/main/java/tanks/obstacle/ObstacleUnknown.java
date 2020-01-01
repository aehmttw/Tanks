package tanks.obstacle;

import tanks.Drawing;

public class ObstacleUnknown extends Obstacle
{

	public ObstacleUnknown(String name, double posX, double posY) 
	{
		super(name, posX, posY);
		
		this.colorR = 255;
		this.colorG = 0;
		this.colorB = 0;
		this.destructible = false;
		this.tankCollision = false;
		this.bulletCollision = false;

		this.description = "A block which could not be identified";
	}
	
	@Override
	public void draw()
	{
		Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB);
		Drawing.drawing.setFontSize(12);
		Drawing.drawing.drawText(this.posX, this.posY, this.name);
		Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, 64);
		Drawing.drawing.fillRect(this.posX, this.posY, draw_size, draw_size);
	}

}
 