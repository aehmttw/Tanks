package tanks.network.event.online;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.gui.screen.ScreenOnline;
import tanks.network.event.PersonalEvent;

public class EventNewScreen extends PersonalEvent
{
    public EventNewScreen()
    {

    }

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
            Game.screen = new ScreenOnline();
        }
    }
}
