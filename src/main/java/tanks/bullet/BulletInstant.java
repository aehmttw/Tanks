package tanks.bullet;

import tanks.*;
import tanks.gui.screen.ScreenGame;
import tanks.item.ItemBullet;
import tanks.network.event.EventBulletDestroyed;
import tanks.network.event.EventBulletInstantWaypoint;
import tanks.tank.Tank;

import java.util.ArrayList;

public class BulletInstant extends Bullet
{
	public static String bullet_class_name = "laser";

	public ArrayList<Double> xTargets = new ArrayList<>();
	public ArrayList<Double> yTargets = new ArrayList<>();

	public ArrayList<Laser> segments = new ArrayList<>();

	public double lastX;
	public double lastY;
	public double lastZ;

	public boolean expired = false;

	public BulletInstant()
	{
		this.init();
	}

	public BulletInstant(double x, double y, Tank t, boolean affectsMaxLiveBullets, ItemBullet.ItemStackBullet ib)
	{
		super(x, y, t, affectsMaxLiveBullets, ib);
		this.init();
	}

	public void init()
	{
		this.typeName = bullet_class_name;
		this.enableExternalCollisions = false;
		this.playPopSound = false;
		this.playBounceSound = false;
		this.effect = BulletEffect.none;
		this.homingSilent = true;
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
			if (this.item.item.cooldownBase <= 0)
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

		if (!this.tank.isRemote)
		{
			this.collisionX = this.posX;
			this.collisionY = this.posY;
			this.lastX = this.posX;
			this.lastY = this.posY;
			this.lastZ = this.iPosZ;
			this.saveTarget();
		}

		this.affectedByFrameFrequency = false;
		double angle = this.getPolarDirection();
		int redirects = 0;
		while (!this.destroy)
		{
			if (ScreenGame.finished)
				this.destroy = true;

			if (Movable.absoluteAngleBetween(this.getPolarDirection(), angle) >= 0.1)
			{
				redirects++;
				this.collisionX = this.posX;
				this.collisionY = this.posY;
				this.collided();

				if (redirects > 100)
					this.homingSharpness = this.homingSharpness * 0.95;

				angle = this.getPolarDirection();
			}

			super.update();

			if (Math.abs(this.lastFinalVX) < 0.01 && Math.abs(this.lastFinalVY) < 0.01)
				this.destroy = true;
		}
		this.affectedByFrameFrequency = true;

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

		if (this.affectsMaxLiveBullets && this.reboundSuccessor == null && !this.failedRebound)
			this.item.liveBullets--;

		if (!this.isRemote)
			this.onDestroy();

		this.addDestroyEffect();
		this.expired = true;
	}

	@Override
	public void collided()
	{
		if (this.hitStun > 0)
			this.addElectricEffect();

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

	public void addElectricEffect()
	{
		double dist = Math.sqrt(Math.pow(this.collisionX - this.lastX, 2) + Math.pow(this.collisionY - this.lastY, 2));

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
					Laser l = new Laser(lX, lY, lZ, nX, nY, nZ, this.size * size, this.getAngleInDirection(this.lastX, this.lastY), this.outlineColorR, this.outlineColorG, this.outlineColorB);
					l.glows = glows;
					this.segments.add(l);
					lX = nX;
					lY = nY;
					lZ = nZ;
				}
				Laser l = new Laser(lX, lY, lZ, this.collisionX, this.collisionY, this.posZ, this.size * size, this.getAngleInDirection(this.lastX, this.lastY), this.outlineColorR, this.outlineColorG, this.outlineColorB);
				l.glows = glows;
				this.segments.add(l);
			}
		}
	}

	@Override
	public void collidedWithNothing()
	{
		if (this.damage < 0)
		{
			if (this.item.item.cooldownBase > 0)
				Drawing.drawing.playGlobalSound("heal_impact_1.ogg");
			else
			{
				float freq = (float) (this.frameDamageMultipler / 10);
				if (Game.game.window.touchscreen)
					freq = 1;
				Drawing.drawing.playGlobalSound("heal1.ogg", 1, freq);
			}
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

			this.posX = iX + dX;
			this.posY = iY + dY;

			double z = Game.tile_size / 4;
			if (i == 0)
				z = this.iPosZ;

			this.segments.add(new Laser(iX, iY, z, iX + dX, iY + dY, Game.tile_size / 4, this.size / 2, this.getAngleInDirection(iX, iY), this.baseColorR, this.baseColorG, this.baseColorB));
			this.expired = true;

			if (this.hitStun > 0)
			{
				this.lastX = iX;
				this.lastY = iY;
				this.lastZ = z;
				this.collisionX = iX + dX;
				this.collisionY = iY + dY;
				this.posZ = Game.tile_size / 4;
				this.addElectricEffect();
			}
		}

		Game.movables.add(this);
		this.addDestroyEffect();
	}

	@Override
	public void update()
	{
		if (this.delay > 0)
		{
			this.delay -= Panel.frameFrequency;
			return;
		}

		if (!this.expired)
			this.shoot();

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
