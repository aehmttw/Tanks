package tanks;

import java.awt.Color;

public class Level 
{
	String[] preset;
	String[] screen;
	String[] obstaclesPos;
	String[] tanks;

	static Color currentColor = new Color(235, 207, 166);

	public Level(String level)
	{
		preset = level.split("\\{")[1].split("\\}")[0].split("\\|");

		screen = preset[0].split(",");
		obstaclesPos = preset[1].split(",");
		tanks = preset[2].split(",");
	}

	public void loadLevel()
	{
		int sX = Integer.parseInt(screen[0]);
		int sY = Integer.parseInt(screen[1]);

		int r = 235;
		int g = 207;
		int b = 166;

		int dr = 20;
		int dg = 20;
		int db = 20;

		if (screen.length >= 5)
		{
			r = Integer.parseInt(screen[2]);
			g = Integer.parseInt(screen[3]);
			b = Integer.parseInt(screen[4]);

			if (screen.length >= 8)
			{
				dr = Integer.parseInt(screen[5]);
				dg = Integer.parseInt(screen[6]);
				db = Integer.parseInt(screen[7]);
			}
		}

		Game.currentSizeX = (int) (sX * Game.bgResMultiplier);
		Game.currentSizeY = (int) (sY * Game.bgResMultiplier);

		currentColor = new Color(r, g, b);

		Game.tiles = new Color[Game.currentSizeX][Game.currentSizeY];
		for (int i = 0; i < Game.currentSizeX; i++)
		{
			for (int j = 0; j < Game.currentSizeY; j++)
			{
				Game.tiles[i][j] = new Color((int)(r + Math.random() * dr), (int)(g + Math.random() * dg), (int)(b + Math.random() * db));
			}
		}

		Game.window.setScreenBounds(Game.tank_size * sX, Game.tank_size * sY);
				
		if (!((obstaclesPos.length == 1 && obstaclesPos[0].equals("")) || obstaclesPos.length == 0)) 
		{
			for (int i = 0; i < obstaclesPos.length; i++)
			{
				String[] obs = obstaclesPos[i].split("-");

				String[] xPos = obs[0].split("\\.\\.\\.");

				double startX; 
				double endX;

				startX = Double.parseDouble(xPos[0]);
				endX = startX;

				if (xPos.length > 1)
					endX = Double.parseDouble(xPos[1]);

				String[] yPos = obs[1].split("\\.\\.\\.");

				double startY; 
				double endY;

				startY = Double.parseDouble(yPos[0]);
				endY = startY;

				if (yPos.length > 1)
					endY = Double.parseDouble(yPos[1]);

				for (double x = startX; x <= endX; x++)
				{
					for (double y = startY; y <= endY; y++)
					{
						Color col = Obstacle.getRandomColor();

						Game.obstacles.add(new Obstacle(x, y, col));
					}
				}
			}
		}
		
		for (int i = 0; i < tanks.length; i++)
		{
			String[] tank = tanks[i].split("-");
			double x = Game.tank_size * (0.5 + Double.parseDouble(tank[0]));
			double y = Game.tank_size * (0.5 + Double.parseDouble(tank[1]));
			String type = tank[2].toLowerCase();
			double angle = 0;
			if (tank.length == 4)
				angle = (Math.PI / 2 * Double.parseDouble(tank[3]));

			if (type.equals("player"))
			{
				Game.player = new PlayerTank(x, y, angle);
				Game.movables.add(Game.player);
			}
			else
			{
				Game.movables.add(Game.registry.getEntry(type).getTank(x, y, angle));
			}
		}
	}
}
