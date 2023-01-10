package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Movable;
import tanks.StatusEffect;
import tanks.bullet.Bullet;
import tanks.network.NetworkUtils;
import tanks.tank.Tank;

public class EventStatusEffectEnd extends PersonalEvent
{
    public boolean isTank;
    public int networkID;
    public String effect;

    public EventStatusEffectEnd(Movable m, StatusEffect s)
    {
        this.effect = s.name;

        if (m instanceof Tank)
        {
            isTank = true;
            networkID = ((Tank) m).networkID;
        }
        else
        {
            isTank = false;
            networkID = ((Bullet) m).networkID;
        }
    }

    public EventStatusEffectEnd()
    {

    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeBoolean(this.isTank);
        b.writeInt(this.networkID);
        NetworkUtils.writeString(b, this.effect);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.isTank = b.readBoolean();
        this.networkID = b.readInt();
        this.effect = NetworkUtils.readString(b);
    }

    @Override
    public void execute()
    {
        if (clientID != null)
            return;

        Movable m;

        if (isTank)
            m = Tank.idMap.get(this.networkID);
        else
            m = Bullet.idMap.get(this.networkID);

        if (m != null)
            m.statusEffects.remove(StatusEffect.statusEffectRegistry.get(this.effect));
    }
}
