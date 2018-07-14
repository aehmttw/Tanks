package tanks;

import java.awt.Color;
import java.awt.Graphics;

public abstract class Screen
{
	public abstract void update();
	
	public abstract void draw(Graphics g);
	
	public void drawDefaultBackground(Graphics g)
	{
		if (!(Game.screen instanceof ScreenInterlevel))
			Panel.darkness = Math.max(Panel.darkness - Panel.frameFrequency * 3, 0);

		g.setColor(Level.currentColor);
		Window.fillRect(g, Window.sizeX / 2, Window.sizeY / 2, Window.sizeX, Window.sizeY);

		if (Game.graphicalEffects)
		{
			for (int i = 0; i < Game.currentSizeX; i++)
			{
				for (int j = 0; j < Game.currentSizeY; j++)
				{
					int extra;
					if (Window.scale * 10 == Math.round(Window.scale * 10))
						extra = 0;
					else
						extra = 0;

					g.setColor(Game.tiles[i][j]);
					Window.fillRect(g, (i + 0.5) / Game.bgResMultiplier * Obstacle.obstacle_size, (j + 0.5) / Game.bgResMultiplier * Obstacle.obstacle_size, extra + Obstacle.obstacle_size / Game.bgResMultiplier, extra + Obstacle.obstacle_size / Game.bgResMultiplier);
				}
			}
			
			g.setColor(new Color(0, 0, 0, (int) Panel.darkness));
			Window.fillRect(g, Window.sizeX / 2, Window.sizeY / 2, Window.sizeX, Window.sizeY);
		}
	}
}
