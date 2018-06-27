package tanks;

import java.util.ArrayList;

public class Ray
{
	int size = 10;
	int bounces;
	double posX;
	double posY;
	double vX;
	double vY;

	double xMul = 0;
	double yMul = 0;

	double speed = 10;
	
	boolean skipSelfCheck = false;
	boolean inShooter = true;
	int age = 0;

	Tank tank;

	ArrayList<Double> bounceX = new ArrayList<Double>();
	ArrayList<Double> bounceY = new ArrayList<Double>();
	
	public Ray(double x, double y, double angle, int bounces, Tank tank) 
	{
		this.vX = speed * Math.cos(angle);
		this.vY = speed * Math.sin(angle);

		this.posX = x;
		this.posY = y;
		this.bounces = bounces;

		this.tank = tank;
	}
	
	public Ray(double x, double y, double angle, int bounces, Tank tank, int speed) 
	{
		this.vX = speed * Math.cos(angle);
		this.vY = speed * Math.sin(angle);

		this.posX = x;
		this.posY = y;
		this.bounces = bounces;

		this.tank = tank;
	}

	public Movable getTarget(double xMul, double yMul)
	{
		this.xMul = xMul;
		this.yMul = yMul;
		return this.getTarget();
	}
	
	public Movable getTarget() 
	{
		while (true)
		{
			age++;
			//Game.effects.add(new Effect(this.posX, this.posY, Effect.EffectType.ray));
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
						bounceX.add(this.posX);
						bounceY.add(this.posY);
					}
					else if (dy <= 0 && dy > 0 - bound && horizontalDist < verticalDist)
					{
						this.posY += verticalDist - bound;
						this.vY = -Math.abs(this.vY);
						collided = true;
						bounceX.add(this.posX);
						bounceY.add(this.posY);
					}
					else if (dx >= 0 && dx < bound && horizontalDist > verticalDist)
					{
						this.posX -= horizontalDist - bound;
						this.vX = Math.abs(this.vX);
						collided = true;
						bounceX.add(this.posX);
						bounceY.add(this.posY);
					}
					else if (dy >= 0 && dy < bound && horizontalDist < verticalDist)
					{
						this.posY -= verticalDist - bound;
						this.vY = Math.abs(this.vY);
						collided = true;
						bounceX.add(this.posX);
						bounceY.add(this.posY);
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
					Tank t = (Tank)(Game.movables.get(i));
					
					double xMult = 1;
					double yMult = 1;

					if (t.equals(this))
					{
						xMult = xMul;
						yMult = yMul;
					}
					
					if (Math.abs(this.posX - t.posX) < (t.size * xMult + this.size) / 2 &&
							Math.abs(this.posY - t.posY) < (t.size * yMult + this.size) / 2)
					{
						if (!Game.movables.get(i).equals(tank))
						{
							this.inShooter = false;
						}
						if (!Game.movables.get(i).equals(tank) || age * speed > Math.sqrt(2) * tank.size / 2 + 10 || skipSelfCheck)
						{
							//Game.effects.add(new Effect(this.posX, this.posY, Effect.EffectType.laser));
							return Game.movables.get(i);
						}
					}				
				}
			}
		}
	}
	
	public int getDist() 
	{
		while (true)
		{
			age++;
			
			//Game.effects.add(new Effect(this.posX, this.posY, Effect.EffectType.ray));
			this.posX += this.vX;
			this.posY += this.vY;

			for (int i = 0; i < Game.obstacles.size(); i++)
			{
				Obstacle o = Game.obstacles.get(i);

				double horizontalDist = Math.abs(this.posX - o.posX);
				double verticalDist = Math.abs(this.posY - o.posY);
				
				double bound = this.size / 2 + Obstacle.obstacle_size / 2;

				if (horizontalDist < bound && verticalDist < bound)
				{
					return age;
				}

			}

			if (this.posX + this.size/2 > Screen.sizeX)
			{
				return this.age;
			}
			else if (this.posX - this.size/2 < 0)
			{
				return this.age;
			}
			else if (this.posY + this.size/2 > Screen.sizeY)
			{
				return this.age;
			}
			else if (this.posY - this.size/2 < 0)
			{
				return this.age;
			}
		}
	}
}
