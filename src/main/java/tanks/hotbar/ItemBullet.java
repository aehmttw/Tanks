package tanks.hotbar;

import tanks.Game;
import tanks.bullet.Bullet;
import tanks.tank.Tank;
import tanks.tank.TankPlayer;

public class ItemBullet extends Item
{
	public Bullet.BulletEffect effect = Bullet.BulletEffect.none;
	public double speed = 25.0 / 4;
	public int bounces = 1;
	public double damage = 1;
	public int maxAmount = 5;
	public double cooldown = 20;
	public double size = Bullet.bullet_size;
	public double recoil = 1.0;
	public boolean heavy = false;

	public int liveBullets;

	public String name;
	
	public String className;

	public Class<? extends Bullet> bulletClass = Bullet.class;

	public ItemBullet()
	{
		this.rightClick = false;
		this.isConsumable = true;
	}

	@Override
	public void use()
	{
		try
		{
			Bullet b = bulletClass.getConstructor(Double.class, Double.class, Integer.class, Tank.class, ItemBullet.class).newInstance(Game.player.posX, Game.player.posY, bounces, Game.player, this);

			b.damage = this.damage;
			b.effect = this.effect;
			b.size = this.size;
			b.heavy = heavy;
			b.recoil = recoil;
			
			Game.player.cooldown = this.cooldown;

			if (Game.player instanceof TankPlayer)
				((TankPlayer)Game.player).fireBullet(b, speed);

			this.stackSize--;
			
			if (this.stackSize <= 0)
				this.destroy = true;			
		}
		catch (Exception e)
		{
			Game.exitToCrash(e);
		}
	}

	@Override
	public boolean usable()
	{
		return (this.maxAmount <= 0 || this.liveBullets < this.maxAmount) && !(Game.player.cooldown > 0) && this.stackSize > 0;
	}

	@Override
	public String toString()
	{
		return super.toString() + ",bullet,"
				+ className + "," + effect + "," + speed + "," + bounces + "," + damage + "," + maxAmount + "," + cooldown + "," + size + "," + recoil + "," + heavy;
	}
}
