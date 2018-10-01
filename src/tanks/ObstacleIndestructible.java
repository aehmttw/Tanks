package tanks;

import java.awt.Color;
import java.awt.Graphics;

public class ObstacleIndestructible extends Obstacle
{

	public ObstacleIndestructible(String name, double posX, double posY) 
	{
		super(name, posX, posY);
		
		this.destructible = false;
		int col = (this.color.getRed() + this.color.getGreen() + this.color.getBlue()) / 3;
		this.color = new Color(col, col, col);
	}
	
	public void draw(Graphics g)
	{	
		g.setColor(this.color);
		Drawing.fillRect(g, this.posX, this.posY, draw_size, draw_size);
	}

}
