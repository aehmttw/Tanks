package tanks.obstacles;

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
	}
	
	@Override
	public void draw()
	{
		Drawing.drawing.setFontSize(12);
		Drawing.drawing.drawText(this.posX, this.posY + 32, this.name);
		
		super.draw();
	}

}
 