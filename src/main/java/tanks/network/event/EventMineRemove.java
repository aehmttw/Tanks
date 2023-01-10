package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.tank.Mine;

public class EventMineRemove extends PersonalEvent
{
    public int mine;

    public EventMineRemove()
    {

    }

    public EventMineRemove(Mine m)
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

        Game.removeMovables.add(m);

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
