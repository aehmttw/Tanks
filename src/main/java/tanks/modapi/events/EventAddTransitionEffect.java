package tanks.modapi.events;

import io.netty.buffer.ByteBuf;
import tanks.event.PersonalEvent;
import tanks.modapi.ModAPI;
import tanks.modapi.menus.TransitionEffect;
import tanks.network.NetworkUtils;

public class EventAddTransitionEffect extends PersonalEvent
{
    public TransitionEffect.types type;
    public float speed;
    public int colorR;
    public int colorB;
    public int colorG;

    public EventAddTransitionEffect()
    {

    }

    public EventAddTransitionEffect(TransitionEffect.types type, float speed, int colR, int colG, int colB)
    {
        this.type = type;
        this.speed = speed;
        this.colorR = colR;
        this.colorG = colG;
        this.colorB = colB;
    }

    @Override
    public void write(ByteBuf b)
    {
        NetworkUtils.writeString(b, this.type.toString());
        b.writeFloat(this.speed);
        b.writeInt(this.colorR);
        b.writeInt(this.colorG);
        b.writeInt(this.colorB);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.type = TransitionEffect.types.valueOf(NetworkUtils.readString(b));
        this.speed = b.readFloat();
        this.colorR = b.readInt();
        this.colorG = b.readInt();
        this.colorB = b.readInt();
    }

    @Override
    public void execute()
    {
        ModAPI.menuGroup.add(new TransitionEffect(this.type, this.speed, this.colorR, this.colorG, this.colorB));
    }
}
