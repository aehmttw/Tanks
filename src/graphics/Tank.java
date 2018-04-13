package graphics;

import java.awt.Color;
import java.awt.Graphics;

public abstract class Tank extends Movable
{
	double angle = 0;

	double accel = 0.1;
	double maxV = 2.5;
	int liveBullets = 0;
	int size;
	public Color color;
	int liveBulletMax;
	int age = 0;
	int destroyTimer = 0;

	Turret turret;
	
	public Tank(double x, double y, int size, Color color) 
	{
		super(x, y);
		this.size = size;
		this.color = color;
		turret = new Turret(this);
	}

	@Override
	public void checkCollision() 
	{
		if (this.posX + this.size / 2 > Screen.sizeX)
			this.posX = Screen.sizeX - this.size / 2;
		if (this.posY + this.size / 2 > Screen.sizeY)
			this.posY = Screen.sizeY - this.size / 2;
		if (this.posX - this.size / 2 < 0)
			this.posX = this.size / 2;
		if (this.posY - this.size / 2 < 0)
			this.posY = this.size / 2;

		for (int i = 0; i < Game.obstacles.size(); i++)
		{
			Obstacle o = Game.obstacles.get(i);

			double horizontalDist = Math.abs(this.posX - o.posX);
			double verticalDist = Math.abs(this.posY - o.posY);

			double dx = this.posX - o.posX;
			double dy = this.posY - o.posY;

			double bound = this.size / 2 + Obstacle.obstacle_size / 2;

			if (horizontalDist < bound && verticalDist < bound)
			{
				if (dx <= 0 && dx > 0 - bound && horizontalDist > verticalDist)
				{
					this.posX += horizontalDist - bound;
				}
				else if (dy <= 0 && dy > 0 - bound && horizontalDist < verticalDist)
				{
					this.posY += verticalDist - bound;
				}
				else if (dx >= 0 && dx < bound && horizontalDist > verticalDist)
				{
					this.posX -= horizontalDist - bound;
				}
				else if (dy >= 0 && dy < bound && horizontalDist < verticalDist)
				{
					this.posY -= verticalDist - bound;
				}
			}
		}

		for (int i = 0; i < Game.movables.size(); i++)
		{
			Movable o = Game.movables.get(i);
			if (o instanceof Tank)
			{
				double horizontalDist = Math.abs(this.posX - o.posX);
				double verticalDist = Math.abs(this.posY - o.posY);

				double dx = this.posX - o.posX;
				double dy = this.posY - o.posY;

				double bound = this.size / 2 + Obstacle.obstacle_size / 2;

				if (horizontalDist < bound && verticalDist < bound)
				{
					if (dx <= 0 && dx > 0 - bound && horizontalDist > verticalDist)
					{
						this.posX += horizontalDist - bound;
					}
					else if (dy <= 0 && dy > 0 - bound && horizontalDist < verticalDist)
					{
						this.posY += verticalDist - bound;
					}
					else if (dx >= 0 && dx < bound && horizontalDist > verticalDist)
					{
						this.posX -= horizontalDist - bound;
					}
					else if (dy >= 0 && dy < bound && horizontalDist < verticalDist)
					{
						this.posY -= verticalDist - bound;
					}
				}
			}
		}
	}

	@Override
	public void update()
	{
		age++;
		
		if (destroy)
			this.destroyTimer++;
		
		if (this.destroyTimer > Game.tank_size)
			Game.movables.remove(this);
		
		super.update();
	}

	public abstract void shoot();

	@Override
	public void draw(Graphics g) 
	{
		g.setColor(this.color);
		g.fillRect((int)this.posX - this.size / 2 + destroyTimer / 2 + Math.max(Game.tank_size - age, 0) / 2, (int)this.posY - this.size / 2 + destroyTimer / 2 + Math.max(Game.tank_size - age, 0) / 2, this.size - destroyTimer - Math.max(Game.tank_size - age, 0), this.size - destroyTimer - Math.max(Game.tank_size - age, 0));
		this.turret.draw(g, angle);
	}

}
