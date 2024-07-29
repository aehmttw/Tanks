package tanks.bullet;

import tanks.AreaEffectFreeze;
import tanks.Drawing;
import tanks.Game;
import tanks.hotbar.item.ItemBullet;
import tanks.tank.Tank;

public class BulletFreeze2 extends Bullet
{
	public static String bullet_name = "freezing";

	public BulletFreeze2(double x, double y, int bounces, Tank t, ItemBullet ib)
	{
		this(x, y, bounces, t, true, ib);
	}

	public BulletFreeze2(double x, double y, int bounces, Tank t, boolean affectsLiveBulletCount, ItemBullet ib)
	{
		super(x, y, bounces, t, affectsLiveBulletCount, ib);
		this.outlineColorR = 255;
		this.outlineColorG = 255;
		this.outlineColorB = 255;
		this.freezing = true;
		this.name = bullet_name;
	}
}
