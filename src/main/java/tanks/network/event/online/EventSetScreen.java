package tanks.network.event.online;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.network.event.PersonalEvent;
import tanks.gui.screen.ScreenUploadLevel;
import tanks.network.NetworkUtils;

public class EventSetScreen extends PersonalEvent
{
    public String name;

    public EventSetScreen()
    {

    }

    public EventSetScreen(String name)
    {
        this.name = name;
    }

    @Override
    public void write(ByteBuf b)
    {
        NetworkUtils.writeString(b, this.name);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.name = NetworkUtils.readString(b);
    }

    @Override
    public void execute()
    {
        if (this.clientID != null)
            return;

        if (this.name.equals("upload_level"))
            Game.screen = new ScreenUploadLevel();
    }
}
