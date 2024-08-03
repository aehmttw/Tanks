package tanks.bullet.legacy2;

import tanks.bullet.Bullet;
import tanks.item.legacy.ItemBullet;
import tanks.tank.Tank;

public class BulletBoost2 extends Bullet
{
	public static String bullet_name = "boost";

	public BulletBoost2(double x, double y, int bounces, Tank t, ItemBullet ib)
	{
		this(x, y, bounces, t, true, ib);
	}

	public BulletBoost2(double x, double y, int bounces, Tank t, boolean affectsLiveBulletCount, ItemBullet ib)
	{
		super(x, y, bounces, t, affectsLiveBulletCount, ib);
		this.overrideOutlineColor = true;
		this.outlineColorR = 255;
		this.outlineColorG = 180;
		this.outlineColorB = 0;
		this.name = bullet_name;

		// TODO: a way to calculate these?
		this.damage = 0;
		this.shouldDodge = false;
		this.dealsDamage = false;

		this.boosting = true;
	}
}
