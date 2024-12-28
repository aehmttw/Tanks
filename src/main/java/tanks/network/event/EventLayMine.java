package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.item.Item;
import tanks.item.ItemBullet;
import tanks.item.ItemMine;
import tanks.tank.*;

public class EventLayMine extends PersonalEvent
{
	public int id;

	public int tank;
	public double posX;
	public double posY;
	public int item;

	public EventLayMine()
	{
		
	}
	
	public EventLayMine(Mine m)
	{
		this.id = m.networkID;
		this.tank = m.tank.networkID;
		this.posX = m.posX;
		this.posY = m.posY;
		this.item = m.item.networkIndex;
	}

	@Override
	public void execute() 
	{
		if (clientID == null)
		{
			System.out.println(this.item);
			Tank t = Tank.idMap.get(tank);

			if (tank == -1)
				t = Game.dummyTank;

			if (t == null)
				return;

			ItemMine.ItemStackMine sm = null;
			if (this.item > 0 && this.item <= Game.currentLevel.clientShop.size())
			{
				Item.ItemStack<?> i = Game.currentLevel.clientShop.get(this.item - 1).itemStack;
				if (i instanceof ItemMine.ItemStackMine)
					sm = (ItemMine.ItemStackMine) i;
			}
			else if (this.item > Game.currentLevel.clientShop.size())
			{
				Item.ItemStack<?> i = Game.currentLevel.clientStartingItems.get(this.item - 1 - Game.currentLevel.clientShop.size());

				if (i instanceof ItemMine.ItemStackMine)
					sm = ((ItemMine.ItemStackMine) i);
			}
			else if (t instanceof TankRemote || t instanceof TankPlayer)
			{
				Tank t2 = t;

				if (t instanceof TankRemote)
					t2 = ((TankRemote) t).tank;

				if (t2 instanceof TankAIControlled)
					sm = ((TankAIControlled) ((TankRemote) t).tank).mineItem;
				else if (t2 instanceof TankPlayer)
					sm = ((ItemMine.ItemStackMine)(((TankPlayer) t2).abilities.get(-this.item)));
			}

			if (sm == null)
				return;

			Mine m = new Mine(this.posX, this.posY, sm.item.mine.timer, t, sm);
			m.networkID = id;
			m.size = sm.item.mine.size;
			Game.movables.add(m);

			Mine.idMap.put(id, m);
		}
	}

	@Override
	public void write(ByteBuf b) 
	{
		b.writeInt(this.id);
		b.writeInt(this.tank);
		b.writeDouble(this.posX);
		b.writeDouble(this.posY);
		b.writeInt(this.item);
	}

	@Override
	public void read(ByteBuf b) 
	{
		this.id = b.readInt();
		this.tank = b.readInt();
		this.posX = b.readDouble();
		this.posY = b.readDouble();
		this.item = b.readInt();
	}
}
