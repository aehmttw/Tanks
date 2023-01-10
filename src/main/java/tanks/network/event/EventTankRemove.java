package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.tank.Tank;

public class EventTankRemove extends PersonalEvent
{
    public int tank;
    public boolean destroyAnimation;

    public EventTankRemove()
    {

    }

    public EventTankRemove(Tank t, boolean destroyAnimation)
    {
        this.tank = t.networkID;
        this.destroyAnimation = destroyAnimation;
    }

    @Override
    public void execute()
    {
        if (this.clientID != null)
            return;

        Tank t = Tank.idMap.get(this.tank);

        if (t == null)
            return;

        if (destroyAnimation)
            t.destroy = true;
        else
            Game.removeMovables.add(t);

        t.unregisterNetworkID();
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(this.tank);
        b.writeBoolean(this.destroyAnimation);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.tank = b.readInt();
        this.destroyAnimation = b.readBoolean();
    }
}
