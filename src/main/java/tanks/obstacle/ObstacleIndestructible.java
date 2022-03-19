package tanks.obstacle;

public class ObstacleIndestructible extends Obstacle
{

	public ObstacleIndestructible(String name, double posX, double posY) 
	{
		super(name, posX, posY);
		
		this.destructible = false;
		double col = (this.colorR + this.colorG + this.colorB) / 3 + 50;
		this.colorR = col;
		this.colorG = col;
		this.colorB = col;

		for (int i = 0; i < default_max_height; i++)
		{
			double c = (this.stackColorR[i] + this.stackColorG[i] + this.stackColorB[i]) / 3 + 50;
			this.stackColorR[i] = c;
			this.stackColorG[i] = c;
			this.stackColorB[i] = c;
		}

		this.description = "A solid indestructible block";
	}
}
