package tanks.hotbar.item;

import tanks.AttributeModifier;
import tanks.Game;
import tanks.Panel;
import tanks.Player;
import tanks.bullet.*;
import tanks.bullet.legacy.BulletAir;
import tanks.bullet.legacy.BulletFlame;
import tanks.gui.property.UIPropertyBoolean;
import tanks.gui.property.UIPropertyDouble;
import tanks.gui.property.UIPropertyInt;
import tanks.gui.property.UIPropertySelector;
import tanks.minigames.Minigame;
import tanks.tank.Tank;

import java.util.HashMap;

public class ItemBullet extends Item
{
	public static final String item_name = "bullet";

	public Bullet.BulletEffect effect = Bullet.BulletEffect.none;
	public double speed = 25.0 / 8;
	public int bounces = 1;
	public double damage = 1;
	public int maxLiveBullets = 5;
	public double cooldownBase = 20;
	public double size = Bullet.bullet_size;
	public double recoil = 1.0;
	public boolean heavy = false;
	public double accuracy = 0;
	public int shotCount = 1;
	public double shotSpread = 30;

	public double fractionUsed = 0;
	public int liveBullets;
	public String className;

	public Class<? extends Bullet> bulletClass = Bullet.class;

	public static HashMap<String, Bullet.BulletEffect> effectsMap1 = new HashMap<>();
	public static HashMap<Bullet.BulletEffect, String> effectsMap2 = new HashMap<>();

	public static HashMap<String, Class<? extends Bullet>> classMap1 = new HashMap<>();
	public static HashMap<Class<? extends Bullet>, String> classMap2 = new HashMap<>();

	public ItemBullet(Player p)
	{
		super(p);
		this.rightClick = false;
		this.isConsumable = true;

		new UIPropertySelector(this.properties, "type", Game.registryBullet.getEntryNames(), Game.registryBullet.getImageNames(), 0);
		new UIPropertySelector(this.properties, "effect", new String[]{"none", "trail", "fire", "fire_and_smoke", "dark_fire", "ice", "ember"},
				new String[]{"bullet_large.png", "bullet_normal.png", "bullet_fire.png", "bullet_fire_trail.png", "bullet_dark_fire.png", "bullet_freeze.png", "bullet_boost.png"}, 0);
		new UIPropertyDouble(this.properties, "speed", 3.125);
		new UIPropertyInt(this.properties, "bounces", 1);
		new UIPropertyDouble(this.properties, "damage", 1.0);
		new UIPropertyInt(this.properties, "max_live_bullets", 5);
		new UIPropertyDouble(this.properties, "cooldown", 20.0);
		new UIPropertyDouble(this.properties, "size", 10.0);
		new UIPropertyDouble(this.properties, "recoil", 1.0);
		new UIPropertyBoolean(this.properties, "heavy", false);
		new UIPropertyDouble(this.properties, "accuracy_spread_angle", 0.0);
		new UIPropertyInt(this.properties, "shot_count", 1);
		new UIPropertyDouble(this.properties, "multishot_spread_angle", 30.0);

		this.supportsHits = true;
	}

	public ItemBullet()
	{
		this(null);
	}

	@Override
	public void use(Tank m)
	{
		try
		{
			double remainingQty = this.stackSize - this.fractionUsed;
			double useAmt = 1;

			if (this.unlimitedStack)
				remainingQty = Double.MAX_VALUE;

			if (this.cooldownBase <= 0)
				useAmt = Panel.frameFrequency;

			int q = (int) Math.min(this.shotCount, Math.ceil(remainingQty / useAmt));

			double speedmul = m.getAttributeValue(AttributeModifier.bullet_speed, 1);

			for (int i = 0; i < q; i++)
			{
				double baseOff = 0;

				if (q > 1)
				{
					if (shotSpread >= 360)
						baseOff = Math.PI * 2 * i / q;
					else
						baseOff = Math.toRadians(this.shotSpread) * ((i * 1.0 / (q - 1)) - 0.5);
				}

				Bullet b = bulletClass.getConstructor(double.class, double.class, int.class, Tank.class, ItemBullet.class).newInstance(m.posX, m.posY, bounces, m, this);

				b.damage = this.damage;
				b.effect = this.effect;
				b.size = this.size;
				b.heavy = heavy;
				b.recoil = recoil;

				if (this.cooldownBase <= 0)
				{
					b.frameDamageMultipler = Panel.frameFrequency;
					this.fractionUsed += Panel.frameFrequency;
				}
				else
					this.fractionUsed++;

				this.setOtherItemsCooldown();
				this.cooldown = this.cooldownBase;

				double off = baseOff + (Math.random() - 0.5) * Math.toRadians(this.accuracy);
				m.fireBullet(b, speed * speedmul, off);

				if (Game.currentLevel instanceof Minigame)
				{
					((Minigame) Game.currentLevel).onBulletFire(b);
				}

				while (this.fractionUsed >= 1 && !this.unlimitedStack)
				{
					this.stackSize--;
					this.fractionUsed--;
				}

				if (this.stackSize <= 0)
					this.destroy = true;
			}
		}
		catch (Exception e)
		{
			Game.exitToCrash(e);
		}
	}

