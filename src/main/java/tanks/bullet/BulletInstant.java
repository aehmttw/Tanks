package tanks.bullet;

import tanks.Effect;
import tanks.Game;
import tanks.Panel;
import tanks.network.event.EventBulletDestroyed;
import tanks.network.event.EventBulletInstantWaypoint;
import tanks.network.event.EventShootBullet;
import tanks.gui.screen.ScreenGame;
import tanks.hotbar.item.ItemBullet;
import tanks.tank.Tank;

import java.util.ArrayList;

public abstract class BulletInstant extends Bullet
{
	public ArrayList<Double> xTargets = new ArrayList<>();
	public ArrayList<Double> yTargets = new ArrayList<>();

	public ArrayList<Laser> segments = new ArrayList<>();

	public double lastX;
	public double lastY;
	public double lastZ;

	public boolean expired = false;

	public BulletInstant(double x, double y, int bounces, Tank t, boolean affectsMaxLiveBullets, ItemBullet ib)
	{
		super(x, y, bounces, t, affectsMaxLiveBullets, ib);
		this.enableExternalCollisions = false;
		this.playPopSound = false;
		this.playBounceSound = false;
	}

	public void saveTarget()
	{
		this.xTargets.add(this.collisionX);
		this.yTargets.add(this.collisionY);
	}

	public void addDestroyEffect()
	{
		if (Game.effectsEnabled)
		{
			double mul = 4;
			if (this.item.cooldownBase <= 0)
				mul = 0.25;

			for (int i = 0; i < this.size * mul * Game.effectMultiplier; i++)
			{
				Effect e = Effect.createNewEffect(this.posX, this.posY, this.posZ, Effect.EffectType.piece);
				double var = 50;
				e.maxAge /= 2;
				e.colR = Math.min(255, Math.max(0, this.baseColorR + Math.random() * var - var / 2));
				e.colG = Math.min(255, Math.max(0, this.baseColorG + Math.random() * var - var / 2));
				e.colB = Math.min(255, Math.max(0, this.baseColorB + Math.random() * var - var / 2));

				if (Game.enable3d)
					e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.random() * Math.PI, Math.random() * this.size / 50.0 * 4);
				else
					e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() * this.size / 50.0 * 4);

				Game.effects.add(e);
			}
		}
	}

	public void shoot()
	{
		if (this.expired)
			return;

		Game.eventsOut.add(new EventShootBullet(this));

		if (!this.tank.isRemote)
		{
			this.collisionX = this.posX;
			this.collisionY = this.posY;
			this.lastX = this.posX;
			this.lastY = this.posY;
			this.lastZ = this.iPosZ;
			this.saveTarget();
		}

		while (!this.destroy)
		{
			if (ScreenGame.finished)
				this.destroy = true;

			super.update();

			if (Math.abs(this.lastFinalVX) < 0.01 && Math.abs(this.lastFinalVY) < 0.01)
				this.destroy = true;
		}

		if (!this.tank.isRemote)
		{
			this.saveTarget();

			for (int i = 0; i < this.xTargets.size(); i++)
			{
				Game.eventsOut.add(new EventBulletInstantWaypoint(this, this.xTargets.get(i), this.yTargets.get(i)));
			}

			Game.eventsOut.add(new EventBulletDestroyed(this));
		}

		freeIDs.add(this.networkID);
		idMap.remove(this.networkID);

		this.addDestroyEffect();
		this.expired = true;
	}

	@Override
	public void collided()
	{
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

	public void remoteShoot()
	{
		for (int i = 0; i < xTargets.size() - 1; i++)
		{
			double iX = xTargets.get(i);
			double iY = yTargets.get(i);
			double dX = xTargets.get(i + 1) - iX;
			double dY = yTargets.get(i + 1) - iY;

			this.posX = iX;
			this.posY = iY;

			double z = Game.tile_size / 4;
			if (i == 0)
				z = this.iPosZ;

			this.segments.add(new Laser(iX + dX, iY + dY, z, iX, iY, Game.tile_size / 4, this.size / 2, this.getAngleInDirection(iX + dX, iY + dY), this.baseColorR, this.baseColorG, this.baseColorB));
			this.expired = true;
		}

		Game.movables.add(this);
		this.addDestroyEffect();
	}

	@Override
	public void update()
	{
		boolean finished = true;

		for (Laser s: this.segments)
		{
			s.age += Panel.frameFrequency;

			if (s.age > s.maxAge)
				s.expired = true;

			if (!s.expired)
				finished = false;
		}

		if (finished)
			Game.removeMovables.add(this);
	}

	@Override
	public void draw()
	{
		for (Laser s: this.segments)
		{
			s.draw();
		}
	}

	public void superUpdate()
	{
		super.update();
	}

	public void addTrail(boolean redirect)
	{

	}
}
