package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.tank.Mine;

public class EventMineChangeTimer extends PersonalEvent
{
    public int mine;
    public double countdown;

    public EventMineChangeTimer()
    {

    }

    public EventMineChangeTimer(Mine m)
    {
        this.mine = m.networkID;
        this.countdown = m.timer;
    }

    @Override
    public void execute()
    {
        if (this.clientID != null)
            return;

        Mine m = Mine.idMap.get(mine);

        if (m == null)
            return;

        m.timer = countdown;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(this.mine);
        b.writeDouble(this.countdown);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.mine = b.readInt();
        this.countdown = b.readDouble();
    }
}
