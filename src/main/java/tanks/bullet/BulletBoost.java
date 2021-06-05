package tanks.bullet;

import tanks.*;
import tanks.hotbar.item.ItemBullet;
import tanks.tank.Tank;

public class BulletBoost extends Bullet
{
	public static String bullet_name = "boost";

	public BulletBoost(double x, double y, int bounces, Tank t)
	{
		this(x, y, bounces, t, true, null);
	}

	public BulletBoost(double x, double y, int bounces, Tank t, boolean affectsLiveBulletCount, ItemBullet ib)
	{
		super(x, y, bounces, t, affectsLiveBulletCount, ib);
		this.outlineColorR = 255;
		this.outlineColorG = 180;
		this.outlineColorB = 0;
		this.damage = 0;
		this.name = bullet_name;
		this.shouldDodge = false;
		this.dealsDamage = false;

		this.playPopSound = false;
	}

	/** Do not use, instead use the constructor with primitive data types. Intended for Item use only!*/
	@Deprecated
	public BulletBoost(Double x, Double y, Integer bounces, Tank t, ItemBullet ib)
	{
		this(x.doubleValue(), y.doubleValue(), bounces.intValue(), t, true, ib);
	}

	@Override
	public void collidedWithTank(Tank t)
	{
		this.destroy = true;

		AttributeModifier c = new AttributeModifier("boost_speed", "velocity", AttributeModifier.Operation.multiply, 3);
		c.duration = 10 * this.size;
		c.deteriorationAge = 5 * this.size;
		t.addUnduplicateAttribute(c);

		AttributeModifier e = new AttributeModifier("bullet_boost", "bullet_boost", AttributeModifier.Operation.multiply, 1);
		e.duration = 10 * this.size;
		e.deteriorationAge = 5 * this.size;
		t.addUnduplicateAttribute(e);

		AttributeModifier a = new AttributeModifier("boost_glow", "glow", AttributeModifier.Operation.multiply, 1);
		a.duration = 10 * this.size;
		a.deteriorationAge = 5 * this.size;
		t.addUnduplicateAttribute(a);

		AttributeModifier b = new AttributeModifier("boost_slip", "friction", AttributeModifier.Operation.multiply, -0.75);
		b.duration = 10 * this.size;
		b.deteriorationAge = 5 * this.size;
		t.addUnduplicateAttribute(b);

		AttributeModifier d = new AttributeModifier("boost_effect", "effect", AttributeModifier.Operation.add, 1);
		d.duration = 10 * this.size;
		d.deteriorationAge = 5 * this.size;
		t.addUnduplicateAttribute(d);
	}

	@Override
	public void draw()
	{
		if (Game.glowEnabled)
		{
			double frac = Math.min(1, this.destroyTimer / 60);
			Drawing.drawing.setColor(255, 180, 0, 180 * frac, 1);

			if (Game.enable3d)
				Drawing.drawing.fillGlow(this.posX, this.posY, this.posZ, this.size * 8, this.size * 8);
			else
				Drawing.drawing.fillGlow(this.posX, this.posY, this.size * 8, this.size * 8);
		}

		super.draw();
	}

	@Override
	public void onDestroy()
	{
		if (Game.playerTank != null && !Game.playerTank.destroy)
		{
			double distsq = Math.pow(this.posX - Game.playerTank.posX, 2) + Math.pow(this.posY - Game.playerTank.posY, 2);

			double radius = 250000;
			if (distsq <= radius)
			{
				Drawing.drawing.playSound("boost.ogg", (float) (10.0 / this.size), (float) ((radius - distsq) / radius));
			}
		}

		if (Game.effectsEnabled)
		{
			for (int i = 0; i < 25 * Game.effectMultiplier; i++)
			{
				Effect e = Effect.createNewEffect(this.posX, this.posY, Game.tile_size / 2, Effect.EffectType.piece);
				double var = 50;

				e.colR = Math.min(255, Math.max(0, this.outlineColorR + Math.random() * var - var / 2));
				e.colG = Math.min(255, Math.max(0, this.outlineColorG + Math.random() * var - var / 2));
				e.colB = Math.min(255, Math.max(0, this.outlineColorB + Math.random() * var - var / 2));

				if (Game.enable3d)
					e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.random() * Math.PI, Math.random() + 0.5);
				else
					e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() + 0.5);

				Game.effects.add(e);
			}
		}
	}
}
