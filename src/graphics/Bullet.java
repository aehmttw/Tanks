package graphics;

import java.awt.Color;
import java.awt.Graphics;


public class Bullet extends Movable
{
	public static enum BulletEffect {none, fire, fireTrail, trail};

	static int bullet_size = 10;
	int size;
	int bounces;
	Color color;
	int destroyTimer = 0;
	Tank tank;
	public BulletEffect effect = BulletEffect.none;

	public Bullet(double x, double y, Color color, int bounces, Tank t)
	{
		super(x, y);
		this.vX = 0;
		this.vY = 0;
		this.size = bullet_size;
		this.color = color;
		this.bounces = bounces;
		this.tank = t;
		t.liveBullets++;
	}

	public void moveOut(int amount)
	{
		this.moveInDirection(vX, vY, amount);
	}

	@Override
	public void checkCollision() 
	{
		if (this.destroy)
			return;

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

		for (int i = 0; i < Game.movables.size(); i++)
		{
			Movable o = Game.movables.get(i);

			if (o instanceof Tank)
			{
				double horizontalDist = Math.abs(this.posX - o.posX);
				double verticalDist = Math.abs(this.posY - o.posY);

				double bound = this.size / 2 + Game.tank_size / 2;

				if (horizontalDist < bound && verticalDist < bound)
				{
					this.destroy = true;
					this.vX = 0;
					this.vY = 0;
					((Tank) o).destroy = true;
				}
			}
			else if (o instanceof Bullet && o != this)
			{
				if (!((Bullet)o).destroy)
				{

					double horizontalDist = Math.abs(this.posX - o.posX);
					double verticalDist = Math.abs(this.posY - o.posY);

					double bound = this.size / 2 + Bullet.bullet_size / 2;

					if (horizontalDist < bound && verticalDist < bound)
					{
						this.destroy = true;
						this.vX = 0;
						this.vY = 0;
						this.destroy = true;
						((Bullet)o).destroy = true;

						this.vX = 0;
						this.vY = 0;
						o.vX = 0;
						o.vY = 0;
					}
				}
			}

		}


		if (collided)
		{
			if (this.bounces <= 0)
			{
				this.destroy = true;
				this.vX = 0;
				this.vY = 0;
			}
			this.bounces--;
		}
	}

	@Override
	public void update()
	{
		if (destroy)
			this.destroyTimer++;
		else
		{
			if (this.effect.equals(BulletEffect.trail))
				Game.movables.add(new Effect(this.posX, this.posY, Effect.EffectType.trail));

			if (this.effect.equals(BulletEffect.fireTrail))
				Game.movables.add(new Effect(this.posX, this.posY, Effect.EffectType.smokeTrail));

			if (this.effect.equals(BulletEffect.fire) || this.effect.equals(BulletEffect.fireTrail))
				Game.movables.add(new Effect(this.posX, this.posY, Effect.EffectType.fire));
		}

		if (this.destroyTimer == 60)
		{
			this.tank.liveBullets--;
			Game.movables.remove(this);
		}

		super.update();
	}

	@Override
	public void draw(Graphics p) 
	{
		double opacity = ((60 - destroyTimer) / 60.0);
		p.setColor(new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), (int)(opacity * opacity * opacity * 255.0)));
		p.fillOval((int)(posX - size / 2 - destroyTimer / 2), (int)(posY - size / 2 - destroyTimer / 2), size + destroyTimer, size + destroyTimer);
	}

}
