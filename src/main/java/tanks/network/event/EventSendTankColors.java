package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Player;

public class EventSendTankColors extends PersonalEvent
{
    public int colorR;
    public int colorG;
    public int colorB;

    public int colorR2;
    public int colorG2;
    public int colorB2;

    public EventSendTankColors()
    {

    }

    public EventSendTankColors(Player p)
    {
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
        if (this.clientID != null)
        {
            synchronized (Game.players)
            {
                for (Player p: Game.players)
                {
                    if (p.clientID.equals(this.clientID))
                    {
                        p.colorR = this.colorR;
                        p.colorG = this.colorG;
                        p.colorB = this.colorB;
                        p.turretColorR = this.colorR2;
                        p.turretColorG = this.colorG2;
                        p.turretColorB = this.colorB2;
                    }
                }
            }
        }
    }
}
