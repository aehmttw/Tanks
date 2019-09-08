package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.tank.Tank;

public class EventTankUpdate implements INetworkEvent
{
	public int tank;
	public double posX;
	public double posY;
	public double vX;
	public double vY;
	public double angle;

	public EventTankUpdate()
	{
		
	}
	
	public EventTankUpdate(Tank t)
	{
		this.tank = t.networkID;
		this.posX = t.posX;
		this.posY = t.posY;
		this.vX = t.vX;
		this.vY = t.vY;
		this.angle = t.angle;
	}
	
	@Override
	public void write(ByteBuf b)
	{
		b.writeInt(this.tank);
		b.writeDouble(this.posX);
		b.writeDouble(this.posY);
		b.writeDouble(this.vX);
		b.writeDouble(this.vY);
		b.writeDouble(this.angle);
	}

	@Override
	public void read(ByteBuf b) 
	{
		this.tank = b.readInt();
		this.posX = b.readDouble();
		this.posY = b.readDouble();
		this.vX = b.readDouble();
		this.vY = b.readDouble();
		this.angle = b.readDouble();
	}

	@Override
	public void execute()
	{
		Tank t = Tank.idMap.get(this.tank);
		
		if (t != null)
		{
			t.posX = this.posX;
			t.posY = this.posY;
			t.vX = this.vX;
			t.vY = this.vY;
			t.angle = this.angle;
		}
	}

}
