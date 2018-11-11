package tanks.item;

import java.lang.reflect.Constructor;

import lombok.SneakyThrows;
import tanks.Bullet;
import tanks.tank.Tank;

public class ItemBullet extends Item {
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
	
	private final Constructor<? extends Bullet> bulletConstructor;
	
	@SneakyThrows
	public ItemBullet(Class<? extends Bullet> bulletClass) {
		this.bulletConstructor = bulletClass.getDeclaredConstructor(double.class, double.class, int.class, Tank.class);
	}
	
	@SneakyThrows
	public Bullet createBullet(double x, double y, int bounces, Tank tank) {
		return bulletConstructor.newInstance(x, y, bounces, tank);
	}
	
//	@Override
//	public void use()
//	{
//		Bullet b = new Bullet(Game.player.posX, Game.player.posY, bounces, Game.player, false);
//		b.damage = this.damage;
//		b.effect = this.effect;
//		b.size = this.size;
//		
//		Game.player.cooldown = this.cooldown;
//		Game.player.fireBullet(b, speed);
//		
//		this.stackSize--;
//	}
//
//	@Override
//	public boolean usable()
//	{
//		if (this.liveBullets >= this.maxAmount || Game.player.cooldown > 0 || this.stackSize <= 0)
//			return false;
//		
//		return true;
//	}
	
	
}
