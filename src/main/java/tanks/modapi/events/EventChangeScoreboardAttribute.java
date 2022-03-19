package tanks.modapi.events;

import io.netty.buffer.ByteBuf;
import tanks.event.PersonalEvent;
import tanks.modapi.ModAPI;
import tanks.modapi.menus.FixedMenu;
import tanks.modapi.menus.RemoteScoreboard;
import tanks.network.NetworkUtils;

public class EventChangeScoreboardAttribute extends PersonalEvent {
    public int id;
    public String attributeName;
    public Double value;

    public EventChangeScoreboardAttribute() {

    }

    public EventChangeScoreboardAttribute(int id, String attributeName, Double value) {
        this.id = id;
        this.attributeName = attributeName;
        this.value = value;
    }

    @Override
    public void write(ByteBuf b) {
        b.writeInt(this.id);
        NetworkUtils.writeString(b, this.attributeName);
        b.writeDouble(this.value);
    }

    @Override
    public void read(ByteBuf b) {
        this.id = b.readInt();
        this.attributeName = NetworkUtils.readString(b);
        this.value = b.readDouble();
    }

    @Override
    public void execute() {
        for (FixedMenu s : ModAPI.menuGroup)
        {
            if (s instanceof RemoteScoreboard && ((RemoteScoreboard) s).id == this.id)
            {
                RemoteScoreboard r = (RemoteScoreboard) s;

                switch (this.attributeName) {
                    case "titleColR":
                        r.titleColR = this.value;
                        break;
                    case "titleColG":
                        r.titleColG = this.value;
                        break;
                    case "titleColB":
                        r.titleColB = this.value;
                        break;
                    case "titleFontSize":
                        r.titleFontSize = this.value;
                        break;
                    case "namesFontSize":
                        r.namesFontSize = this.value;
                        break;
                }

                break;
            }
        }
    }
}
