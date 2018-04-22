package tanks;

import java.awt.Color;

public class Level 
{
	String[] resolution;
	String[] obstaclesPos;
	String[] tanks;

	public Level(String level)
	{
		String[] preset = level.split("\\{")[1].split("\\}")[0].split("\\|");
		resolution = preset[0].split(",");
		obstaclesPos = preset[1].split(",");
		tanks = preset[2].split(",");
	}
	
	public void loadLevel()
	{
		int sX = Integer.parseInt(resolution[0]);
		int sY = Integer.parseInt(resolution[1]);
		Game.gamescreen.setScreenSize(Game.tank_size * sX, Game.tank_size * sY);
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
					Game.obstacles.add(new Obstacle(x, y, Color.BLACK));
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
			
			if (type.equals("brown"))
				Game.movables.add(new EnemyTankBrown(x, y, Game.tank_size, angle));
			else if (type.equals("mint"))
				Game.movables.add(new EnemyTankMint(x, y, Game.tank_size, angle));
			else if (type.equals("yellow"))
				Game.movables.add(new EnemyTankYellow(x, y, Game.tank_size, angle));
			else if (type.equals("green"))
				Game.movables.add(new EnemyTankGreen(x, y, Game.tank_size, angle));
			else if (type.equals("player"))
			{
				Game.player = new PlayerTank(x, y, Game.tank_size, new Color(0, 150, 255));
				Game.movables.add(Game.player);
			}
		}
		
		Panel.preGameTimer = 300;
	}
}
