package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.bullet.Bullet;
import tanks.tank.Mine;
import tanks.tank.Tank;

public class EventMineExplode extends PersonalEvent
{
    public int mine;

    public EventMineExplode()
    {

    }

    public EventMineExplode(Mine m)
    {
        this.mine = m.networkID;
    }

    @Override
    public void execute()
    {
        if (this.clientID != null)
            return;

        Mine m = Mine.idMap.get(mine);

        if (m == null)
            return;

        m.explode();

        if (!Mine.freeIDs.contains(m.networkID))
        {
            Mine.freeIDs.add(m.networkID);
            Mine.idMap.remove(m.networkID);
        }
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(this.mine);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.mine = b.readInt();
    }
}
