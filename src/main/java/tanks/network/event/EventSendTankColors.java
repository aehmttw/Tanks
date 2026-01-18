package tanks.network.event;

import basewindow.Color;
import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Player;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.network.NetworkUtils;

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
    public void write(ByteBuf b)
    {
        NetworkUtils.writeColor(b, this.color1);
        NetworkUtils.writeColor(b, this.color2);
        NetworkUtils.writeColor(b, this.color3);
    }

    @Override
    public void read(ByteBuf b)
    {
        NetworkUtils.readColor(b, this.color1);
        NetworkUtils.readColor(b, this.color2);
        NetworkUtils.readColor(b, this.color3);
    }

    @Override
    public void execute()
    {
        if (this.clientID != null)
        {
            synchronized (ScreenPartyLobby.connections)
            {
                for (Player p: Game.players)
                {
                    if (p.clientID.equals(this.clientID))
                    {
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
