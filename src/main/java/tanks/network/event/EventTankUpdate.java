package tanks.network.event;

import tanks.tank.*;

public class EventTankUpdate extends PersonalEvent implements IStackableEvent
{
	public int tank;
    public double posX, posY;
    public double vX, vY;
    public double angle, pitch;


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
				((TankRemote) t).updatePositions(this.posX, this.posY, this.vX, this.vY, this.angle, this.pitch);
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
