package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.tank.Tank;

public class EventTankUpdateHealth implements INetworkEvent
{
	public int tank;
	public double health;
	
	public EventTankUpdateHealth()
	{
		
	}
	
	public EventTankUpdateHealth(Tank t)
	{
		tank = t.networkID;
		health = t.lives;
	}
	
	@Override
	public void execute() 
	{
		Tank t = Tank.idMap.get(tank);
		if (t == null)
			return;

		if (t.lives > health && health > 0)
			t.flashAnimation = 1;

		t.lives = health;
	}

	@Override
	public void write(ByteBuf b) 
	{
		b.writeInt(this.tank);
		b.writeDouble(this.health);
	}

	@Override
	public void read(ByteBuf b)
	{
		this.tank = b.readInt();
		this.health = b.readDouble();
	}
}
