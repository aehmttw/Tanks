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

        t.vX = 0;
        t.vY = 0;

        if (destroyAnimation)
            t.destroy = true;
        else
            Game.removeMovables.add(t);

        t.unregisterNetworkID();
    }
}
