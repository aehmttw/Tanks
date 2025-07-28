package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Effect;
import tanks.Game;
import tanks.tank.Tank;

public class EventBulletStunEffect extends PersonalEvent
{
    int tank;
    public double length;

    public EventBulletStunEffect()
    {

    }

    public EventBulletStunEffect(Tank t, double length)
    {
        this.tank = t.networkID;
        this.length = length;
    }

    @Override
    public void execute()
    {
        if (Game.effectsEnabled && this.clientID == null)
        {
            Tank t = Tank.idMap.get(tank);
            if (t == null)
                return;

            for (int i = 0; i < 25 * Game.effectMultiplier; i++)
            {
                Effect e = Effect.createNewEffect(t.posX, t.posY, Game.tile_size / 4, Effect.EffectType.stun);
                e.linkedMovable = t;
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
        b.writeInt(this.tank);
        b.writeDouble(this.length);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.tank = b.readInt();
        this.length = b.readDouble();
    }
}
