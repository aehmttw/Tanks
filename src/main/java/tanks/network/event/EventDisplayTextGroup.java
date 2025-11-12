package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.ModAPI;
import tanks.gui.FixedTextGroup;
import tanks.network.NetworkUtils;

import java.util.*;

public class EventDisplayTextGroup extends PersonalEvent
{
    public String location;
    public ArrayList<String> texts;
    public boolean afterGameStarted;
    public ArrayList<Integer> duration;
    public double fontSize;
    public double colorR;
    public double colorG;
    public double colorB;

    public EventDisplayTextGroup()
    {

    }

    public EventDisplayTextGroup(String location, ArrayList<String> text, boolean afterGameStarted, ArrayList<Integer> durationInMs, double fontSize, double r, double g, double b)
    {
        this.location = location;
        this.texts = text;
        this.duration = durationInMs;
        this.afterGameStarted = afterGameStarted;

        this.fontSize = fontSize;
        this.colorR = r;
        this.colorG = g;
        this.colorB = b;
    }

    @Override
    public void write(ByteBuf b)
    {
        NetworkUtils.writeString(b, this.location);

        StringBuilder texts = new StringBuilder();
        for (String s : this.texts)
            texts.append(s).append("---");
        NetworkUtils.writeString(b, texts.toString());

        b.writeBoolean(this.afterGameStarted);

        StringBuilder durations = new StringBuilder();
        for (int i : this.duration)
            durations.append(i).append(" ");
        NetworkUtils.writeString(b, durations.toString());

        b.writeDouble(this.fontSize);
        b.writeDouble(this.colorR);
        b.writeDouble(this.colorG);
        b.writeDouble(this.colorB);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.location = NetworkUtils.readString(b);

        String[] texts = NetworkUtils.readString(b).split("---");
        this.texts = new ArrayList<>(Arrays.asList(texts));

        this.afterGameStarted = b.readBoolean();

        ArrayList<String> durations = new ArrayList<>(Arrays.asList(NetworkUtils.readString(b).split(" ")));
        this.duration = new ArrayList<>();
        for (String s : durations)
            this.duration.add(Integer.parseInt(s));

        this.fontSize = b.readDouble();
        this.colorR = b.readDouble();
        this.colorG = b.readDouble();
        this.colorB = b.readDouble();
    }

    @Override
    public void execute()
    {
        ModAPI.menuGroup.add(new FixedTextGroup(location, texts, afterGameStarted, duration, this.fontSize, this.colorR, this.colorG, this.colorB));
    }
}
