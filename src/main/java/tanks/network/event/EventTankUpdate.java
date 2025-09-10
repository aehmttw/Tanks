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
                boolean firstFrame = r.lastUpdate == -1;
				double iTime = Math.min(100, (time - r.lastUpdate) / 10.0);

				r.prevKnownPosX = firstFrame ? this.posX : r.posX;
				r.prevKnownPosY = firstFrame ? this.posY : r.posY;
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

				r.lastAngle = firstFrame ? this.angle : r.angle;
				r.lastPitch = firstFrame ? this.pitch : r.pitch;
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
