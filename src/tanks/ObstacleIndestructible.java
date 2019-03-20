package tanks;

public class ObstacleIndestructible extends Obstacle
{

	public ObstacleIndestructible(String name, double posX, double posY) 
	{
		super(name, posX, posY);
		
		this.destructible = false;
		double col = (this.colorR + this.colorG + this.colorB) / 3;
		this.colorR = col;
		this.colorG = col;
		this.colorB = col;
	}
	
	@Override
	public void draw()
	{	
		Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA);
		Drawing.drawing.fillRect(this.posX, this.posY, draw_size, draw_size);
	}

}
