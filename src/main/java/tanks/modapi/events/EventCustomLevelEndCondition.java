package tanks.modapi.events;

import io.netty.buffer.ByteBuf;
import tanks.event.PersonalEvent;
import tanks.gui.screen.ScreenGame;

public class EventCustomLevelEndCondition extends PersonalEvent
{
    @Override
    public void execute()
    {
        if (this.clientID == null)
            ScreenGame.remoteCustomLevelEndCondition = true;
    }

    @Override
    public void write(ByteBuf b)
    {
    }

    @Override
    public void read(ByteBuf b)
    {
    }
}
