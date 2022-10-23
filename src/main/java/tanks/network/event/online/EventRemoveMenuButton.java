package tanks.network.event.online;

import io.netty.buffer.ByteBuf;
import tanks.Panel;
import tanks.network.event.PersonalEvent;

public class EventRemoveMenuButton extends PersonalEvent
{
    public int id;

    public EventRemoveMenuButton(int id)
    {
        this.id = id;
    }

    public EventRemoveMenuButton()
    {

    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(id);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.id = b.readInt();
    }

    @Override
    public void execute()
    {
        if (this.clientID == null)
        {
            Panel.panel.onlineOverlay.buttons.remove(this.id);
        }
    }
}
