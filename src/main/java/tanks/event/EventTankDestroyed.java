package tanks.event;

import tanks.tank.Tank;

public class EventTankDestroyed implements INetworkEvent
{
	public Tank tank;
	
	public EventTankDestroyed(Tank t)
	{
		tank = t;
	}
	
	public EventTankDestroyed(String t)
	{
		tank = Tank.idMap.get(Integer.parseInt(t));
	}
	
	@Override
	public String getNetworkString()
	{
		return "" + tank.networkID;
	}

	@Override
	public void execute() 
	{
		if (tank == null)
			return;

		tank.destroy = true;
		tank.lives = 0;
		
		if (!Tank.freeIDs.contains(tank.networkID))
		{
			Tank.freeIDs.add(tank.networkID);
			Tank.idMap.remove(tank.networkID);
		}
	}
}
