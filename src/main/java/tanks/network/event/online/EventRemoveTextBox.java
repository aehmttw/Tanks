package tanks.network.event.online;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.network.event.PersonalEvent;
import tanks.gui.screen.ScreenOnline;

public class EventRemoveTextBox extends PersonalEvent
{
    public int id;

    public EventRemoveTextBox(int id)
    {
        this.id = id;
    }

    public EventRemoveTextBox()
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
            ((ScreenOnline) Game.screen).removeTextbox(this.id);
        }
    }
}
