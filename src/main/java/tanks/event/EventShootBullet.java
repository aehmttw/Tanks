package tanks.event;

import tanks.Game;
import tanks.bullets.Bullet;
import tanks.bullets.BulletElectric;
import tanks.bullets.BulletFlame;
import tanks.bullets.BulletFreeze;
import tanks.bullets.BulletHealing;
import tanks.bullets.BulletInstant;
import tanks.bullets.BulletLaser;
import tanks.tank.Tank;

public class EventShootBullet implements INetworkEvent
{
	public Bullet bullet;
	
	public EventShootBullet(Bullet b)
	{
		bullet = b;
	}
	
	public EventShootBullet(String s)
	{
		String[] parts = s.split(",");
	
		Tank t = Tank.idMap.get(Integer.parseInt(parts[0]));

		if (parts[5].equals("electric"))
			this.bullet = new BulletElectric(0, 0, 0, t);
		else if (parts[5].equals("flame"))
			this.bullet = new BulletFlame(0, 0, 0, t);
		else if (parts[5].equals("freeze"))
			this.bullet = new BulletFreeze(0, 0, 0, t);
		else if (parts[5].equals("heal"))
			this.bullet = new BulletHealing(0, 0, 0, t);
		else if (parts[5].equals("laser"))
			this.bullet = new BulletLaser(0, 0, 0, t);
		else
			this.bullet = new Bullet(0, 0, 0, t);
		
		this.bullet.posX = Double.parseDouble(parts[1]);
		this.bullet.posY = Double.parseDouble(parts[2]);
		this.bullet.vX = Double.parseDouble(parts[3]);
		this.bullet.vY = Double.parseDouble(parts[4]);
		this.bullet.name = parts[5];
				
		if (parts[6].equals("none"))
			this.bullet.effect = Bullet.BulletEffect.none;
		else if (parts[6].equals("trail"))
			this.bullet.effect = Bullet.BulletEffect.trail;
		else if (parts[6].equals("fire"))
			this.bullet.effect = Bullet.BulletEffect.fire;
		else if (parts[6].equals("fireTrail"))
			this.bullet.effect = Bullet.BulletEffect.fireTrail;
		else if (parts[6].equals("darkFire"))
			this.bullet.effect = Bullet.BulletEffect.darkFire;
		else if (parts[6].equals("ice"))
			this.bullet.effect = Bullet.BulletEffect.ice;
		
		this.bullet.bounces = Integer.parseInt(parts[7]);
		this.bullet.damage = Double.parseDouble(parts[8]);
		this.bullet.size = Double.parseDouble(parts[9]);
		this.bullet.heavy = Boolean.parseBoolean(parts[10]);
	}
	
	@Override
	public String getNetworkString()
	{
		return bullet.tank.networkID + "," + bullet.posX + "," + bullet.posY + "," + bullet.vX + "," + bullet.vY + 
				"," + bullet.name + "," + bullet.effect + "," + bullet.bounces + "," + bullet.damage + ","
				+ bullet.size + "," + bullet.heavy;
	}

	@Override
	public void execute() 
	{
		if (bullet instanceof BulletInstant)
			((BulletInstant) bullet).shotQueued = true;
		
		Game.movables.add(bullet);
	}
}
