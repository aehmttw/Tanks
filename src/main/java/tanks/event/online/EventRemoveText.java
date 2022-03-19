package tanks.event.online;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.event.PersonalEvent;
import tanks.gui.screen.ScreenOnline;

public class EventRemoveText extends PersonalEvent
{
    public int id;

    public EventRemoveText(int id)
    {
        this.id = id;
    }

    public EventRemoveText()
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
            ((ScreenOnline) Game.screen).removeText(this.id);
        }
    }
}
