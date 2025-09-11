package tanks.network.event;

import tanks.tank.*;

public class EventTankUpdate extends PersonalEvent implements IStackableEvent
{
	public int tank;
    public double posX, posY;
    public double vX, vY;
    public double angle, pitch;

    @NetworkIgnored
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
	public void execute()
	{
		Tank t = Tank.idMap.get(this.tank);
		
		if (t != null && this.clientID == null)
		{
			if (t instanceof TankRemote)
			{
				TankRemote r = (TankRemote) t;
				double iTime = Math.min(100, Math.max(0, time - r.lastUpdate) / 10.0);

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
