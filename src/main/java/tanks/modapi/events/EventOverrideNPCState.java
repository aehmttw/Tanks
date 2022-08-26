package tanks.modapi.events;

import io.netty.buffer.ByteBuf;
import tanks.event.PersonalEvent;
import tanks.modapi.TankNPC;
import tanks.tank.Tank;

public class EventOverrideNPCState extends PersonalEvent
{
    public int id;
    public boolean display;
    public boolean override;

    public EventOverrideNPCState()
    {
    }

    public EventOverrideNPCState(TankNPC t)
    {
        this.id = t.networkID;
        this.display = t.draw;
        this.override = t.overrideDisplayState;
    }

    @Override
    public void execute()
    {
        TankNPC t = (TankNPC) Tank.idMap.get(this.id);

        if (t == null)
            return;

        t.draw = this.display;
        t.overrideDisplayState = this.override;

        if (this.display)
            t.initMessageScreen();
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(this.id);
        b.writeBoolean(this.display);
        b.writeBoolean(this.override);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.id = b.readInt();
        this.display = b.readBoolean();
        this.override = b.readBoolean();
    }
}
