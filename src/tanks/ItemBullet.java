package tanks;

public class ItemBullet extends Item
{
	public Bullet.BulletEffect effect;
	public double speed;
	public int bounces;
	public double damage;
	public int maxAmount;
	public double cooldown;
	public double size;
	
	public int amount;
	public int liveBullets;
	
	@Override
	public void use()
	{
		Bullet b = new Bullet(Game.player.posX, Game.player.posY, bounces, Game.player, false);
		b.damage = this.damage;
		b.effect = this.effect;
		b.size = this.size;
		
		Game.player.fireBullet(b, speed);
	}

	@Override
	public boolean usable()
	{
		if (this.liveBullets >= this.maxAmount)
			return false;
		
		return true;
	}
	
	
}
