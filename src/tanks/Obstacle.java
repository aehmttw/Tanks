package tanks;

import java.awt.Color;
import java.awt.Graphics;

public class Obstacle
{
	int posX;
	int posY;
	Color color;
	public static int draw_size = 0;
	public static int obstacle_size = Game.tank_size; 
	
	public Obstacle(int posX, int posY, Color color)
	{
		this.posX = (int) ((posX + 0.5) * obstacle_size);
		this.posY = (int) ((posY + 0.5) * obstacle_size);
		this.color = color;
	}
	
	public void draw(Graphics g)
	{	
		g.setColor(color);
		g.fillRect(this.posX-draw_size/2, this.posY-draw_size/2, draw_size, draw_size);
	}
}
