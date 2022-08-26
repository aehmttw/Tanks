package tanks.bullet;

import tanks.Effect;
import tanks.Game;
import tanks.Movable;
import tanks.hotbar.item.ItemBullet;
import tanks.tank.Tank;

/**
 * A laser which can be fired by a Tank.
 * @see tanks.tank.TankRed
 */
public class BulletLaser extends BulletInstant
{
	public static String bullet_name = "laser";

	public BulletLaser(double x, double y, int bounces, Tank t, boolean affectsMaxLiveBullets, ItemBullet ib)
	{
		super(x, y, bounces, t, affectsMaxLiveBullets, ib);
		this.playPopSound = false;
		this.baseColorR = 255;
		this.baseColorG = 0;
		this.baseColorB = 0;
		this.name = bullet_name;
		this.effect = BulletEffect.none;
		this.itemSound = "laser.ogg";
	}

	public BulletLaser(double x, double y, int bounces, Tank t, ItemBullet ib)
	{
		this(x, y, bounces, t, false, ib);
	}

	@Override
	public void update()
	{
		if (!this.expired)
			this.shoot();

		super.update();
	}

	@Override
	public void collidedWithObject(Movable m)
	{
		this.playPopSound = true;
		super.collidedWithObject(m);
		this.playPopSound = false;
	}

	@Override
	public void addDestroyEffect()
	{
		if (Game.effectsEnabled)
		{
			for (int i = 0; i < this.size * 4 * Game.effectMultiplier; i++)
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
}
