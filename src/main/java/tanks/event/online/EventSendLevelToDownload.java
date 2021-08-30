package tanks.event.online;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Level;
import tanks.event.PersonalEvent;
import tanks.gui.screen.ScreenDownloadLevel;
import tanks.network.NetworkUtils;

public class EventSendLevelToDownload extends PersonalEvent
{
    public String name;
    public String level;

    public EventSendLevelToDownload()
    {

    }

    public EventSendLevelToDownload(String name, String level)
    {
        this.name = name;
        this.level = level;
    }

    @Override
    public void write(ByteBuf b)
    {
        NetworkUtils.writeString(b, this.name);
        NetworkUtils.writeString(b, this.level);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.name = NetworkUtils.readString(b);
        this.level = NetworkUtils.readString(b);
    }

    @Override
    public void execute()
    {
        if (this.clientID == null)
        {
            ScreenDownloadLevel sc = new ScreenDownloadLevel(this.name, this.level);
            Level l = new Level(this.level);
            l.preview = true;
            l.loadLevel(sc);
            Game.screen = sc;
        }
    }
}
