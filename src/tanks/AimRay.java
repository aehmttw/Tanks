package tanks;

public class AimRay
{
	int size = 10;
	int bounces;
	double posX;
	double posY;
	double vX;
	double vY;

	double speed = 10;
	
	boolean inShooter = true;
	int age = 0;

	Tank tank;

	public AimRay(double x, double y, double angle, int bounces, Tank tank) 
	{
		this.vX = speed * Math.cos(angle);
		this.vY = speed * Math.sin(angle);

		this.posX = x;
		this.posY = y;
		this.bounces = bounces;

		this.tank = tank;
	}

	public Movable getTarget() 
	{
		while (true)
		{
			age++;
			//Game.movables.add(new Effect(this.posX, this.posY, Effect.EffectType.aimRay));
			this.posX += this.vX;
			this.posY += this.vY;

			boolean collided = false;

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
						this.vX = -Math.abs(this.vX);
						collided = true;
					}
					else if (dy <= 0 && dy > 0 - bound && horizontalDist < verticalDist)
					{
						this.posY += verticalDist - bound;
						this.vY = -Math.abs(this.vY);
						collided = true;
					}
					else if (dx >= 0 && dx < bound && horizontalDist > verticalDist)
					{
						this.posX -= horizontalDist - bound;
						this.vX = Math.abs(this.vX);
						collided = true;
					}
					else if (dy >= 0 && dy < bound && horizontalDist < verticalDist)
					{
						this.posY -= verticalDist - bound;
						this.vY = Math.abs(this.vY);
						collided = true;
					}
				}

			}

			if (this.posX + this.size/2 > Screen.sizeX)
			{
				collided = true;
				this.posX = Screen.sizeX - this.size/2 - (this.posX + this.size/2 - Screen.sizeX);
				this.vX = -Math.abs(this.vX);
			}
			if (this.posX - this.size/2 < 0)
			{
				collided = true;
				this.posX = this.size/2 - (this.posX - this.size / 2);
				this.vX = Math.abs(this.vX);
			}
			if (this.posY + this.size/2 > Screen.sizeY)
			{
				collided = true;
				this.posY = Screen.sizeY - this.size/2 - (this.posY + this.size/2 - Screen.sizeY);
				this.vY = -Math.abs(this.vY); 
			}
			if (this.posY - this.size/2 < 0)
			{
				collided = true;
				this.posY = this.size/2 - (this.posY - this.size / 2);
				this.vY = Math.abs(this.vY);
			}
			if (collided)
			{
				if (this.bounces <= 0)
				{
					return null;
				}
				this.bounces--;
			}
			for (int i = 0; i < Game.movables.size(); i++)
			{
				if (Game.movables.get(i) instanceof Tank)
				{
					if (Math.abs(this.posX - Game.movables.get(i).posX) < (Game.tank_size + this.size) / 2 &&
							Math.abs(this.posY - Game.movables.get(i).posY) < (Game.tank_size + this.size) / 2)
					{
						if (!Game.movables.get(i).equals(tank))
						{
							this.inShooter = false;
						}
						if (!Game.movables.get(i).equals(tank) || age * speed > Math.sqrt(2) * tank.size / 2)
						{
							//Game.effects.add(new Effect(this.posX, this.posY, Effect.EffectType.fire));
							return Game.movables.get(i);
						}
					}				
				}
			}
		}
	}
}
