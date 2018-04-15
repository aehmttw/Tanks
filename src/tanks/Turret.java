package tanks;

import java.awt.Color;
import java.awt.Graphics;

public class Turret extends Movable
{
	int size = 10;
	
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
		
		for (int i = 0; i < Game.tank_size - this.tank.destroyTimer - Math.max(Game.tank_size - tank.drawAge, 0); i++)
		{
			p.setColor(Color.BLACK);
			int s = (int) (size * (Game.tank_size - this.tank.destroyTimer - Math.max(Game.tank_size - tank.drawAge, 0)) / Game.tank_size);
			p.fillOval((int)(this.posX - s/2), (int)(this.posY - s/2), s, s);
			this.posX += this.vX;
			this.posY += this.vY;
		}
	}
	
	@Override
	public void update() {}

	@Override
	public void draw(Graphics p) {}

}
