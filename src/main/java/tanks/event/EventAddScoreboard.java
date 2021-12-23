package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.ModAPI;
import tanks.gui.RemoteScoreboard;
import tanks.network.NetworkUtils;

import java.util.ArrayList;
import java.util.Collections;

public class EventAddScoreboard extends PersonalEvent
{
    public String objectiveName;
    public ArrayList<String> names = new ArrayList<>();
    public String objectiveType;
    public int id;
    public double titleColorR;
    public double titleColorG;
    public double titleColorB;
    public double titleFontSize;
    public double namesFontSize;

    public EventAddScoreboard()
    {

    }

    public EventAddScoreboard(RemoteScoreboard scoreboard)
    {
        this.id = scoreboard.id;

        this.objectiveName = scoreboard.name;
        this.objectiveType = scoreboard.objectiveType;
        this.names = scoreboard.names;

        this.titleColorR = scoreboard.titleColR;
        this.titleColorG = scoreboard.titleColG;
        this.titleColorB = scoreboard.titleColB;
        this.titleFontSize = scoreboard.titleFontSize;
        this.namesFontSize = scoreboard.namesFontSize;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(this.id);

        NetworkUtils.writeString(b, this.objectiveName);
        NetworkUtils.writeString(b, this.objectiveType);

        StringBuilder names = new StringBuilder();
        for (String s : this.names)
            names.append(s).append("%s");

        NetworkUtils.writeString(b, names.toString());

        b.writeDouble(this.titleColorR);
        b.writeDouble(this.titleColorG);
        b.writeDouble(this.titleColorB);
        b.writeDouble(this.titleFontSize);
        b.writeDouble(this.namesFontSize);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.id = b.readInt();

        this.objectiveName = NetworkUtils.readString(b);
        this.objectiveType = NetworkUtils.readString(b);

        String[] names = NetworkUtils.readString(b).split("%s");
        Collections.addAll(this.names, names);

        this.titleColorR = b.readDouble();
        this.titleColorG = b.readDouble();
        this.titleColorB = b.readDouble();
        this.titleFontSize = b.readDouble();
        this.namesFontSize = b.readDouble();
    }

    @Override
    public void execute()
    {
        RemoteScoreboard scoreboard = new RemoteScoreboard(objectiveName, objectiveType, names);
        scoreboard.id = this.id;
        scoreboard.titleColR = this.titleColorR;
        scoreboard.titleColG = this.titleColorG;
        scoreboard.titleColB = this.titleColorB;
        scoreboard.titleFontSize = this.titleFontSize;
        scoreboard.namesFontSize = this.namesFontSize;

        ModAPI.menuGroup.add(scoreboard);
    }
}
