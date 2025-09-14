package tanks.network.event;

import tanks.Movable;
import tanks.attribute.StatusEffect;
import tanks.bullet.Bullet;
import tanks.tank.Tank;

public class EventStatusEffectBegin extends PersonalEvent
{
    public boolean isTank;
    public int networkID;
    public String effect;
    public double age;
    public double warmup;

    public EventStatusEffectBegin(Movable m, StatusEffect s, double age, double warmup)
    {
        this.effect = s.name;
        this.age = age;
        this.warmup = warmup;

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

    public EventStatusEffectBegin()
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
            m.em().addStatusEffect(StatusEffect.statusEffectRegistry.get(this.effect), age, warmup, 0, 0);
    }
}
