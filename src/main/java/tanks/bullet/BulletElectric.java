package tanks.bullet;

import tanks.*;
import tanks.AttributeModifier.Operation;
import tanks.network.event.EventBulletDestroyed;
import tanks.network.event.EventBulletElectricStunEffect;
import tanks.network.event.EventBulletInstantWaypoint;
import tanks.network.event.EventShootBullet;
import tanks.gui.screen.ScreenGame;
import tanks.hotbar.item.ItemBullet;
import tanks.tank.Mine;
import tanks.tank.Tank;

import java.util.ArrayList;

public class BulletElectric extends BulletInstant
{
	public static String bullet_name = "electric";

	public int chain;
	public double delay = 0;
	public ArrayList<Movable> targets;
	public double invulnerability = 0;
	public Movable target = null;

	public BulletElectric(double x, double y, int bounces, Tank t, ItemBullet ib)
	{
		this(x, y, bounces, t, new ArrayList<>(), false, ib);
	}

	public BulletElectric(double x, double y, int bounces, Tank t, ArrayList<Movable> targets, boolean affectsLiveBullets, ItemBullet ib)
	{
		super(x, y, 0, t, affectsLiveBullets, ib);
		chain = bounces;
		this.name = "electric";

		this.targets = targets;
		this.damage = 0.125;
		this.effect = BulletEffect.none;
		this.itemSound = "laser.ogg";

		this.baseColorR = 0;
		this.baseColorG = 255;
		this.baseColorB = 255;
	}

	public void sendEvent()
	{
		if (!this.tank.isRemote)
			Game.eventsOut.add(new EventShootBullet(this));
	}

	public void shoot()
	{
		this.collisionX = this.posX;
		this.collisionY = this.posY;
		this.lastX = this.posX;
		this.lastY = this.posY;
		this.lastZ = this.iPosZ;
		this.expired = true;

		this.sendEvent();

		if (this.target != null)
		{
			double angle = this.getAngleInDirection(target.posX, target.posY);
			this.addPolarMotion(angle, 25.0 / 8);
		}

		if (!this.tank.isRemote)
		{
			this.saveTarget();
		}

		while (!this.destroy)
		{
			if (ScreenGame.finished)
				this.destroy = true;

			this.move();
		}

		this.addDestroyEffect();

		if (!this.tank.isRemote)
		{
			this.saveTarget();

			for (int i = 0; i < this.xTargets.size(); i++)
			{
				Game.eventsOut.add(new EventBulletInstantWaypoint(this, this.xTargets.get(i), this.yTargets.get(i)));
			}

			Game.eventsOut.add(new EventBulletDestroyed(this));
		}
	}

	@Override
	public void update()
	{
		if (!this.expired)
		{
			if (this.delay > 0)
			{
				this.delay -= Panel.frameFrequency;
				return;
			}

			if (this.delay <= 0)
			{
				if (!this.tank.destroy)
					this.shoot();

				if (!freeIDs.contains(this.networkID))
				{
					freeIDs.add(this.networkID);
					idMap.remove(this.networkID);
				}
			}
		}

		super.update();
	}

	public void move()
	{	
		this.invulnerability -= Panel.frameFrequency;
		super.superUpdate();
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
			this.playPopSound = true;
			super.collidedWithObject(m);
			this.playPopSound = false;
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

		AttributeModifier a = new AttributeModifier(AttributeModifier.velocity, Operation.multiply, -1);
		a.duration = 100;
		movable.addAttribute(a);

		if (chain > 0 && !this.tank.isRemote)
		{
			double nd = Double.MAX_VALUE;
			Movable n = null;

			for (int i = 0; i < Game.movables.size(); i++)
			{
				Movable m = Game.movables.get(i);
				if (!Team.isAllied(this, m) && this != m && !m.destroy && !this.targets.contains(m) && ((m instanceof Bullet && ((Bullet) m).enableCollision && ((Bullet) m).bulletCollision) || m instanceof Mine || m instanceof Tank))
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
				BulletElectric b = new BulletElectric(this.posX, this.posY, this.chain - 1, this.tank, this.targets, this.affectsMaxLiveBullets, this.item);
				b.iPosZ = this.posZ;
				b.damage = this.damage;
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

		if (movable instanceof Tank && !this.tank.isRemote)
		{
			Game.eventsOut.add(new EventBulletElectricStunEffect(this.posX, this.posY, this.posZ, 1));

			if (Game.effectsEnabled)
			{
				for (int i = 0; i < 25 * Game.effectMultiplier; i++)
				{
					Effect e = Effect.createNewEffect(this.posX, this.posY, this.posZ, Effect.EffectType.stun);
					double var = 50;
					e.colR = Math.min(255, Math.max(0, 0 + Math.random() * var - var / 2));
					e.colG = Math.min(255, Math.max(0, 255 + Math.random() * var - var / 2));
					e.colB = Math.min(255, Math.max(0, 255 + Math.random() * var - var / 2));
					e.glowR = 0;
					e.glowG = 128;
					e.glowB = 128;
					Game.effects.add(e);
				}
			}
		}
	}

	@Override
	public void collided()
	{
		double dist = Math.sqrt(Math.pow(this.collisionX - this.lastX, 2) + Math.pow(this.collisionY - this.lastY, 2));

		double r = 200;
		boolean glows = false;
		double size = 0.25;

		if (Game.fancyBulletTrails)
		{
			for (int j = 0; j < 2; j++)
			{
				int segs = (int) ((Math.random() * 0.4 + 0.8) * dist / 50);

				double lX = this.lastX;
				double lY = this.lastY;
				double lZ = this.lastZ;

				for (int i = 0; i < segs; i++)
				{
					double frac = (i + 1.0) / (segs + 1);
					double nX = (1 - frac) * this.lastX + frac * this.collisionX + (Math.random() - 0.5) * 50;
					double nY = (1 - frac) * this.lastY + frac * this.collisionY + (Math.random() - 0.5) * 50;
					double nZ = (1 - frac) * this.lastZ + frac * this.posZ + (Math.random() - 0.5) * 30;
					Laser l = new Laser(lX, lY, lZ, nX, nY, nZ, this.size * size, this.getAngleInDirection(this.lastX, this.lastY), r, 255, 255);
					l.glows = glows;
					this.segments.add(l);
					lX = nX;
					lY = nY;
					lZ = nZ;
				}
				Laser l = new Laser(lX, lY, lZ, this.collisionX, this.collisionY, this.posZ, this.size * size, this.getAngleInDirection(this.lastX, this.lastY), r, 255, 255);
				l.glows = glows;
				this.segments.add(l);
			}
		}

		this.segments.add(new Laser(this.lastX, this.lastY, this.lastZ, this.collisionX, this.collisionY, this.posZ, this.size / 2, this.getAngleInDirection(this.lastX, this.lastY), this.baseColorR, this.baseColorG, this.baseColorB));
		this.lastX = this.collisionX;
		this.lastY = this.collisionY;
		this.lastZ = this.posZ;

		if (!this.isRemote)
		{
			this.xTargets.add(this.collisionX);
			this.yTargets.add(this.collisionY);
		}
	}
}
