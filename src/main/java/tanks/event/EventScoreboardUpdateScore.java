package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.ModAPI;
import tanks.gui.IFixedMenu;
import tanks.gui.RemoteScoreboard;
import tanks.network.NetworkUtils;

public class EventScoreboardUpdateScore extends PersonalEvent
{
    public int id;

    public String name;

    public double value;

    public EventScoreboardUpdateScore()
    {
    }

    public EventScoreboardUpdateScore(int scoreboardID, String name, double value)
    {
        this.id = scoreboardID;
        this.name = name;
        this.value = value;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(id);
        NetworkUtils.writeString(b, name);
        b.writeDouble(value);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.id = b.readInt();
        this.name = NetworkUtils.readString(b);
        this.value = b.readDouble();
    }

    @Override
    public void execute()
    {
        for (IFixedMenu m : ModAPI.menuGroup)
        {
            if (m instanceof RemoteScoreboard && ((RemoteScoreboard) m).id == this.id)
            {
                if (((RemoteScoreboard) m).scores.containsKey(this.name))
                    ((RemoteScoreboard) m).scores.replace(this.name, this.value);
                else
                {
                    ((RemoteScoreboard) m).names.add(this.name);
                    ((RemoteScoreboard) m).scores.put(this.name, this.value);
                }
            }
        }
    }
}
