package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.bullet.Bullet;
import tanks.bullet.BulletInstant;
import tanks.item.legacy.ItemBullet;
import tanks.network.NetworkUtils;
import tanks.tank.Tank;

public class EventShootBullet extends PersonalEvent
{
	public int id;
	public int tank;
	public double posX;
	public double posY;
	public double posZ;
	public double vX;
	public double vY;
	public double vZ;
	public String name;
	public String type;
	public String effect;
	public int bounces;
	public double damage;
	public double size;
	public boolean heavy;
	
	public EventShootBullet()
	{
		
	}
	
	public EventShootBullet(Bullet b)
	{
		this.id = b.networkID;
		this.tank = b.tank.networkID;
		this.posX = b.posX;
		this.posY = b.posY;
		this.posZ = b.posZ;
		this.vX = b.vX;
		this.vY = b.vY;
		this.vZ = b.vZ;
		this.name = b.name;
		this.effect = b.effect.name();
		this.bounces = b.bounces;
		this.damage = b.damage;
		this.size = b.size;
		this.heavy = b.heavy;
		this.type = b.effect.name();
	}

	@Override
	public void execute() 
	{
		if (this.clientID != null)
			return;

		Bullet bullet;
		
		Tank t = Tank.idMap.get(this.tank);

		if (t == null)
			return;

		try
		{
			bullet = Game.registryBullet.getEntry(this.name).bullet.getConstructor(double.class, double.class, int.class, Tank.class, ItemBullet.class).newInstance(0.0, 0.0, 0, t, t.bullet);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
		
		bullet.posX = this.posX;
		bullet.posY = this.posY;
		bullet.posZ = this.posZ;
		bullet.vX = this.vX;
		bullet.vY = this.vY;
		bullet.vZ = this.vZ;
		bullet.name = this.name;

		switch (this.type)
		{
			case "none":
				bullet.effect = Bullet.BulletEffect.none;
				break;
			case "trail":
				bullet.effect = Bullet.BulletEffect.trail;
				break;
			case "fire":
				bullet.effect = Bullet.BulletEffect.fire;
				break;
			case "fireTrail":
				bullet.effect = Bullet.BulletEffect.fireTrail;
				break;
			case "darkFire":
				bullet.effect = Bullet.BulletEffect.darkFire;
				break;
			case "ice":
				bullet.effect = Bullet.BulletEffect.ice;
				break;
			case "ember":
				bullet.effect = Bullet.BulletEffect.ember;
				break;
		}

		bullet.bounces = this.bounces;
		bullet.damage = this.damage;
		bullet.size = this.size;
		bullet.heavy = this.heavy;
		bullet.speed = Math.sqrt(this.vX * this.vX + this.vY * this.vY);

		bullet.networkID = this.id;
		Bullet.idMap.put(this.id, bullet);
		
		if (!(bullet instanceof BulletInstant))
			Game.movables.add(bullet);
	}

	@Override
	public void write(ByteBuf b) 
	{
		b.writeInt(this.id);
		b.writeInt(this.tank);
		b.writeDouble(this.posX);
		b.writeDouble(this.posY);
		b.writeDouble(this.posZ);
		b.writeDouble(this.vX);
		b.writeDouble(this.vY);
		b.writeDouble(this.vZ);
		NetworkUtils.writeString(b, this.name);
		NetworkUtils.writeString(b, this.type);
		NetworkUtils.writeString(b, this.effect);
		b.writeInt(this.bounces);
		b.writeDouble(this.damage);
		b.writeDouble(this.size);
		b.writeBoolean(this.heavy);
	}

	@Override
	public void read(ByteBuf b) 
	{
		this.id = b.readInt();
		this.tank = b.readInt();
		this.posX = b.readDouble();
		this.posY = b.readDouble();
		this.posZ = b.readDouble();
		this.vX = b.readDouble();
		this.vY = b.readDouble();
		this.vZ = b.readDouble();
		this.name = NetworkUtils.readString(b);
		this.type = NetworkUtils.readString(b);
		this.effect = NetworkUtils.readString(b);
		this.bounces = b.readInt();
		this.damage = b.readDouble();
		this.size = b.readDouble();
		this.heavy = b.readBoolean();
	}
}
