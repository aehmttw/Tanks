package tanks.bullet.legacy2;

import tanks.bullet.BulletInstant;
import tanks.item.legacy.ItemBullet;
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

		this.overrideBaseColor = true;
		this.baseColorR = 255;
		this.baseColorG = 0;
		this.baseColorB = 0;

		this.overrideOutlineColor = true;
		this.outlineColorR = 255;
		this.outlineColorG = 200;
		this.outlineColorB = 200;

		this.name = bullet_name;
		this.effect = BulletEffect.none;
		this.itemSound = "laser.ogg";
	}

	public BulletLaser(double x, double y, int bounces, Tank t, ItemBullet ib)
	{
		this(x, y, bounces, t, false, ib);
	}
}
