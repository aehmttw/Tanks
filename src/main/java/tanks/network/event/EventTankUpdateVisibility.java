package tanks.network.event;

import tanks.tank.*;

public class EventTankUpdateVisibility extends PersonalEvent
{
	public int tank;
	public boolean visible;

	public EventTankUpdateVisibility()
	{

	}

	public EventTankUpdateVisibility(int tank, boolean visible)
	{
		this.tank = tank;
		this.visible = visible;
	}

	@Override
	public void execute() 
	{
		Tank t = Tank.idMap.get(this.tank);

		if (t instanceof TankRemote && this.clientID == null)
		{
			((TankRemote) t).invisible = !visible;
		}
	}
}
