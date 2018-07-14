package tanks;

import java.awt.Color;
import java.awt.Graphics;

public class Obstacle
{
	double posX;
	double posY;
	Color color;
	public static double draw_size = 0;
	public static int obstacle_size = Game.tank_size; 
	
	public Obstacle(double posX, double posY, Color color)
	{
		this.posX = (int) ((posX + 0.5) * obstacle_size);
		this.posY = (int) ((posY + 0.5) * obstacle_size);
		this.color = color;
	}
	
	public void draw(Graphics g)
	{	
		g.setColor(color);

		//if (Screen.scale * 10 == Math.round(Screen.scale * 10))
			Window.fillRect(g, this.posX, this.posY, draw_size, draw_size);
		//else
		//	Screen.fillRect(g, this.posX - 1, this.posY - 1, draw_size + 2, draw_size + 2);
	}
}
