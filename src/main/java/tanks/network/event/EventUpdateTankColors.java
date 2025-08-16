package tanks.network.event;

import basewindow.Color;
import io.netty.buffer.ByteBuf;
import tanks.Player;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.network.ConnectedPlayer;
import tanks.network.NetworkUtils;

import java.util.UUID;

public class EventUpdateTankColors extends PersonalEvent
{
    public UUID player;

    public Color color1 = new Color();
    public Color color2 = new Color();
    public Color color3 = new Color();

    public EventUpdateTankColors()
    {

    }

    public EventUpdateTankColors(Player p)
    {
        this.player = p.clientID;

        this.color1.set(p.color);
        this.color2.set(p.color2);
        this.color3.set(p.color3);
    }

    @Override
    public void write(ByteBuf b)
    {
        NetworkUtils.writeString(b, this.player.toString());
        NetworkUtils.writeColor(b, this.color1);
        NetworkUtils.writeColor(b, this.color2);
        NetworkUtils.writeColor(b, this.color3);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.player = UUID.fromString(NetworkUtils.readString(b));
        NetworkUtils.readColor(b, this.color1);
        NetworkUtils.readColor(b, this.color2);
        NetworkUtils.readColor(b, this.color3);
    }

    @Override
    public void execute()
    {
        if (this.clientID == null)
        {
            synchronized (ScreenPartyLobby.connections)
            {
                for (ConnectedPlayer p: ScreenPartyLobby.connections)
                {
                    if (p.clientId.equals(this.player))
                    {
                        p.color.set(this.color1);
                        p.color2.set(this.color2);
                        p.color3.set(this.color3);
                    }
                }
            }
        }
    }
}
