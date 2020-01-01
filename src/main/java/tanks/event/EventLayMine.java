package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Drawing;
import tanks.Game;
import tanks.tank.Mine;
import tanks.tank.Tank;

public class EventLayMine extends PersonalEvent
{
	public int tank;
	public double posX;
	public double posY;
	public double timer;
	public double radius;
	
	public EventLayMine()
	{
		
	}
	
	public EventLayMine(Mine m)
	{
		this.tank = m.tank.networkID;
		this.posX = m.posX;
		this.posY = m.posY;
		this.timer = m.timer;
		this.radius = m.radius;
	}

	@Override
	public void execute() 
	{
		if (clientID == null)
		{
			Mine m = new Mine(this.posX, this.posY, this.timer, Tank.idMap.get(tank));
			Game.movables.add(m);
		}
	}

	@Override
	public void write(ByteBuf b) 
	{
		b.writeInt(this.tank);
		b.writeDouble(this.posX);
		b.writeDouble(this.posY);
		b.writeDouble(this.timer);
		b.writeDouble(this.radius);
	}

	@Override
	public void read(ByteBuf b) 
	{
		this.tank = b.readInt();
		this.posX = b.readDouble();
		this.posY = b.readDouble();
		this.timer = b.readDouble();
		this.radius = b.readDouble();
	}
}
