package tanks.bullet;

import tanks.Game;
import tanks.event.EventBulletDestroyed;
import tanks.event.EventBulletInstantWaypoint;
import tanks.gui.screen.ScreenGame;
import tanks.hotbar.ItemBullet;
import tanks.tank.Ray;
import tanks.tank.Tank;

import java.util.ArrayList;

public abstract class BulletInstant extends Bullet
{
	public ArrayList<Double> xTargets = new ArrayList<Double>();
	public ArrayList<Double> yTargets = new ArrayList<Double>();

	public BulletInstant(double x, double y, int bounces, Tank t, boolean affectsMaxLiveBullets, ItemBullet ib)
	{
		super(x, y, bounces, t, affectsMaxLiveBullets, ib);
		this.enableExternalCollisions = false;
		this.playPopSound = false;
	}

	public void saveTarget()
	{
		this.xTargets.add(this.posX);
		this.yTargets.add(this.posY);
	}

	public abstract void addEffect();

	public abstract void addDestroyEffect();

	public void shoot()
	{
		if (!this.tank.isRemote)
		{
			Ray r = new Ray(this.posX, this.posY, 0, this.bounces, this.tank);
			r.vX = this.vX;
			r.vY = this.vY;
			r.getTarget();
			this.saveTarget();
			this.xTargets.addAll(r.bounceX);
			this.yTargets.addAll(r.bounceY);
		}

		while (!this.destroy)
		{
			if (ScreenGame.finished)
				this.destroy = true;

			super.update();
			this.addEffect();
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
	}

	public void remoteShoot()
	{
		for (int i = 0; i < xTargets.size() - 1; i++)
		{
			double iX = xTargets.get(i);
			double iY = yTargets.get(i);
			double dX = xTargets.get(i + 1) - iX;
			double dY = yTargets.get(i + 1) - iY;

			int steps = (int) (Math.sqrt((Math.pow(dX, 2) + Math.pow(dY, 2)) / (1 + Math.pow(this.vX, 2) + Math.pow(this.vY, 2))) + 1);
			for (int s = 0; s <= steps; s++)
			{
				this.posX = iX + dX * s / steps;
				this.posY = iY + dY * s / steps;

				this.age++;
				double frac = 1 / (1 + this.age / 100);
				this.posZ = this.iPosZ * frac + (Game.tile_size / 4) * (1 - frac);

				this.addEffect();
			}
		}

		this.addDestroyEffect();
	}
}
