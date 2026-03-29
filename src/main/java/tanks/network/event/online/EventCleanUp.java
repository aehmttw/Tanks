package tanks.network.event.online;

import tanks.Game;
import tanks.network.event.PersonalEvent;

import io.netty.buffer.ByteBuf;

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
