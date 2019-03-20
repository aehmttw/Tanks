package tanks;

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
		Drawing.drawing.fillRect(Drawing.drawing.sizeX / 2, Drawing.drawing.sizeY / 2, Drawing.drawing.sizeX, Drawing.drawing.sizeY);

		if (Game.fancyGraphics)
		{
			for (int i = 0; i < Game.currentSizeX; i++)
			{
				for (int j = 0; j < Game.currentSizeY; j++)
				{				
					Drawing.drawing.setColor(Game.tilesR[i][j], Game.tilesG[i][j], Game.tilesB[i][j]);
					Drawing.drawing.fillRect( 
							(i + 0.5) / Game.bgResMultiplier * Obstacle.obstacle_size, 
							(j + 0.5) / Game.bgResMultiplier * Obstacle.obstacle_size, 
							Obstacle.obstacle_size * size / Game.bgResMultiplier, 
							Obstacle.obstacle_size * size / Game.bgResMultiplier);
				}
			}
			
		    Drawing.drawing.setColor(0, 0, 0, Math.max(0, (int) Panel.darkness));
			Drawing.drawing.fillBackgroundRect(Drawing.drawing.sizeX / 2, Drawing.drawing.sizeY / 2, Drawing.drawing.sizeX, Drawing.drawing.sizeY);
		}
	}
}
