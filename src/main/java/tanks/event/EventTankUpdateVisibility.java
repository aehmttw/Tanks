package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.tank.Tank;
import tanks.tank.TankRemote;

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

	@Override
	public void write(ByteBuf b) 
	{
		b.writeInt(this.tank);
		b.writeBoolean(this.visible);
	}

	@Override
	public void read(ByteBuf b) 
	{
		this.tank = b.readInt();
		this.visible = b.readBoolean();
	}
}
