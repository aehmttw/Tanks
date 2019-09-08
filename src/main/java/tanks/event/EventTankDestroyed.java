package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.tank.Tank;

public class EventTankDestroyed implements INetworkEvent
{
	public int tank;
	
	public EventTankDestroyed()
	{
		
	}
	
	public EventTankDestroyed(Tank t)
	{
		tank = t.networkID;
	}
	
	@Override
	public void execute() 
	{
		Tank t = Tank.idMap.get(tank);
		if (t == null)
			return;

		t.destroyNextFrame = true;
		t.lives = 0;
		
		if (!Tank.freeIDs.contains(tank))
		{
			Tank.freeIDs.add(tank);
			Tank.idMap.remove(tank);
		}
	}

	@Override
	public void write(ByteBuf b) 
	{
		b.writeInt(this.tank);
	}

	@Override
	public void read(ByteBuf b)
	{
		this.tank = b.readInt();
	}
}
