package tanks.network.event;

import tanks.ModAPI;
import tanks.gui.*;

public class EventChangeScoreboardAttribute extends PersonalEvent
{
    public int id;
    public String attributeName;
    public double value;

    public EventChangeScoreboardAttribute()
    {

    }

    public EventChangeScoreboardAttribute(int id, String attributeName, double value)
    {
        this.id = id;
        this.attributeName = attributeName;
        this.value = value;
    }

    @Override
    public void execute()
    {
        for (IFixedMenu s : ModAPI.menuGroup)
        {
            if (s instanceof RemoteScoreboard && ((RemoteScoreboard) s).id == this.id)
            {
                switch (this.attributeName)
                {
                    case "titleColR":
                        ((RemoteScoreboard) s).titleColR = this.value;
                        break;
                    case "titleColG":
                        ((RemoteScoreboard) s).titleColG = this.value;
                        break;
                    case "titleColB":
                        ((RemoteScoreboard) s).titleColB = this.value;
                        break;
                    case "titleFontSize":
                        ((RemoteScoreboard) s).titleFontSize = this.value;
                        break;
                    case "namesFontSize":
                        ((RemoteScoreboard) s).namesFontSize = this.value;
                        break;
                }

                break;
            }
        }
    }
}
