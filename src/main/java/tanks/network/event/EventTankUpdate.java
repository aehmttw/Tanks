package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.tank.Tank;
import tanks.tank.TankRemote;

public class EventTankUpdate extends PersonalEvent implements IStackableEvent
{
	public int tank;
	public double posX;
	public double posY;
	public double vX;
	public double vY;
	public double angle;
	public double pitch;
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
		this.pitch = t.pitch;
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
		b.writeDouble(this.pitch);
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
		this.pitch = b.readDouble();
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
				double iTime = Math.min(100, (time - r.lastUpdate) / 10.0);

				r.prevKnownPosX = r.posX;
				r.prevKnownPosY = r.posY;
				r.prevKnownVX = r.vX;
				r.prevKnownVY = r.vY;
				r.prevKnownVXFinal = r.lastFinalVX;
				r.prevKnownVYFinal = r.lastFinalVY;

				r.currentKnownPosX = this.posX;
				r.currentKnownPosY = this.posY;
				r.currentKnownVX = this.vX;
				r.currentKnownVY = this.vY;

				r.timeSinceRefresh = 0;
				r.interpolationTime = iTime;
				r.lastUpdate = time;

				r.lastAngle = r.angle;
				r.lastPitch = r.pitch;
				r.currentAngle = this.angle;
				r.currentPitch = this.pitch;
			}
			else
			{
				t.posX = this.posX;
				t.posY = this.posY;
				t.vX = this.vX;
				t.vY = this.vY;
				t.angle = this.angle;
				t.pitch = this.pitch;
			}
		}
	}

	@Override
	public int getIdentifier()
	{
		return this.tank;
	}
}
