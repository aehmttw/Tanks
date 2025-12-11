package tanks.network.event;

import tanks.Movable;
import tanks.attribute.StatusEffect;
import tanks.bullet.Bullet;
import tanks.tank.Tank;

public class EventStatusEffectDeteriorate extends PersonalEvent
{
    public boolean isTank;
    public int networkID;
    public String effect;
    public double remainingTime;

    public EventStatusEffectDeteriorate(Movable m, StatusEffect s, double remainingTime)
    {
        this.effect = s.name;
        this.remainingTime = remainingTime;

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

    public EventStatusEffectDeteriorate()
    {

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
            m.em().addStatusEffect(StatusEffect.statusEffectRegistry.get(this.effect), 1, 0, 1, remainingTime + 1);
    }
}
