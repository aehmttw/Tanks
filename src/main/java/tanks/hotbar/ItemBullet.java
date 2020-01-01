package tanks.hotbar;

import tanks.Game;
import tanks.Movable;
import tanks.Player;
import tanks.bullet.Bullet;
import tanks.tank.Tank;
import tanks.tank.TankPlayer;
import tanks.tank.TankPlayerRemote;

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

	public Player player;

	public int liveBullets;

	public String name;
	
	public String className;

	public Class<? extends Bullet> bulletClass = Bullet.class;

	public ItemBullet(Player p)
	{
		this.player = p;
		this.rightClick = false;
		this.isConsumable = true;
	}

	@Override
	public void use()
	{
		try
		{
			if (this.player == Game.player)
			{
				Bullet b = bulletClass.getConstructor(Double.class, Double.class, Integer.class, Tank.class, ItemBullet.class).newInstance(Game.playerTank.posX, Game.playerTank.posY, bounces, Game.playerTank, this);

				b.damage = this.damage;
				b.effect = this.effect;
				b.size = this.size;
				b.heavy = heavy;
				b.recoil = recoil;

				Game.playerTank.cooldown = this.cooldown;

				if (Game.playerTank instanceof TankPlayer)
					((TankPlayer) Game.playerTank).fireBullet(b, speed);
			}
			else
			{
				for (int i = 0; i < Game.movables.size(); i++)
				{
					Movable m = Game.movables.get(i);
					if (m instanceof TankPlayerRemote && ((TankPlayerRemote) m).player == this.player)
					{
						Bullet b = bulletClass.getConstructor(Double.class, Double.class, Integer.class, Tank.class, ItemBullet.class).newInstance(m.posX, m.posY, bounces, (Tank) m, this);

						b.damage = this.damage;
						b.effect = this.effect;
						b.size = this.size;
						b.heavy = heavy;
						b.recoil = recoil;

						m.cooldown = this.cooldown;
						((TankPlayerRemote) m).fireBullet(b, speed);
					}
				}
			}

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
		return (this.maxAmount <= 0 || this.liveBullets < this.maxAmount) && !(Game.playerTank.cooldown > 0) && this.stackSize > 0;
	}

	@Override
	public String toString()
	{
		return super.toString() + ",bullet,"
				+ className + "," + effect + "," + speed + "," + bounces + "," + damage + "," + maxAmount + "," + cooldown + "," + size + "," + recoil + "," + heavy;
	}
}
