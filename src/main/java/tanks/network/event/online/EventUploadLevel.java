package tanks.network.event.online;

import io.netty.buffer.ByteBuf;
import tanks.network.NetworkUtils;
import tanks.network.event.PersonalEvent;
import tanksonline.PlayerMap;
import tanksonline.TanksOnlineServerHandler;
import tanksonline.UploadedLevel;
import tanksonline.screen.ScreenLayout;
import tanksonline.screen.ScreenUploadFinished;

public class EventUploadLevel extends PersonalEvent implements IOnlineServerEvent
{
    public String name;
    public String level;

    public EventUploadLevel()
    {

    }

    public EventUploadLevel(String name, String l)
    {
        this.name = name;
        this.level = l;
    }

    @Override
    public void execute(TanksOnlineServerHandler s)
    {
        UploadedLevel l = new UploadedLevel(name, level, s.computerID, System.currentTimeMillis());

        UploadedLevel.UploadResult success;

        synchronized (PlayerMap.instance)
        {
            if (!PlayerMap.instance.getPlayer(s.computerID).registered)
                return;
        }

        success = PlayerMap.instance.uploadLevel(l);

        String message = "Level successfully uploaded!";

        if (success == UploadedLevel.UploadResult.nameTaken)
            message = "Failed to upload level - you have uploaded a level with that name!";
        else if (success == UploadedLevel.UploadResult.error)
            message = "Failed to upload level - an unexpected error occurred";

        s.sendEvent(new EventCleanUp());
        ScreenLayout sc = new ScreenUploadFinished(s, message);
        sc.setScreen();
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

    }
}
