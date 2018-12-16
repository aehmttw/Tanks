package tanks;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

public class ObstacleUnknown extends Obstacle
{

	public ObstacleUnknown(String name, double posX, double posY) 
	{
		super(name, posX, posY);
		
		this.color = Color.red;
		this.destructible = false;
		this.tankCollision = false;
		this.bulletCollision = false;
	}
	
	@Override
	public void draw(Graphics g)
	{
		g.setFont(g.getFont().deriveFont(Font.BOLD, (float) (12 * Drawing.window.getScale())));
		Drawing.window.drawText(g, this.posX, this.posY + 32, this.name);
		
		super.draw(g);
	}

}
 