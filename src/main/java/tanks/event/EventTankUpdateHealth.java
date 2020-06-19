package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.tank.Tank;

public class EventTankUpdateHealth extends PersonalEvent
{
	public int tank;
	public double health;
	
	public EventTankUpdateHealth()
	{
		
	}
	
	public EventTankUpdateHealth(Tank t)
	{
		tank = t.networkID;
		health = t.health;
	}
	
	@Override
	public void execute() 
	{
		Tank t = Tank.idMap.get(tank);

		if (t == null || this.clientID != null)
			return;

		if (t.health > health && health > 0)
			t.flashAnimation = 1;

		t.health = health;
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
