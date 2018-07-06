package tanks;

import java.awt.Color;
import java.awt.Graphics;

public class Turret extends Movable
{
	int size = 8;
	int length = Game.tank_size;

	Tank tank;
	
	public Turret(Tank t) 
	{
		super(t.posX, t.posY);
		this.tank = t;
	}
	
	@Override
	public void checkCollision() {}

	public void draw(Graphics p, double angle) 
	{
		this.posX = tank.posX;
		this.posY = tank.posY;
		
		this.setPolarMotion(angle, 1);
		
		for (int i = 0; i < length * (Game.tank_size - this.tank.destroyTimer) / Game.tank_size - Math.max(Game.tank_size - tank.drawAge, 0); i++)
		{
			//p.setColor(new Color(75, 40, 0));
			p.setColor(new Color((tank.color.getRed() + 64) / 2, (tank.color.getGreen() + 64) / 2, (tank.color.getBlue() + 64) / 2));
			int s = (int) (size * (Game.tank_size - this.tank.destroyTimer - Math.max(Game.tank_size - tank.drawAge, 0)) / Game.tank_size);
			Screen.fillOval(p, this.posX, this.posY, s, s);
			this.posX += this.vX;
			this.posY += this.vY;
		}
	}
	
	@Override
	public void update() {}

	@Override
	public void draw(Graphics p) {}

}
