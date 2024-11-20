package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Panel;
import tanks.bullet.Bullet;
import tanks.bullet.BulletInstant;
import tanks.item.Item;
import tanks.item.ItemBullet;
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
	public int item;
	
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
		this.item = b.item.networkIndex;
	}

	@Override
	public void execute() 
	{
		if (this.clientID != null)
			return;

		Tank t = Tank.idMap.get(this.tank);

		if (t == null)
			return;

		try
		{
			Bullet sb = null;
			if (this.item > 0)
			{
				Item i = Game.currentLevel.clientShop.get(this.item - 1).itemStack.item;
				if (i instanceof ItemBullet)
					sb = ((ItemBullet) i).bullet;
			}
			else if (this.item < 0)
			{
				Item i = Game.currentLevel.clientStartingItems.get(-this.item - 1).item;
				if (i instanceof ItemBullet)
					sb = ((ItemBullet) i).bullet;
			}

			Bullet b;

			if (sb == null)
				b = t.bullet.getClass().getConstructor(double.class, double.class, Tank.class, boolean.class, ItemBullet.ItemStackBullet.class).newInstance(this.posX, this.posY, t, false, t.bulletItem);
			else
				b = sb.getClass().getConstructor(double.class, double.class, Tank.class, boolean.class, ItemBullet.ItemStackBullet.class).newInstance(this.posX, this.posY, t, false, t.bulletItem);

			b.posZ = posZ;
			b.vX = vX;
			b.vY = vY;
			b.vZ = vZ;

			if (sb == null)
				t.bullet.clonePropertiesTo(b);
			else
				sb.clonePropertiesTo(b);

			b.setColorFromTank();

			if (t.bulletItem.item.cooldownBase <= 0)
				b.frameDamageMultipler = Panel.frameFrequency;

			b.networkID = this.id;
			Bullet.idMap.put(this.id, b);

			if (!(b instanceof BulletInstant))
				Game.movables.add(b);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
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
		b.writeInt(this.item);
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
		this.item = b.readInt();
	}
}
