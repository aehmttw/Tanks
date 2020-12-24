package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Effect;
import tanks.Game;

public class EventBulletElectricStunEffect extends PersonalEvent
{
    public double posX;
    public double posY;
    public double posZ;

    public EventBulletElectricStunEffect()
    {

    }

    public EventBulletElectricStunEffect(double x, double y, double z)
    {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
    }

    @Override
    public void execute()
    {
        if (Game.fancyGraphics && this.clientID == null)
        {
            for (int i = 0; i < 25; i++)
            {
                Effect e = Effect.createNewEffect(this.posX, this.posY, this.posZ, Effect.EffectType.stun);
                double var = 50;
                e.colR = Math.min(255, Math.max(0, 0 + Math.random() * var - var / 2));
                e.colG = Math.min(255, Math.max(0, 255 + Math.random() * var - var / 2));
                e.colB = Math.min(255, Math.max(0, 255 + Math.random() * var - var / 2));
                e.glowR = 0;
                e.glowG = 128;
                e.glowB = 128;
                Game.effects.add(e);
            }
        }
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeDouble(this.posX);
        b.writeDouble(this.posY);
        b.writeDouble(this.posZ);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.posX = b.readDouble();
        this.posY = b.readDouble();
        this.posZ = b.readDouble();
    }
}
