package tanks.obstacle;

public class ObstacleIndestructible extends ObstacleStackable
{

	public ObstacleIndestructible(String name, double posX, double posY) 
	{
		super(name, posX, posY);
		
		this.destructible = false;
		this.description = "A solid indestructible block";
	}

	@Override
	public double[] getRandomColor()
	{
		double[] col = super.getRandomColor();
		double c = (col[0] + col[1] + col[2]) / 3 + 50;
		col[0] = c;
		col[1] = c;
		col[2] = c;

		return col;
	}
}
