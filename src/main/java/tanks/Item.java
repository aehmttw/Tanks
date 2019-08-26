package tanks;

import tanks.bullets.Bullet;
import tanks.bullets.BulletElectric;
import tanks.bullets.BulletFlame;
import tanks.bullets.BulletFreeze;
import tanks.bullets.BulletLaser;

public abstract class Item 
{
	public boolean isConsumable;
	public int levelUnlock;
	public int price;
	public int maxStackSize = 100;
	public int stackSize = 1;
	public boolean inUse = false;
	public String name;
	public String icon;
	
	public boolean destroy = false;
	
	public boolean rightClick;

	public abstract boolean usable();
	
	public abstract void use();
	
	/**name-image-price-level-quantity-max_quantity-type
	 * <br>if (type == bullet):-class-effect-speed-bounces-damage-max_on_screen-cooldown-size*/
	public static Item parseItem(String s)
	{
		String[] p = s.split("-");
		String name = p[0];
		String image = p[1];
		int price = Integer.parseInt(p[2]);
		int level = Integer.parseInt(p[3]);
		int quantity = Integer.parseInt(p[4]);
		int maxStack = Integer.parseInt(p[5]);
		
		Item i = new ItemBullet();
		
		if (p[6].equals("bullet"))
		{
			ItemBullet i2 = new ItemBullet();
			
			if (p[7].equals("normal"))
				i2.bulletClass = Bullet.class;
			else if (p[7].equals("flame"))
				i2.bulletClass = BulletFlame.class;
			else if (p[7].equals("laser"))
				i2.bulletClass = BulletLaser.class;
			else if (p[7].equals("freeze"))
				i2.bulletClass = BulletFreeze.class;
			else if (p[7].equals("electric"))
				i2.bulletClass = BulletElectric.class;
			
			i2.className = p[7];
			
			if (p[8].equals("none"))
				i2.effect = Bullet.BulletEffect.none;
			else if (p[8].equals("trail"))
				i2.effect = Bullet.BulletEffect.trail;
			else if (p[8].equals("fire"))
				i2.effect = Bullet.BulletEffect.fire;
			else if (p[8].equals("fireTrail"))
				i2.effect = Bullet.BulletEffect.fireTrail;
			else if (p[8].equals("darkFire"))
				i2.effect = Bullet.BulletEffect.darkFire;
			else if (p[8].equals("ice"))
				i2.effect = Bullet.BulletEffect.ice;
			
			
			i2.speed = Double.parseDouble(p[9]);
			i2.bounces = Integer.parseInt(p[10]);
			i2.damage = Double.parseDouble(p[11]);
			i2.maxAmount = Integer.parseInt(p[12]);
			i2.cooldown = Double.parseDouble(p[13]);
			i2.size = Double.parseDouble(p[14]);
			i2.recoil = Double.parseDouble(p[15]);
			i2.heavy = Boolean.parseBoolean(p[16]);

			i = i2;
		}
		
		i.name = name;
		i.icon = image;
		i.price = price;
		i.levelUnlock = level;
		i.stackSize = quantity;
		i.maxStackSize = maxStack;
		
		return i;
	}
	
	@Override
	public String toString()
	{
		return name + "-" + icon + "-" + price + "-" + levelUnlock + "-" + stackSize + "-" + maxStackSize;
	}

	public void attemptUse()
	{
		if (this.usable())
			use();
	}
}
