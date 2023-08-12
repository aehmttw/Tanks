package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Player;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.network.ConnectedPlayer;
import tanks.network.NetworkUtils;

import java.util.UUID;

public class EventUpdateTankColors extends PersonalEvent
{
    public UUID player;

    public int colorR;
    public int colorG;
    public int colorB;

    public int colorR2;
    public int colorG2;
    public int colorB2;

    public EventUpdateTankColors()
    {

    }

    public EventUpdateTankColors(Player p)
    {
        this.player = p.clientID;

        this.colorR = p.colorR;
        this.colorG = p.colorG;
        this.colorB = p.colorB;

        this.colorR2 = p.turretColorR;
        this.colorG2 = p.turretColorG;
        this.colorB2 = p.turretColorB;
    }

    @Override
    public void write(ByteBuf b)
    {
        NetworkUtils.writeString(b, this.player.toString());
        b.writeInt(this.colorR);
        b.writeInt(this.colorG);
        b.writeInt(this.colorB);
        b.writeInt(this.colorR2);
        b.writeInt(this.colorG2);
        b.writeInt(this.colorB2);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.player = UUID.fromString(NetworkUtils.readString(b));
        this.colorR = b.readInt();
        this.colorG = b.readInt();
        this.colorB = b.readInt();
        this.colorR2 = b.readInt();
        this.colorG2 = b.readInt();
        this.colorB2 = b.readInt();
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
                        p.colorR = this.colorR;
                        p.colorG = this.colorG;
                        p.colorB = this.colorB;
                        p.colorR2 = this.colorR2;
                        p.colorG2 = this.colorG2;
                        p.colorB2 = this.colorB2;
                    }
                }
            }
        }
    }
}
