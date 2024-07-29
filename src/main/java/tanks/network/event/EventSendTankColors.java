package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Player;
import tanks.network.NetworkUtils;

import java.util.UUID;

public class EventSendTankColors extends PersonalEvent
{
    public int colorR;
    public int colorG;
    public int colorB;

    public int colorR2;
    public int colorG2;
    public int colorB2;

    public int colorR3;
    public int colorG3;
    public int colorB3;

    public EventSendTankColors()
    {

    }

    public EventSendTankColors(Player p)
    {
        this.colorR = p.colorR;
        this.colorG = p.colorG;
        this.colorB = p.colorB;

        this.colorR2 = p.colorR2;
        this.colorG2 = p.colorG2;
        this.colorB2 = p.colorB2;

        this.colorR3 = p.colorR3;
        this.colorG3 = p.colorG3;
        this.colorB3 = p.colorB3;
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
        b.writeInt(this.colorR3);
        b.writeInt(this.colorG3);
        b.writeInt(this.colorB3);
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
        this.colorR3 = b.readInt();
        this.colorG3 = b.readInt();
        this.colorB3 = b.readInt();
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
                        p.colorR2 = this.colorR2;
                        p.colorG2 = this.colorG2;
                        p.colorB2 = this.colorB2;
                        p.colorR3 = this.colorR3;
                        p.colorG3 = this.colorG3;
                        p.colorB3 = this.colorB3;
                        Game.eventsOut.add(new EventUpdateTankColors(p));
                    }
                }
            }
        }
    }
}
