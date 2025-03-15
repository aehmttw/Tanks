package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.screen.ScreenGame;
import tanks.network.NetworkUtils;

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
