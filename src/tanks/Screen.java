package tanks;

import org.lwjgl.opengl.GL11;

public abstract class Screen
{
	public String screenHint = "";

	public abstract void update();

	public abstract void draw();

	public void drawDefaultBackground()
	{
		this.drawDefaultBackground(1);
	}

	public void drawDefaultBackground(double size)
	{
		if (!(Game.screen instanceof ScreenInterlevel))
			Panel.darkness = Math.max(Panel.darkness - Panel.frameFrequency * 3, 0);

		Drawing.drawing.setColor(Level.currentColorR, Level.currentColorG, Level.currentColorB, 255.0 * size);
		Drawing.drawing.fillBackgroundRect(Drawing.drawing.sizeX / 2, Drawing.drawing.sizeY / 2, Drawing.drawing.sizeX, Drawing.drawing.sizeY);

		if (Game.fancyGraphics)
		{
			for (int i = 0; i < Game.currentSizeX; i++)
			{
				for (int j = 0; j < Game.currentSizeY; j++)
				{				
					Drawing.drawing.setColor(Game.tilesR[i][j], Game.tilesG[i][j], Game.tilesB[i][j]);

					if (Game.enable3d)
					{
						if (Game.tileDrawables[i][j] != null)
						{
							Game.tileDrawables[i][j].drawTile(Game.tilesR[i][j], Game.tilesG[i][j], Game.tilesB[i][j], Game.tilesDepth[i][j]);
							Game.tileDrawables[i][j] = null;
						}
						else
						{
							if (size != 1)
								Drawing.drawing.fillBox( 
										(i + 0.5) / Game.bgResMultiplier * Obstacle.obstacle_size, 
										(j + 0.5) / Game.bgResMultiplier * Obstacle.obstacle_size, Math.max(0, 2000 - size * 2000 * (1 + Game.tilesDepth[i][j] / 10)) - Obstacle.obstacle_size + Game.tilesDepth[i][j], 
										Obstacle.obstacle_size / Game.bgResMultiplier, 
										Obstacle.obstacle_size / Game.bgResMultiplier,
										Obstacle.obstacle_size);
							else
								Drawing.drawing.fillBox( 
										(i + 0.5) / Game.bgResMultiplier * Obstacle.obstacle_size, 
										(j + 0.5) / Game.bgResMultiplier * Obstacle.obstacle_size, 0, 
										Obstacle.obstacle_size / Game.bgResMultiplier, 
										Obstacle.obstacle_size / Game.bgResMultiplier,
										Game.tilesDepth[i][j]);
						}
					}
					else
						Drawing.drawing.fillRect( 
								(i + 0.5) / Game.bgResMultiplier * Obstacle.obstacle_size, 
								(j + 0.5) / Game.bgResMultiplier * Obstacle.obstacle_size, 
								Obstacle.obstacle_size * size / Game.bgResMultiplier, 
								Obstacle.obstacle_size * size / Game.bgResMultiplier);
				}
			}

			Drawing.drawing.setColor(0, 0, 0, Math.max(0, (int) Panel.darkness));
			Drawing.drawing.fillBackgroundRect(Drawing.drawing.sizeX / 2, Drawing.drawing.sizeY / 2, Drawing.drawing.sizeX * 2, Drawing.drawing.sizeY * 2);
		}
		else if (Game.enable3d)
		{
			for (int i = 0; i < Game.currentSizeX; i++)
			{
				for (int j = 0; j < Game.currentSizeY; j++)
				{				
					Drawing.drawing.setColor(Level.currentColorR, Level.currentColorG, Level.currentColorB);

					if (Game.tileDrawables[i][j] != null)
					{
						Game.tileDrawables[i][j].drawTile(Level.currentColorR, Level.currentColorG, Level.currentColorB, 0);
						Game.tileDrawables[i][j] = null;
					}
					else
					{
						GL11.glEnable(GL11.GL_DEPTH_TEST);
						Drawing.drawing.fillRect( 
								(i + 0.5) / Game.bgResMultiplier * Obstacle.obstacle_size, 
								(j + 0.5) / Game.bgResMultiplier * Obstacle.obstacle_size, 
								Obstacle.obstacle_size * size / Game.bgResMultiplier, 
								Obstacle.obstacle_size * size / Game.bgResMultiplier);
						GL11.glDisable(GL11.GL_DEPTH_TEST);
					}
				}
			}
		}
	}
}
