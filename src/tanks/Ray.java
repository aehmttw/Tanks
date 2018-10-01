package tanks;

import java.util.ArrayList;

import tanks.tank.Tank;

public class Ray
{
	public int size = 10;
	public int bounces;
	public double posX;
	public double posY;
	public double vX;
	public double vY;

	public double xMul = 0;
	public double yMul = 0;

	public double speed = 10;

	public boolean skipSelfCheck = false;
	public boolean inShooter = true;
	public int age = 0;

	public Tank tank;
	public Tank targetTank;
	
	public boolean enableBullet = false;

	public ArrayList<Double> bounceX = new ArrayList<Double>();
	public ArrayList<Double> bounceY = new ArrayList<Double>();

	public double targetX;
	public double targetY;

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

	public Movable getTarget(double xMul, double yMul, Tank targetTank)
	{
		this.xMul = xMul;
		this.yMul = yMul;
		this.targetTank = targetTank;
		return this.getTarget();
	}

	public Movable getTarget() 
	{
		while (true)
		{
			age++;
			//Game.effects.add(Effect.createNewEffect(this.posX, this.posY, Effect.EffectType.ray));
			this.posX += this.vX;
			this.posY += this.vY;

			boolean collided = false;

			for (int i = 0; i < Game.obstacles.size(); i++)
			{
				Obstacle o = Game.obstacles.get(i);
				
				if (!o.bulletCollision)
					continue;

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

			if (this.posX + this.size/2 > Drawing.sizeX)
			{
				collided = true;
				this.posX = Drawing.sizeX - this.size/2 - (this.posX + this.size/2 - Drawing.sizeX);
				this.vX = -Math.abs(this.vX);
			}
			if (this.posX - this.size/2 < 0)
			{
				collided = true;
				this.posX = this.size/2 - (this.posX - this.size / 2);
				this.vX = Math.abs(this.vX);
			}
			if (this.posY + this.size/2 > Drawing.sizeY)
			{
				collided = true;
				this.posY = Drawing.sizeY - this.size/2 - (this.posY + this.size/2 - Drawing.sizeY);
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
				if (this.bounces <= 0 || this.age <= 1)
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

					if (this.targetTank != null)
						if (t.equals(this.targetTank))
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
							this.targetX = this.posX;
							this.targetY = this.posY;
							return Game.movables.get(i);
						}
					}				
				}
				else if (Game.movables.get(i) instanceof Bullet && enableBullet)
				{
					Bullet t = (Bullet)(Game.movables.get(i));

					if (Math.abs(this.posX - t.posX) < (t.size + this.size) / 2 &&
							Math.abs(this.posY - t.posY) < (t.size + this.size) / 2)
					{
						if (age * speed > Math.sqrt(2) * tank.size / 2 + 10 || skipSelfCheck)
						{
							//Game.effects.add(new Effect(this.posX, this.posY, Effect.EffectType.laser));
							this.targetX = this.posX;
							this.targetY = this.posY;
							System.out.println("hi");
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
				
				if (!o.tankCollision)
					continue;
					
				double horizontalDist = Math.abs(this.posX - o.posX);
				double verticalDist = Math.abs(this.posY - o.posY);

				double bound = this.size / 2 + Obstacle.obstacle_size / 2;

				if (horizontalDist < bound && verticalDist < bound)
				{
					return age;
				}

			}

			for (int i = 0; i < Game.movables.size(); i++)
			{
				Movable o = Game.movables.get(i);

				if (o instanceof Tank)
				{
					double horizontalDist = Math.abs(this.posX - o.posX);
					double verticalDist = Math.abs(this.posY - o.posY);

					double bound = this.size / 2 + ((Tank)o).size / 2;

					if (horizontalDist < bound && verticalDist < bound)
					{
						return age;
					}
				}

			}


			if (this.posX + this.size/2 > Drawing.sizeX)
			{
				return this.age;
			}
			else if (this.posX - this.size/2 < 0)
			{
				return this.age;
			}
			else if (this.posY + this.size/2 > Drawing.sizeY)
			{
				return this.age;
			}
			else if (this.posY - this.size/2 < 0)
			{
				return this.age;
			}
		}
	}
	
	public void moveOut(double amount)
	{
		this.posX += this.vX * amount;
		this.posY += this.vY * amount;
	}
}
