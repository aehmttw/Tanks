package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.tank.Tank;
import tanks.tank.TankRemote;

public class EventRemoveTank extends PersonalEvent
{
    public int tank;

    public EventRemoveTank()
    {

    }

    public EventRemoveTank(Tank t)
    {
        this.tank = t.networkID;
    }

    @Override
    public void execute()
    {
        if (this.clientID != null)
            return;

        Tank t = Tank.idMap.get(this.tank);

        if (t == null)
            return;

        Game.removeMovables.add(t);

        if (!Tank.freeIDs.contains(t.networkID))
        {
            Tank.freeIDs.add(t.networkID);
            Tank.idMap.remove(t.networkID);
        }
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(this.tank);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.tank = b.readInt();
    }
}
