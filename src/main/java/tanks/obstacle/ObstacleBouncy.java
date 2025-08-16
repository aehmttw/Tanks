package tanks.obstacle;

import tanks.Game;

public class ObstacleBouncy extends ObstacleStackable
{
	public ObstacleBouncy(String name, double posX, double posY) 
	{
		super(name, posX, posY);
		
		this.bouncy = true;

		for (int i = 1; i < default_max_height; i++)
		{
			stackColorR[i] = Game.fancyTerrain ? Math.random() * 127 + 128 : 191;
			stackColorG[i] = 0;
			stackColorB[i] = 255;
		}

		this.colorR = this.stackColorR[0];
        this.colorG = this.stackColorG[0];
        this.colorB = this.stackColorB[0];

		this.description = "A destructible block which allows bullets to bounce more";
	}

	@Override
	public double[] getRandomColor()
	{
		double[] col = new double[3];

		if (Game.fancyTerrain)
		{
			col[0] = Math.random() * 127 + 128;
			col[1] = 0;
			col[2] = 255;
		}
		else
			col = new double[]{196, 0, 255};

		return col;
	}
}
