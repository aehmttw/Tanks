package tanks.modapi.events;

import io.netty.buffer.ByteBuf;
import tanks.event.PersonalEvent;
import tanks.modapi.TankNPC;
import tanks.network.NetworkUtils;
import tanks.tank.Tank;

public class EventChangeNPCMessage extends PersonalEvent
{
    public String[] messages;
    public int id;

    public EventChangeNPCMessage() {}

    public EventChangeNPCMessage(TankNPC t)
    {
        this.messages = t.messages;
        this.id = t.networkID;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(this.id);

        StringBuilder m = new StringBuilder();
        for (String s : messages)
            m.append(s).append("\n");

        NetworkUtils.writeString(b, m.toString());
    }

    @Override
    public void read(ByteBuf b)
    {
        this.id = b.readInt();
        this.messages = NetworkUtils.readString(b).split("\n");
    }

    @Override
    public void execute()
    {
        TankNPC t = (TankNPC) Tank.idMap.get(this.id);
        t.messages = this.messages;
        t.messageNum = 0;
        t.initMessageScreen();
    }
}
