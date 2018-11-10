package tanks.item;

import tanks.Bullet;
import tanks.Game;

public class ItemBullet extends Item
{
	public Bullet.BulletEffect effect = Bullet.BulletEffect.none;
	public double speed = 25.0 / 4;
	public int bounces = 1;
	public double damage = 1;
	public int maxAmount = 5;
	public double cooldown = 20;
	public double size = Bullet.bullet_size;
	
	public int amount;
	public int liveBullets;
	
	public String name;
	
	public ItemBullet()
	{
		this.isConsumable = true;
	}
	
	@Override
	public void use()
	{
		Bullet b = new Bullet(Game.player.posX, Game.player.posY, bounces, Game.player, false);
		b.damage = this.damage;
		b.effect = this.effect;
		b.size = this.size;
		
		Game.player.cooldown = this.cooldown;
		Game.player.fireBullet(b, speed);
		
		this.stackSize--;
	}

	@Override
	public boolean usable()
	{
		if (this.liveBullets >= this.maxAmount || Game.player.cooldown > 0 || this.stackSize <= 0)
			return false;
		
		return true;
	}
	
	
}
