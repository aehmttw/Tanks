package tanks.network.event;

import tanks.Game;
import tanks.obstacle.*;
import tanks.tank.*;

public class EventTankTeleport extends PersonalEvent
{
	public int tank;
	
	public double fX;
	public double fY;
	public double iX;
	public double iY;
	public double dX;
	public double dY;
	
	public double age;
	public double maxAge;
	public double endAge;
	
	public EventTankTeleport()
	{
		
	}
	
	public EventTankTeleport(TeleporterOrb t)
	{
		this.tank = t.tank.networkID;
		
		this.fX = t.fX;
		this.fY = t.fY;
		this.iX = t.iX;
		this.iY = t.iY;
		this.dX = t.dX;
		this.dY = t.dY;
		
		this.age = t.age;
		this.maxAge = t.maxAge;
		this.endAge = t.endAge;
	}

	@Override
	public void execute() 
	{
		if (this.clientID != null)
			return;

		Tank tank = Tank.idMap.get(this.tank);

		if (tank == null)
			return;

		TeleporterOrb t = new TeleporterOrb(this.fX, this.fY, this.iX, this.iY, this.dX, this.dY, tank);
		t.age = this.age;
		t.maxAge = this.maxAge;
		t.endAge = this.endAge;
		Game.movables.add(t);

		for (int i = 0; i < Game.obstacles.size(); i++)
		{
			Obstacle o = Game.obstacles.get(i);

			if (o instanceof ObstacleTeleporter && ((o.posX == this.iX && o.posY == this.iY) || (o.posX == this.dX && o.posY == this.dY)))
			{
				((ObstacleTeleporter)o).cooldown = 500;
			}
		}
	}

}
