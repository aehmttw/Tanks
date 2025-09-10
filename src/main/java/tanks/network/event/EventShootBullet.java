package tanks.network.event;

import tanks.*;
import tanks.bullet.*;
import tanks.item.*;
import tanks.tank.*;

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
	public double speed;
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
		this.speed = b.speed;
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
			ItemBullet.ItemStackBullet sb = null;
			if (this.item > 0 && this.item <= Game.currentLevel.clientShop.size())
			{
				Item.ItemStack<?> i = Game.currentLevel.clientShop.get(this.item - 1).itemStack;
				if (i instanceof ItemBullet.ItemStackBullet)
					sb = (ItemBullet.ItemStackBullet) i;
			}
			else if (this.item > Game.currentLevel.clientShop.size())
			{
				Item.ItemStack<?> i = Game.currentLevel.clientStartingItems.get(this.item - 1 - Game.currentLevel.clientShop.size());

				if (i instanceof ItemBullet.ItemStackBullet)
					sb = ((ItemBullet.ItemStackBullet) i);
			}
			else if (t instanceof TankRemote || t instanceof TankPlayer)
			{
				Tank t2 = t;

				if (t instanceof TankRemote)
					t2 = ((TankRemote) t).tank;

				if (t2 instanceof TankAIControlled)
					sb = ((TankAIControlled) t2).bulletItem;
				else if (t2 instanceof TankPlayer)
					sb = ((ItemBullet.ItemStackBullet)(((TankPlayer) t2).abilities.get(-this.item - 1)));
			}

			if (sb == null)
				return;

			Bullet b = sb.item.bullet.getClass().getConstructor(double.class, double.class, Tank.class, boolean.class, ItemBullet.ItemStackBullet.class).newInstance(this.posX, this.posY, t, false, sb);
			b.posZ = posZ;
			b.vX = vX;
			b.vY = vY;
			b.vZ = vZ;

			sb.item.bullet.clonePropertiesTo(b);

			b.speed = speed;

			b.setColorFromTank();

			if (sb.item.cooldownBase <= 0)
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
}
