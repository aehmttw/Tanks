package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.tank.Mine;
import tanks.tank.Tank;

public class EventLayMine extends PersonalEvent
{
	public int id;

	public int tank;
	public double posX;
	public double posY;
	public double timer;
	public double radius;
	public double size;
	public boolean destroysObstacles;
	
	public EventLayMine()
	{
		
	}
	
	public EventLayMine(Mine m)
	{
		this.id = m.networkID;
		this.tank = m.tank.networkID;
		this.posX = m.posX;
		this.posY = m.posY;
		this.timer = m.timer;
		this.radius = m.radius;
		this.size = m.size;
		this.destroysObstacles = m.destroysObstacles;
	}

	@Override
	public void execute() 
	{
		if (clientID == null)
		{
			Tank t = Tank.idMap.get(tank);

			if (tank == -1)
				t = Game.dummyTank;

			if (t == null)
				return;

			Mine m = new Mine(this.posX, this.posY, this.timer, t);
			m.networkID = id;
			m.size = size;
			m.radius = radius;
			m.destroysObstacles = destroysObstacles;
			Game.movables.add(m);

			Mine.idMap.put(id, m);
		}
	}

	@Override
	public void write(ByteBuf b) 
	{
		b.writeInt(this.id);
		b.writeInt(this.tank);
		b.writeDouble(this.posX);
		b.writeDouble(this.posY);
		b.writeDouble(this.timer);
		b.writeDouble(this.radius);
		b.writeDouble(this.size);
		b.writeBoolean(this.destroysObstacles);
	}

	@Override
	public void read(ByteBuf b) 
	{
		this.id = b.readInt();
		this.tank = b.readInt();
		this.posX = b.readDouble();
		this.posY = b.readDouble();
		this.timer = b.readDouble();
		this.radius = b.readDouble();
		this.size = b.readDouble();
		this.destroysObstacles = b.readBoolean();
	}
}
