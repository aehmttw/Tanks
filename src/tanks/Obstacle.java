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

		Window.fillRect(g, this.posX, this.posY, draw_size, draw_size);
	}

	public void drawOutline(Graphics g)
	{
		g.setColor(this.color);
		Window.fillRect(g, this.posX - Obstacle.obstacle_size * 0.4, this.posY, Obstacle.obstacle_size * 0.2, Obstacle.obstacle_size);
		Window.fillRect(g, this.posX + Obstacle.obstacle_size * 0.4, this.posY, Obstacle.obstacle_size * 0.2, Obstacle.obstacle_size);
		Window.fillRect(g, this.posX, this.posY - Obstacle.obstacle_size * 0.4, Obstacle.obstacle_size, Obstacle.obstacle_size * 0.2);
		Window.fillRect(g, this.posX, this.posY + Obstacle.obstacle_size * 0.4, Obstacle.obstacle_size, Obstacle.obstacle_size * 0.2);
	}
	
	public static Color getRandomColor()
	{
		double colorMul = Math.random() * 0.5 + 0.5;
		Color col;
		
		if (Game.graphicalEffects)
			col = new Color((int) (colorMul * (176 - Math.random() * 70)), (int) (colorMul * (111 - Math.random() * 34)), (int) (colorMul * 14));
		else
			col = new Color(87, 46, 8);
		
		return col;
	}
}
