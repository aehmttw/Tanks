package tanks.network.event;

import tanks.bullet.Bullet;
import tanks.effect.AttributeModifier;

public class EventBulletAddAttributeModifier extends PersonalEvent
{
    public int bullet;
    public String name;
    public double duration = 0;
    public double deteriorationAge = 0;
    public double warmupAge = 0;
    public double value;
    public String effect;
    public double age;
    public String type;
    public boolean expired;

    public boolean unduplicate;

    public EventBulletAddAttributeModifier()
    {

    }

    public EventBulletAddAttributeModifier(Bullet b, AttributeModifier m, boolean unduplicate)
    {
        this.bullet = b.networkID;

        this.name = m.name;
        this.duration = m.duration;
        this.deteriorationAge = m.deteriorationAge;
        this.warmupAge = m.warmupAge;
        this.value = m.value;
        this.effect = m.effect.toString();
        this.age = m.age;
        this.type = m.type.name;
        this.expired = m.expired;

        this.unduplicate = unduplicate;
    }

    @Override
    public void execute()
    {
        Bullet b = Bullet.idMap.get(this.bullet);

        if (b != null && this.clientID == null)
        {
            AttributeModifier.Operation o = AttributeModifier.Operation.add;

            if (this.effect.equals("multiply"))
                o = AttributeModifier.Operation.multiply;

            AttributeModifier m = AttributeModifier.newInstance(this.name, AttributeModifier.attributeModifierTypes.get(this.type), o, this.value);

            m.duration = this.duration;
            m.deteriorationAge = this.deteriorationAge;
            m.warmupAge = this.warmupAge;
            m.age = this.age;
            m.expired = this.expired;

            if (unduplicate)
                b.em().addUnduplicateAttribute(m);
            else
                b.em().addAttribute(m);
        }
    }

}
