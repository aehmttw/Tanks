package tanks.obstacle;

import tanks.Game;

public class ObstacleBouncy extends Obstacle
{
	public ObstacleBouncy(String name, double posX, double posY) 
	{
		super(name, posX, posY);
		
		this.bouncy = true;
		this.colorR = Math.random() * 127 + 128;
		this.colorG = 0;
		this.colorB = 255;

		for (int i = 1; i < default_max_height; i++)
		{
			stackColorR[i] = Math.random() * 127 + 128;
			stackColorG[i] = 0;
			stackColorB[i] = 255;

			if (!Game.fancyTerrain)
				stackColorR[i] = 191;
		}

		stackColorR[0] = colorR;
		stackColorG[0] = colorG;
		stackColorB[0] = colorB;
		
		if (!Game.fancyTerrain)
			this.colorR = 191;

		this.description = "A destructible block which allows bullets to bounce more";
	}
}
