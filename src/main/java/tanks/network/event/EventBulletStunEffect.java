package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Effect;
import tanks.Game;

public class EventBulletStunEffect extends PersonalEvent
{
    public double posX;
    public double posY;
    public double posZ;
    public double length;

    public EventBulletStunEffect()
    {

    }

    public EventBulletStunEffect(double x, double y, double z, double length)
    {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.length = length;
    }

    @Override
    public void execute()
    {
        if (Game.effectsEnabled && this.clientID == null)
        {
            for (int i = 0; i < 25 * Game.effectMultiplier; i++)
            {
                Effect e = Effect.createNewEffect(this.posX, this.posY, this.posZ, Effect.EffectType.stun);
                double var = 50;
                e.colR = Math.min(255, Math.max(0, 0 + Math.random() * var - var / 2));
                e.colG = Math.min(255, Math.max(0, 255 + Math.random() * var - var / 2));
                e.colB = Math.min(255, Math.max(0, 255 + Math.random() * var - var / 2));
                e.glowR = 0;
                e.glowG = 128;
                e.glowB = 128;
                e.maxAge *= length;
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
        b.writeDouble(this.length);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.posX = b.readDouble();
        this.posY = b.readDouble();
        this.posZ = b.readDouble();
        this.length = b.readDouble();
    }
}
