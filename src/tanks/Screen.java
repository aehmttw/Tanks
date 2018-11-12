package tanks;

import java.awt.Color;
import java.awt.Graphics;

public abstract class Screen
{
	public abstract void update() throws Throwable;
	
	public abstract void draw(Graphics g) throws Throwable;
	
	public void drawDefaultBackground(Graphics g)
	{
		if (!(Game.screen instanceof ScreenInterlevel))
			Panel.darkness = Math.max(Panel.darkness - Panel.frameFrequency * 3, 0);

		g.setColor(Level.currentColor);
		Drawing.window.fillRect(g, Drawing.sizeX / 2, Drawing.sizeY / 2, Drawing.sizeX, Drawing.sizeY);

		if (Game.graphicalEffects)
		{
			for (int i = 0; i < Game.currentSizeX; i++)
			{
				for (int j = 0; j < Game.currentSizeY; j++)
				{					
					g.setColor(Game.tiles[i][j]);
					Drawing.window.fillRect(g, 
							(i + 0.5) / Game.bgResMultiplier * Obstacle.obstacle_size, 
							(j + 0.5) / Game.bgResMultiplier * Obstacle.obstacle_size, 
							Obstacle.obstacle_size / Game.bgResMultiplier, 
							Obstacle.obstacle_size / Game.bgResMultiplier);
				}
			}
			
			g.setColor(new Color(0, 0, 0, Math.max(0, (int) Panel.darkness)));
			Drawing.window.fillBackgroundRect(g, Drawing.sizeX / 2, Drawing.sizeY / 2, Drawing.sizeX, Drawing.sizeY);
		}
	}
}
