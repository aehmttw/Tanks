package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Effect;
import tanks.Game;
import tanks.tank.Tank;

public class EventTankLightPinkAngry extends PersonalEvent
{
    public int tank;

    public EventTankLightPinkAngry()
    {

    }

    public EventTankLightPinkAngry(int tank)
    {
        this.tank = tank;
    }

    @Override
    public void execute()
    {
        if (this.clientID != null)
            return;

        Tank t = Tank.idMap.get(this.tank);

        if (t == null)
            return;

        Effect e1 = Effect.createNewEffect(t.posX, t.posY, t.posZ + t.size * 0.75, Effect.EffectType.exclamation);
        e1.size = t.size;
        Game.effects.add(e1);
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(this.tank);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.tank = b.readInt();
    }
}
