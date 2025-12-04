package tanks.network.event;

import tanks.*;

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
    public void execute()
    {
        Game.movables.add(new AreaEffectFreeze(posX, posY));
    }
}
