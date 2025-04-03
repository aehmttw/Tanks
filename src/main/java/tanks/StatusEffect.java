package tanks;

import tanks.minigames.Arcade;

import java.util.HashMap;

public class StatusEffect
{
    public AttributeModifier[] attributeModifiers;
    public String name;
    public String family;

    public static HashMap<String, StatusEffect> statusEffectRegistry = new HashMap<>();

    public static final StatusEffect ice = new StatusEffect("ice", new AttributeModifier[]
    {
        new AttributeModifier("ice_accel", AttributeModifier.acceleration, AttributeModifier.Operation.multiply, 5),
                new AttributeModifier("ice_slip", AttributeModifier.friction, AttributeModifier.Operation.multiply, -0.875),
                new AttributeModifier("ice_max_speed", AttributeModifier.max_speed, AttributeModifier.Operation.multiply, 8)
    });

    public static final StatusEffect snow_velocity = new StatusEffect("snow_velocity", new AttributeModifier[]
    {
        new AttributeModifier("snow_velocity", AttributeModifier.velocity, AttributeModifier.Operation.multiply, -0.25),
    });

    public static final StatusEffect snow_friction = new StatusEffect("snow_friction", new AttributeModifier[]
    {
        new AttributeModifier("snow_friction", AttributeModifier.friction, AttributeModifier.Operation.multiply, 4)
    });


    public static final StatusEffect mud = new StatusEffect("mud", new AttributeModifier[]
    {
        new AttributeModifier("mud", AttributeModifier.velocity, AttributeModifier.Operation.multiply, -0.5)
    });


    public static final StatusEffect boost_tank = new StatusEffect("boost_tank", new AttributeModifier[]
    {
        new AttributeModifier("boost_speed", AttributeModifier.velocity, AttributeModifier.Operation.multiply, 3),
            new AttributeModifier("boost_glow", AttributeModifier.glow, AttributeModifier.Operation.multiply, 1),
            new AttributeModifier("boost_slip", AttributeModifier.friction, AttributeModifier.Operation.multiply, -0.75),
            new AttributeModifier("boost_accel", AttributeModifier.acceleration, AttributeModifier.Operation.multiply, -2),
            new AttributeModifier("boost_effect", AttributeModifier.ember_effect, AttributeModifier.Operation.add, 1)
    });

    public static final StatusEffect boost_bullet = new StatusEffect("boost_bullet", new AttributeModifier[]
            {
                    new AttributeModifier("boost_speed", AttributeModifier.velocity, AttributeModifier.Operation.multiply, -4),
                    new AttributeModifier("boost_glow", AttributeModifier.glow, AttributeModifier.Operation.multiply, 1)
            });

    public static final StatusEffect[] arcade_rampage = createArcadeRampage();

    public static StatusEffect[] createArcadeRampage()
    {
        StatusEffect[] s = new StatusEffect[Arcade.max_power];

        for (int i = 0; i < Arcade.max_power; i++)
        {
            s[i] = new StatusEffect("rampage", "rampage_" + (i + 1), new AttributeModifier[]
                    {
                            new AttributeModifier("rampage_speed", AttributeModifier.velocity, AttributeModifier.Operation.multiply, -(i + 1) / 5.0),
                            new AttributeModifier("rampage_glow", AttributeModifier.glow, AttributeModifier.Operation.multiply, -(i + 1) / 5.0),
                            new AttributeModifier("rampage_reload", AttributeModifier.reload, AttributeModifier.Operation.multiply, -(i + 1) / 5.0),
                            new AttributeModifier("rampage_recoil", AttributeModifier.recoil, AttributeModifier.Operation.multiply, -1.0 / (1 + (i + 1) / 5.0) - 1),
                            new AttributeModifier("rampage_bullet_speed", AttributeModifier.bullet_speed, AttributeModifier.Operation.multiply, -(i + 1) / 5.0)
                    });
        }

        return s;
    }

    public static class Instance
    {
        public StatusEffect effect;
        public double age;

        /**Duration of the Attribute Modifier, leave at 0 for indefinite duration*/
        public double duration = 0;

        /**Age at which the Attribute starts to wear off*/
        public double deteriorationAge = 0;

        /**Age at which the Attribute is at full strength*/
        public double warmupAge = 0;

        public Instance(StatusEffect effect, double age, double warmupAge, double deteriorationAge, double maxAge)
        {
            this.effect = effect;
            this.age = age;
            this.deteriorationAge = deteriorationAge;
            this.duration = maxAge;
            this.warmupAge = warmupAge;
        }

        public double getValue(double in, AttributeModifier.Type type)
        {
            return this.effect.getValue(in, age, type, warmupAge, deteriorationAge, duration);
        }
    }

    public StatusEffect(String name, AttributeModifier[] modifiers)
    {
        this(null, name, modifiers);
    }

    public StatusEffect(String family, String name, AttributeModifier[] modifiers)
    {
        this.family = family;
        this.attributeModifiers = modifiers;
        this.name = name;

        statusEffectRegistry.put(name, this);
    }

    public double getValue(double in, double age, AttributeModifier.Type type, double warmupAge, double deteriorationAge, double duration)
    {
        for (AttributeModifier a: this.attributeModifiers)
        {
            if (a.type.equals(type))
            {
                if (age >= duration && duration > 0)
                    return in;

                double val;
                if (age < warmupAge)
                    val = a.value * age / warmupAge;
                else if (age < deteriorationAge || deteriorationAge <= 0)
                    val = a.value;
                else
                    val = a.value * (duration - age) / (duration - deteriorationAge);

                if (a.effect == AttributeModifier.Operation.add)
                    return in + val;
                else if (a.effect == AttributeModifier.Operation.multiply)
                    return in * (val + 1);
                else
                    return in;
            }
        }

        return in;
    }
}
