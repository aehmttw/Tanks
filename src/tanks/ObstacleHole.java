package tanks;

import java.awt.Color;
import java.awt.Graphics;

public class ObstacleHole extends Obstacle
{

	public ObstacleHole(String name, double posX, double posY) 
	{
		super(name, posX, posY);
		
		this.drawBelow = true;
		this.destructible = false;
		this.bulletCollision = false;
		this.color = new Color(0, 0, 0, 128);
	}
	
	public void draw(Graphics g)
	{	
		g.setColor(this.color);
		Drawing.fillRect(g, this.posX, this.posY, draw_size / 2, draw_size / 2);
	}

}
