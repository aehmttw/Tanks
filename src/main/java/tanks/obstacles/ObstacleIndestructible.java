package tanks.obstacles;

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
}
