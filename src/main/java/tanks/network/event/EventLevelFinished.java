package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.gui.screen.ScreenGame;

public class EventLevelFinished extends PersonalEvent
{
    public EventLevelFinished()
    {

    }

    @Override
    public void execute()
    {
        if (this.clientID != null)
            return;

        ScreenGame.finished = true;
    }

    @Override
    public void write(ByteBuf b) { }

    @Override
    public void read(ByteBuf b) { }
}
