package tanks;

import java.util.ArrayList;

import tanks.AttributeModifier.Operation;
import tanks.tank.Tank;

public class BulletElectric extends Bullet
{
	public int chain = 0;
	public double delay = 0;
	public ArrayList<Movable> targets;
	public double invulnerability = 0;
	public Movable target = null;

	public BulletElectric(double x, double y, int bounces, Tank t) 
	{
		this(y, y, bounces, t, new ArrayList<Movable>());
	}
	
	/** Do not use, instead use the constructor with primitive data types. */
	@Deprecated
	public BulletElectric(Double x, Double y, Integer bounces, Tank t, ItemBullet ib) 
	{
		this(x, y, bounces, t, new ArrayList<Movable>());
		this.item = ib;
		this.item.liveBullets--;
	}
	
	public BulletElectric(double x, double y, int bounces, Tank t, ArrayList<Movable> targets) 
	{
		super(x, y, 0, t);
		chain = bounces;
		
		if (targets.size() == 0)
			t.liveBullets--;
		
		this.targets = targets;
		this.damage = 0.1;
	}

	public void shoot()
	{
		if (this.target != null)
		{
			double angle =  this.getAngleInDirection(target.posX, target.posY);
			this.addPolarMotion(angle, 25.0 / 4);
		}

		while (!this.destroy)
		{
			if (ScreenGame.finished)
				this.destroy = true;

			this.move();				

			Game.effects.add(Effect.createNewEffect(this.posX, this.posY, Effect.EffectType.electric));
		}
	}

	@Override
	public void update()
	{
		if (this.delay > 0)
		{
			this.delay -= Panel.frameFrequency;
			return;
		}

		if (this.delay <= 0)
		{
			Game.movables.remove(this);
			this.shoot();
		}
	}

	public void move()
	{	
		this.invulnerability -= Panel.frameFrequency;
		super.update();
	}

	@Override
	public void collidedWithTank(Tank t)
	{
		if (this.invulnerability <= 0 && !this.destroy)
		{
			this.collided(t);
			super.collidedWithTank(t);
		}
	}

	@Override
	public void collidedWithObject(Movable m)
	{
		if (this.invulnerability <= 0 && !this.destroy)
		{
			this.collided(m);
			super.collidedWithObject(m);
		}
	}

	public void collided(Movable movable)
	{
		this.destroy = true;

		if (movable instanceof BulletElectric)
		{
			return;		
		}

		this.targets.add(movable);

		this.posX = movable.posX;
		this.posY = movable.posY;

		AttributeModifier a = new AttributeModifier("velocity", Operation.multiply, -1);
		a.duration = 100;
		movable.attributes.add(a);

		if (chain > 0)
		{
			double nd = Double.MAX_VALUE;
			Movable n = null;

			for (int i = 0; i < Game.movables.size(); i++)
			{
				Movable m = Game.movables.get(i);
				if (!Team.isAllied(this, m) && this != m && !this.targets.contains(m))
				{
					double d = Movable.distanceBetween(this, m);
					if (d < nd)
					{
						nd = d;
						n = m;
					}
				}
			}

			if (n != null)
			{
				BulletElectric b = new BulletElectric(this.posX, this.posY, this.chain - 1, this.tank, this.targets);
				b.team = this.team;

				b.delay = 10;

				if (movable instanceof Tank)
					b.invulnerability = 16;
				else
					b.invulnerability = 2;

				b.target = n;

				Game.movables.add(b);
			}
		}

		if (Game.fancyGraphics && movable instanceof Tank)
		{
			for (int i = 0; i < 25; i++)
			{
				Effect e = Effect.createNewEffect(this.posX, this.posY, Effect.EffectType.stun);
				int var = 50;
				e.colR = Math.min(255, Math.max(0, 0 + Math.random() * var - var / 2));
				e.colG = Math.min(255, Math.max(0, 255 + Math.random() * var - var / 2));
				e.colB = Math.min(255, Math.max(0, 255 + Math.random() * var - var / 2));
				Game.effects.add(e);
			}
		}
	}
	
	@Override
	public void draw()
	{
		
	}
}
