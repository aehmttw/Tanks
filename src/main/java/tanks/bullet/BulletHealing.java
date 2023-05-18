package tanks.bullet;

import tanks.*;
import tanks.AttributeModifier.Operation;
import tanks.network.event.EventTankUpdateHealth;
import tanks.hotbar.item.ItemBullet;
import tanks.tank.Tank;

public class BulletHealing extends BulletInstant
{
	public static String bullet_name = "healing";

	public boolean hitTank = false;

	public BulletHealing(double x, double y, int bounces, Tank t, boolean affectsMaxLiveBullets, ItemBullet ib)
	{
		super(x, y, bounces, t, affectsMaxLiveBullets, ib);
		this.playPopSound = false;
		this.baseColorR = 0;
		this.baseColorG = 255;
		this.baseColorB = 0;
		this.name = bullet_name;
		this.effect = BulletEffect.none;
		this.damage = 0.01;
		this.shouldDodge = false;
		this.dealsDamage = false;

		this.itemSound = null;
		// this.itemSound = "heal.ogg";
	}

	public BulletHealing(double x, double y, int bounces, Tank t, ItemBullet ib)
	{
		this(x, y, bounces, t, false, ib);
	}

	@Override
	public void update()
	{
		if (!this.expired)
		{
			this.shoot();

			float freq = (float) (Panel.frameFrequency / 10);

			if (Game.game.window.touchscreen)
				freq = 1;

			if (!hitTank)
				Drawing.drawing.playGlobalSound("heal1.ogg", 1f, freq / 2);
		}

		super.update();
	}

	@Override
	public void collidedWithTank(Tank t)
	{
		if (!heavy)
			this.destroy = true;

		hitTank = true;

		float freq = (float) (Panel.frameFrequency / 10);

		if (Game.game.window.touchscreen)
			freq = 1;

		double before = t.health;

		if (t.health < t.baseHealth + 1)
			t.health = Math.min(t.baseHealth + 1, t.health + this.damage * this.frameDamageMultipler);

		t.checkHit(this.tank, this);

		Drawing.drawing.playGlobalSound("heal2.ogg", (float) ((Math.min(t.health, t.baseHealth + 1) / (t.baseHealth + 1) / 2) + 1f) / 2, freq / 2);

		Game.eventsOut.add(new EventTankUpdateHealth(t));

		t.addAttribute(new AttributeModifier("healray", AttributeModifier.healray, Operation.add, 1.0));

		if (t.health > 6 && (int) (before) != (int) (t.health))
		{
			Effect e = Effect.createNewEffect(t.posX, t.posY, t.posZ + t.size * 0.75, Effect.EffectType.shield);
			e.size = t.size;
			e.radius = t.health - 1;
			Game.effects.add(e);
		}
	}

	@Override
	public void collidedWithObject(Movable o)
	{

	}
}
