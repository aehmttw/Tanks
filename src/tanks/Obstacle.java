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
	}

	public void draw(Graphics g)
	{	
		g.setColor(color);

		Drawing.fillRect(g, this.posX, this.posY, draw_size, draw_size);
	}

	public void drawOutline(Graphics g)
	{
		g.setColor(this.color);
		Drawing.fillRect(g, this.posX - Obstacle.obstacle_size * 0.4, this.posY, Obstacle.obstacle_size * 0.2, Obstacle.obstacle_size);
		Drawing.fillRect(g, this.posX + Obstacle.obstacle_size * 0.4, this.posY, Obstacle.obstacle_size * 0.2, Obstacle.obstacle_size);
		Drawing.fillRect(g, this.posX, this.posY - Obstacle.obstacle_size * 0.4, Obstacle.obstacle_size, Obstacle.obstacle_size * 0.2);
		Drawing.fillRect(g, this.posX, this.posY + Obstacle.obstacle_size * 0.4, Obstacle.obstacle_size, Obstacle.obstacle_size * 0.2);
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
