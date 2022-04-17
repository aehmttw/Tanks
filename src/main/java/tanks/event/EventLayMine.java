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
	public double size;

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
		this.size = m.size;
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

			Mine m = new Mine(this.posX, this.posY, this.timer, t, t.mine);
			m.networkID = id;
			m.size = size;
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
		b.writeDouble(this.size);
	}

	@Override
	public void read(ByteBuf b) 
	{
		this.id = b.readInt();
		this.tank = b.readInt();
		this.posX = b.readDouble();
		this.posY = b.readDouble();
		this.timer = b.readDouble();
		this.size = b.readDouble();
	}
}
