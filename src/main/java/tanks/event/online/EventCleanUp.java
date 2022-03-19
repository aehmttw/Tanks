package tanks.event.online;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.event.PersonalEvent;

public class EventCleanUp extends PersonalEvent
{
    @Override
    public void write(ByteBuf b)
    {

    }

    @Override
    public void read(ByteBuf b)
    {

    }

    @Override
    public void execute()
    {
        if (this.clientID == null)
        {
            Game.cleanUp();
        }
    }
}
