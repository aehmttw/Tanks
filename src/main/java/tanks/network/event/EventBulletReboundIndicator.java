package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.bullet.BulletReboundIndicator;

public class EventBulletReboundIndicator extends PersonalEvent
{
    public BulletReboundIndicator indicator;

    public EventBulletReboundIndicator()
    {

    }

    public EventBulletReboundIndicator(BulletReboundIndicator b)
    {
        this.indicator = b;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeDouble(indicator.posX);
        b.writeDouble(indicator.posY);
        b.writeDouble(indicator.posZ);
        b.writeDouble(indicator.size);
        b.writeDouble(indicator.maxAge);
        b.writeDouble(indicator.colorR);
        b.writeDouble(indicator.colorG);
        b.writeDouble(indicator.colorB);
        b.writeDouble(indicator.colorR2);
        b.writeDouble(indicator.colorG2);
        b.writeDouble(indicator.colorB2);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.indicator = new BulletReboundIndicator(
                b.readDouble(),
                b.readDouble(),
                b.readDouble(),
                b.readDouble(),
                b.readDouble(),
                b.readDouble(),
                b.readDouble(),
                b.readDouble(),
                b.readDouble(),
                b.readDouble(),
                b.readDouble());
    }

    @Override
    public void execute()
    {
        if (this.clientID == null)
            Game.movables.add(indicator);
    }
}
