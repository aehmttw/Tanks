package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.tank.Tank;
import tanks.tank.TankPlayerController;
import tanks.tank.TankRemote;

public class EventTankUpdate extends PersonalEvent
{
	public int tank;
	public double posX;
	public double posY;
	public double vX;
	public double vY;
	public double angle;
	public long time = System.currentTimeMillis();


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
		
		if (t != null && this.clientID == null)
		{
			if (t instanceof TankRemote)
			{
				TankRemote r = (TankRemote) t;
				double iTime = Math.max(0.1, (time - r.lastUpdate) / 10.0);

				r.interpolatedOffX = this.posX - (t.posX - r.interpolatedOffX * (r.interpolationTime - r.interpolatedProgress) / r.interpolationTime);
				r.interpolatedOffY = this.posY - (t.posY - r.interpolatedOffY * (r.interpolationTime - r.interpolatedProgress) / r.interpolationTime);
				r.interpolatedProgress = 0;
				r.interpolationTime = iTime;
				r.lastUpdate = time;
			}

			t.posX = this.posX;
			t.posY = this.posY;
			t.vX = this.vX;
			t.vY = this.vY;
			t.angle = this.angle;
		}
	}

}
