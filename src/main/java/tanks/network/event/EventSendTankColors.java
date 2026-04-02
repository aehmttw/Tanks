package tanks.network.event;

import basewindow.Color;
import tanks.*;
import tanks.gui.screen.ScreenPartyHost;
import tanks.network.ServerHandler;

public class EventSendTankColors extends PersonalEvent
{
    public Color color1 = new Color();
    public Color color2 = new Color();
    public Color color3 = new Color();

    public EventSendTankColors()
    {

    }

    public EventSendTankColors(Player p)
    {
        this.color1.set(p.color);
        this.color2.set(p.color2);
        this.color3.set(p.color3);
    }

    @Override
    public void execute()
    {
        if (this.clientID != null)
        {
            synchronized (ScreenPartyHost.server.connections)
            {
                for (ServerHandler sh: ScreenPartyHost.server.connections)
                {
                    if (sh.clientID.equals(this.clientID) && sh.player != null)
                    {
                        Player p = sh.player;
                        p.color.set(this.color1);
                        p.color2.set(this.color2);
                        p.color3.set(this.color3);
                        Game.eventsOut.add(new EventUpdateTankColors(p));
                    }
                }
            }
        }
    }
}
