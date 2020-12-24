package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.obstacle.Obstacle;
import tanks.obstacle.ObstacleTeleporter;
import tanks.tank.Tank;
import tanks.tank.TeleporterOrb;

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
	public void write(ByteBuf b) 
	{
		b.writeInt(this.tank);
		b.writeDouble(this.fX);
		b.writeDouble(this.fY);
		b.writeDouble(this.iX);
		b.writeDouble(this.iY);
		b.writeDouble(this.dX);
		b.writeDouble(this.dY);
		b.writeDouble(this.age);
		b.writeDouble(this.maxAge);
		b.writeDouble(this.endAge);

	}

	@Override
	public void read(ByteBuf b) 
	{
		this.tank = b.readInt();
		this.fX = b.readDouble();
		this.fY = b.readDouble();
		this.iX = b.readDouble();
		this.iY = b.readDouble();
		this.dX = b.readDouble();
		this.dY = b.readDouble();
		this.age = b.readDouble();
		this.maxAge = b.readDouble();
		this.endAge = b.readDouble();
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
