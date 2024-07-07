package tanks.network.event.online;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Panel;
import tanks.gui.screen.ScreenOnline;
import tanks.network.NetworkUtils;
import tanks.network.event.PersonalEvent;

public class EventSetPauseScreenTitle extends PersonalEvent
{
    public String title;

    public EventSetPauseScreenTitle(String title)
    {
        this.title = title;
    }

    public EventSetPauseScreenTitle()
    {

    }

    @Override
    public void write(ByteBuf b)
    {
        NetworkUtils.writeString(b, title);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.title = NetworkUtils.readString(b);
    }

    @Override
    public void execute()
    {
        if (this.clientID == null && Game.screen instanceof ScreenOnline)
        {
            Panel.panel.onlineOverlay.title = title;
        }
    }
}
