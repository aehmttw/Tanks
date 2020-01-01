package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.AreaEffectFreeze;
import tanks.Game;

public class EventCreateFreezeEffect extends PersonalEvent
{
    public double posX;
    public double posY;

    public EventCreateFreezeEffect()
    {

    }

    public EventCreateFreezeEffect(AreaEffectFreeze a)
    {
        this.posX = a.posX;
        this.posY = a.posY;
    }


    @Override
    public void write(ByteBuf b)
    {
        b.writeDouble(posX);
        b.writeDouble(posY);
    }

    @Override
    public void read(ByteBuf b)
    {
        posX = b.readDouble();
        posY = b.readDouble();
    }

    @Override
    public void execute()
    {
        Game.movables.add(new AreaEffectFreeze(posX, posY));
    }
}
