package tanks.network.event;

import tanks.gui.screen.ScreenGame;

import io.netty.buffer.ByteBuf;

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
