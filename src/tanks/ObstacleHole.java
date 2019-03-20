package tanks;

public class ObstacleHole extends Obstacle
{
	public ObstacleHole(String name, double posX, double posY) 
	{
		super(name, posX, posY);
		
		this.drawBelow = true;
		this.destructible = false;
		this.bulletCollision = false;
		this.colorR = 0;
		this.colorG = 0;
		this.colorB = 0;
		this.colorA = 128;
	}
	
	@Override
	public void draw()
	{	
		Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA);
		Drawing.drawing.fillRect(this.posX, this.posY, draw_size / 2, draw_size / 2);
	}
	
	@Override
	public void drawForInterface(double x, double y)
	{	
		Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA);
		Drawing.drawing.fillInterfaceRect(x, y, draw_size / 2, draw_size / 2);
	}

}
