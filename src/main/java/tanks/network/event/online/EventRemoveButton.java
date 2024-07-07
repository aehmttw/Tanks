package tanks.network.event.online;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.gui.screen.ScreenOnline;
import tanks.network.event.PersonalEvent;

public class EventRemoveButton extends PersonalEvent
{
    public int id;

    public EventRemoveButton(int id)
    {
        this.id = id;
    }

    public EventRemoveButton()
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
        if (this.clientID == null && Game.screen instanceof ScreenOnline)
        {
            ((ScreenOnline) Game.screen).removeButton(this.id);
        }
    }
}
