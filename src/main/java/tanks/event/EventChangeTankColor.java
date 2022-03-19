package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Player;
import tanks.network.NetworkUtils;
import tanks.tank.Turret;

import java.util.UUID;

public class EventChangeTankColor extends PersonalEvent
{
    public UUID id;

    public int colorR;
    public int colorG;
    public int colorB;

    public int turretR;
    public int turretG;
    public int turretB;

    public EventChangeTankColor() {}

    public EventChangeTankColor(UUID id, Player p)
    {
        this.id = id;

        this.colorR = p.colorR;
        this.colorG = p.colorG;
        this.colorB = p.colorB;

        if (Game.player.enableSecondaryColor)
        {
            this.turretR = p.turretColorR;
            this.turretG = p.turretColorG;
            this.turretB = p.turretColorB;
        }
        else
        {
            this.turretR = (int) Turret.calculateSecondaryColor(p.colorR);
            this.turretG = (int) Turret.calculateSecondaryColor(p.colorG);
            this.turretB = (int) Turret.calculateSecondaryColor(p.colorB);
        }
    }

    @Override
    public void execute()
    {
        if (this.id == null)
            return;

        for (Player p : Game.players)
        {
            if (p.clientID.equals(this.id))
            {
                p.colorR = this.colorR;
                p.colorG = this.colorG;
                p.colorB = this.colorB;

                p.turretColorR = this.turretR;
                p.turretColorG = this.turretG;
                p.turretColorB = this.turretB;

                break;
            }
        }
    }

    @Override
    public void write(ByteBuf b)
    {
        NetworkUtils.writeString(b, this.id.toString());

        b.writeInt(this.colorR);
        b.writeInt(this.colorG);
        b.writeInt(this.colorB);

        b.writeInt(this.turretR);
        b.writeInt(this.turretG);
        b.writeInt(this.turretB);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.id = UUID.fromString(NetworkUtils.readString(b));

        this.colorR = b.readInt();
        this.colorG = b.readInt();
        this.colorB = b.readInt();

        this.turretR = b.readInt();
        this.turretG = b.readInt();
        this.turretB = b.readInt();
    }
}
