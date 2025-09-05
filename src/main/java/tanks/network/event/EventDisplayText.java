package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Crusade;
import tanks.Game;
import tanks.ModAPI;
import tanks.gui.FixedText;
import tanks.network.NetworkUtils;

public class EventDisplayText extends PersonalEvent
{
    public String location;
    public String text;
    public boolean afterGameStarted;
    public int duration;
    public boolean hasItems = Game.currentLevel != null && (Game.currentLevel.shop.size() > 0 || Game.currentLevel.startingItems.size() > 0 || Crusade.crusadeMode);

    public double colorR;
    public double colorG;
    public double colorB;

    public EventDisplayText()
    {

    }

    public EventDisplayText(String location, String text, boolean afterGameStarted, int durationInMs, double r, double g, double b)
    {
        this.location = location;
        this.text = text;
        this.duration = durationInMs;
        this.afterGameStarted = afterGameStarted;

        this.colorR = r;
        this.colorG = g;
        this.colorB = b;
    }

    @Override
    public void execute()
    {
        FixedText t = new FixedText(this.location, this.text, this.afterGameStarted, this.duration, this.colorR, this.colorG, this.colorB);
        t.hasItems = this.hasItems;
        ModAPI.menuGroup.add(t);
    }
}
