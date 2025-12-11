package tanks.network.event;

import tanks.*;
import tanks.tank.Tank;

public class EventBulletStunEffect extends PersonalEvent
{
    public int tank;
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
                e.setColor(0, 255, 255);
                e.setGlowColor(e.color, 127);
                e.maxAge *= length;
                Game.effects.add(e);
            }
        }
    }
}
