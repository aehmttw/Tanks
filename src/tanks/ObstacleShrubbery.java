package tanks;

import java.awt.Color;
import java.awt.Graphics;

public class ObstacleShrubbery extends Obstacle
{
	
	public double opacity = 255;
	public ObstacleShrubbery(String name, double posX, double posY) 
	{
		super(name, posX, posY);
		
		this.destructible = true;
		this.tankCollision = false;
		this.bulletCollision = false;
		this.checkForObjects = true;
		this.color = new Color((int) (Math.random() * 20), (int) (Math.random() * 50) + 150, (int) (Math.random() * 20));
	}
	
	public void draw(Graphics g)
	{	
		this.opacity = Math.min(this.opacity + Panel.frameFrequency, 255);
		this.color = new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), (int) opacity);
		g.setColor(this.color);
		Drawing.window.fillRect(g, this.posX, this.posY, draw_size, draw_size);
	}
	
	@Override
	public void onObjectEntry(Movable m)
	{
		this.opacity = Math.max(this.opacity - Panel.frameFrequency * Math.pow(Math.abs(m.vX) + Math.abs(m.vY), 2), 127);
		m.hiddenTimer = Math.min(100, m.hiddenTimer + (this.opacity - 127) / 255);
		m.canHide = true;
	}

}
