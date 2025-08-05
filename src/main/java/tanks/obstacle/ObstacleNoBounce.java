package tanks.obstacle;

public class ObstacleNoBounce extends ObstacleStackable
{
	public ObstacleNoBounce(String name, double posX, double posY)
	{
		super(name, posX, posY);

        this.type = ObstacleType.full;
		this.destructible = false;
		this.allowBounce = false;
		this.description = "An indestructible block which prevents bullets from bouncing";
	}

	@Override
	public double[] getRandomColor()
	{
		double[] col = super.getRandomColor();
		double c = ((col[0] + col[1] + col[2]) / 3 + 50) / 2;
		col[0] = c;
		col[1] = c;
		col[2] = c;

		return col;
	}
}
