package tanks;

import java.awt.Color;
import java.awt.Graphics;

public abstract class Tank extends Movable
{
	double angle = 0;

	double accel = 0.1;
	double maxV = 2.5;
	int liveBullets = 0;
	int liveMines = 0;
	int size;
	public Color color;
	int liveBulletMax;
	int liveMinesMax;
	int drawAge = 0;
	int destroyTimer = 0;
	boolean hasCollided = false;
	
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
		hasCollided = false;
		
		if (this.posX + this.size / 2 > Screen.sizeX)
		{
			this.posX = Screen.sizeX - this.size / 2;
			hasCollided = true;
		}
		if (this.posY + this.size / 2 > Screen.sizeY)
		{
			this.posY = Screen.sizeY - this.size / 2;
			hasCollided = true;
		}
		if (this.posX - this.size / 2 < 0)
		{
			this.posX = this.size / 2;
			hasCollided = true;
		}
		if (this.posY - this.size / 2 < 0)
		{
			this.posY = this.size / 2;
			hasCollided = true;
		}

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
					hasCollided = true;
					this.posX += horizontalDist - bound;
				}
				else if (dy <= 0 && dy > 0 - bound && horizontalDist < verticalDist)
				{
					hasCollided = true;
					this.posY += verticalDist - bound;
				}
				else if (dx >= 0 && dx < bound && horizontalDist > verticalDist)
				{
					hasCollided = true;
					this.posX -= horizontalDist - bound;
				}
				else if (dy >= 0 && dy < bound && horizontalDist < verticalDist)
				{
					hasCollided = true;
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
						hasCollided = true;
						this.posX += horizontalDist - bound;
					}
					else if (dy <= 0 && dy > 0 - bound && horizontalDist < verticalDist)
					{
						hasCollided = true;
						this.posY += verticalDist - bound;
					}
					else if (dx >= 0 && dx < bound && horizontalDist > verticalDist)
					{
						hasCollided = true;
						this.posX -= horizontalDist - bound;
					}
					else if (dy >= 0 && dy < bound && horizontalDist < verticalDist)
					{
						hasCollided = true;
						this.posY -= verticalDist - bound;
					}
				}
			}
		}
	}

	@Override
	public void update()
	{		
		if (destroy)
			this.destroyTimer++;
		
		if (this.destroyTimer > Game.tank_size)
			Game.removeMovables.add(this);
		
		super.update();
	}

	public abstract void shoot();

	@Override
	public void draw(Graphics g) 
	{
		drawAge++;
		g.setColor(this.color);
		Screen.fillRect(g, this.posX, this.posY, this.size - destroyTimer - Math.max(Game.tank_size - drawAge, 0), this.size - destroyTimer - Math.max(Game.tank_size - drawAge, 0));
		this.turret.draw(g, angle);
	}

}
