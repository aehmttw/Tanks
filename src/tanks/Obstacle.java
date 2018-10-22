package tanks;

import java.awt.Color;
import java.awt.Graphics;

public class Obstacle
{
	public boolean destructible = true;
	public boolean tankCollision = true;
	public boolean bulletCollision = true;
	public boolean drawBelow = false;
	
	public double posX;
	public double posY;
	public Color color;
	public static double draw_size = 0;
	public static int obstacle_size = Game.tank_size; 
	
	public String name;

	public Obstacle(String name, double posX, double posY)
	{
		this.name = name;
		this.posX = (int) ((posX + 0.5) * obstacle_size);
		this.posY = (int) ((posY + 0.5) * obstacle_size);
		this.color = Obstacle.getRandomColor();
		
		//int col = (int) (this.posX) % 255;
		//this.color = new Color(col, col, col);
		/*if (((this.posY - 25) / 50) % 2 == 1)
		{
			this.color = new Color(255, 0, 0);
		}*/
		
		/*if (this.posX > 200)
		{
			if (this.posX < 700)
			{
				if (this.posY > 200)
				{
					if (this.posY < 700)
					{
						this.color = new Color(0, 0, 255);
					}
				}
			}
		}*/
	}

	public void draw(Graphics g)
	{	
		Drawing drawing = Drawing.window;
		
		g.setColor(this.color);
		drawing.fillRect(g, this.posX, this.posY, draw_size, draw_size);
		
		/*for (int i = 0; i < 10; i++)
		{
			int col = i * 25;
			g.setColor(new Color(col, col, col));
			drawing.fillRect(g, this.posX, this.posY, draw_size - i * 5, draw_size - i * 5);
		}*/
		
		/*for (int i = 0; i < Math.max(1, draw_size * 2 - obstacle_size); i++)
		{
			g.setColor(new Color((int) (color.getRed() - 0.5 * color.getRed() * (obstacle_size - i) / obstacle_size),
					(int) (color.getGreen() - 0.5 * color.getGreen() * (obstacle_size - i) / obstacle_size), 
					(int) (color.getBlue() - 0.5 * color.getBlue() * (obstacle_size - i) / obstacle_size)));
			drawing.fillRect(g, this.posX - 0.5 * i, this.posY - 0.5 * i, Math.min(draw_size * 2, obstacle_size), Math.min(draw_size * 2, obstacle_size));
		}*/
	}

	public void drawOutline(Graphics g)
	{
		g.setColor(this.color);
		Drawing drawing = Drawing.window;
		drawing.fillRect(g, this.posX - Obstacle.obstacle_size * 0.4, this.posY, Obstacle.obstacle_size * 0.2, Obstacle.obstacle_size);
		drawing.fillRect(g, this.posX + Obstacle.obstacle_size * 0.4, this.posY, Obstacle.obstacle_size * 0.2, Obstacle.obstacle_size);
		drawing.fillRect(g, this.posX, this.posY - Obstacle.obstacle_size * 0.4, Obstacle.obstacle_size, Obstacle.obstacle_size * 0.2);
		drawing.fillRect(g, this.posX, this.posY + Obstacle.obstacle_size * 0.4, Obstacle.obstacle_size, Obstacle.obstacle_size * 0.2);
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
