package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.AttributeModifier;
import tanks.network.NetworkUtils;
import tanks.tank.Tank;

public class EventTankAddAttributeModifier extends PersonalEvent
{
    public int tank;
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

    public EventTankAddAttributeModifier()
    {

    }

    public EventTankAddAttributeModifier(Tank t, AttributeModifier m, boolean unduplicate)
    {
        this.tank = t.networkID;

        this.name = m.name;
        this.duration = m.duration;
        this.deteriorationAge = m.deteriorationAge;
        this.warmupAge = m.warmupAge;
        this.value = m.value;
        this.effect = m.effect.toString();
        this.age = m.age;
        this.type = m.type;
        this.expired = m.expired;

        this.unduplicate = unduplicate;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(this.tank);
        NetworkUtils.writeString(b, this.name);
        b.writeDouble(this.duration);
        b.writeDouble(this.deteriorationAge);
        b.writeDouble(this.warmupAge);
        b.writeDouble(this.value);
        NetworkUtils.writeString(b, this.effect);
        b.writeDouble(this.age);
        NetworkUtils.writeString(b, this.type);
        b.writeBoolean(this.expired);
        b.writeBoolean(this.unduplicate);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.tank = b.readInt();
        this.name = NetworkUtils.readString(b);
        this.duration = b.readDouble();
        this.deteriorationAge = b.readDouble();
        this.warmupAge = b.readDouble();
        this.value = b.readDouble();
        this.effect = NetworkUtils.readString(b);
        this.age = b.readDouble();
        this.type = NetworkUtils.readString(b);
        this.expired = b.readBoolean();
        this.unduplicate = b.readBoolean();
    }

    @Override
    public void execute()
    {
        Tank t = Tank.idMap.get(this.tank);

        if (t != null && this.clientID == null)
        {
            AttributeModifier.Operation o = AttributeModifier.Operation.add;

            if (this.effect.equals("multiply"))
                o = AttributeModifier.Operation.multiply;

            AttributeModifier m = new AttributeModifier(this.name, this.type, o, this.value);

            m.duration = this.duration;
            m.deteriorationAge = this.deteriorationAge;
            m.warmupAge = this.warmupAge;
            m.age = this.age;
            m.expired = this.expired;

            if (unduplicate)
                t.addUnduplicateAttribute(m);
            else
                t.addAttribute(m);
        }
    }

}