	@Override
	public boolean usable(Tank t)
	{
		return t != null && (this.maxLiveBullets <= 0 || this.liveBullets <= this.maxLiveBullets - this.shotCount) && !(this.cooldown > 0) && this.stackSize > 0;
	}

	@Override
	public String convertToString()
	{
		return super.convertToString() + "," + item_name + ","
				+ className + "," + effectsMap2.get(effect) + "," + speed + "," + bounces + "," + damage + "," + maxLiveBullets + "," + cooldownBase + "," + size + "," + recoil + "," + heavy +
				"," + this.accuracy + "," + this.shotCount + "," + this.shotSpread;
	}

	@Override
	public void fromString(String s)
	{
		String[] p = s.split(",");

		this.bulletClass = classMap1.get(p[0]);
		this.className = p[0];
		this.effect = effectsMap1.get(p[1]);

		this.speed = Double.parseDouble(p[2]);
		this.bounces = Integer.parseInt(p[3]);
		this.damage = Double.parseDouble(p[4]);
		this.maxLiveBullets = Integer.parseInt(p[5]);
		this.cooldownBase = Double.parseDouble(p[6]);
		this.size = Double.parseDouble(p[7]);
		this.recoil = Double.parseDouble(p[8]);
		this.heavy = Boolean.parseBoolean(p[9]);

		if (p.length > 10)
		{
			this.accuracy = Double.parseDouble(p[10]);
			this.shotCount = Integer.parseInt(p[11]);
			this.shotSpread = Double.parseDouble(p[12]);
		}
	}

	@Override
	public void importProperties()
	{
		super.importProperties();

		this.setProperty("type", this.className);
		this.setProperty("effect", effectsMap2.get(this.effect));
		this.setProperty("speed", this.speed);
		this.setProperty("bounces", this.bounces);
		this.setProperty("damage", this.damage);
		this.setProperty("max_live_bullets", this.maxLiveBullets);
		this.setProperty("cooldown", this.cooldownBase);
		this.setProperty("size", this.size);
		this.setProperty("recoil", this.recoil);
		this.setProperty("heavy", this.heavy);
		this.setProperty("accuracy_spread_angle", this.accuracy);
		this.setProperty("shot_count", this.shotCount);
		this.setProperty("multishot_spread_angle", this.shotSpread);
	}

	@Override
	public String getTypeName()
	{
		return "Bullet";
	}

	@Override
	public void exportProperties()
	{
		super.exportProperties();

		this.className = (String) this.getProperty("type");
		this.bulletClass = classMap1.get(this.className);
		this.effect = effectsMap1.get(this.getProperty("effect"));
		this.speed = (double) this.getProperty("speed");
		this.bounces = (int) this.getProperty("bounces");
		this.damage = (double) this.getProperty("damage");
		this.maxLiveBullets = (int) this.getProperty("max_live_bullets");
		this.cooldownBase = (double) this.getProperty("cooldown");
		this.size = (double) this.getProperty("size");
		this.recoil = (double) this.getProperty("recoil");
		this.heavy = (boolean) this.getProperty("heavy");
		this.accuracy = (double) this.getProperty("accuracy_spread_angle");
		this.shotCount = (int) this.getProperty("shot_count");
		this.shotSpread = (double) this.getProperty("multishot_spread_angle");
	}

	//TODO change
	public double getRange()
	{
		if (BulletArc.class.isAssignableFrom(this.bulletClass))
			return this.speed / 3.125 * 1000.0;
		else if (BulletFlame.class.isAssignableFrom(this.bulletClass))
			return 400;
		else if (BulletAir.class.isAssignableFrom(this.bulletClass))
			return 800;
		else
			return -1;
	}

	public static void initializeMaps()
	{
		addToMaps(effectsMap1, effectsMap2, "none", Bullet.BulletEffect.none);
		addToMaps(effectsMap1, effectsMap2, "trail", Bullet.BulletEffect.trail);
		addToMaps(effectsMap1, effectsMap2, "fire", Bullet.BulletEffect.fire);
		addToMaps(effectsMap1, effectsMap2, "fire_and_smoke", Bullet.BulletEffect.fireTrail);
		addToMaps(effectsMap1, effectsMap2, "dark_fire", Bullet.BulletEffect.darkFire);
		addToMaps(effectsMap1, effectsMap2, "ice", Bullet.BulletEffect.ice);
		addToMaps(effectsMap1, effectsMap2, "ember", Bullet.BulletEffect.ember);
	}

	public static <X, Y> void addToMaps(HashMap<X, Y> map1, HashMap<Y, X> map2, X a, Y b)
	{
		map1.put(a, b);
		map2.put(b, a);
	}
}
