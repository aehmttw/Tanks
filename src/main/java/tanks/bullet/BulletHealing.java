package tanks.bullet;

import tanks.*;
import tanks.AttributeModifier.Operation;
import tanks.hotbar.item.ItemBullet;
import tanks.network.event.EventTankUpdateHealth;
import tanks.tank.Tank;

public class BulletHealing extends BulletInstant
{
	public static String bullet_name = "healing";

	public boolean hitTank = false;

	public BulletHealing(double x, double y, int bounces, Tank t, boolean affectsMaxLiveBullets, ItemBullet ib)
	{
		super(x, y, bounces, t, affectsMaxLiveBullets, ib);
		this.playPopSound = false;

		this.overrideBaseColor = true;
		this.baseColorR = 0;
		this.baseColorG = 255;
		this.baseColorB = 0;

		this.overrideOutlineColor = true;
		this.outlineColorR = 200;
		this.outlineColorG = 255;
		this.outlineColorB = 200;

		this.name = bullet_name;
		this.effect = BulletEffect.none;
		this.damage = -0.01;
		this.shouldDodge = false;
		this.dealsDamage = false;
		this.bulletCollision = false;

		this.itemSound = null;
	}

	public BulletHealing(double x, double y, int bounces, Tank t, ItemBullet ib)
	{
		this(x, y, bounces, t, false, ib);
	}
}
