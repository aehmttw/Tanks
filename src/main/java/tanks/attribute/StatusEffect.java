package tanks.attribute;

import tanks.minigames.Arcade;

import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class StatusEffect
{
    public AttributeModifier[] attributeModifiers;
    public String name;
    public String family;

    public static HashMap<String, StatusEffect> statusEffectRegistry = new HashMap<>();

    public static final StatusEffect ice = new StatusEffect("ice", new AttributeModifier[]
    {
        AttributeModifier.newInstance("ice_accel", AttributeModifier.acceleration, AttributeModifier.Operation.multiply, -0.75),
                AttributeModifier.newInstance("ice_slip", AttributeModifier.friction, AttributeModifier.Operation.multiply, -0.875),
                AttributeModifier.newInstance("ice_max_speed", AttributeModifier.max_speed, AttributeModifier.Operation.multiply, 3)
    });

    public static final StatusEffect snow_velocity = new StatusEffect("snow_velocity", new AttributeModifier[]
    {
        AttributeModifier.newInstance("snow_velocity", AttributeModifier.velocity, AttributeModifier.Operation.multiply, -0.25),
    });

    public static final StatusEffect snow_friction = new StatusEffect("snow_friction", new AttributeModifier[]
    {
        AttributeModifier.newInstance("snow_friction", AttributeModifier.friction, AttributeModifier.Operation.multiply, 4)
    });


    public static final StatusEffect mud = new StatusEffect("mud", new AttributeModifier[]
    {
        AttributeModifier.newInstance("mud", AttributeModifier.velocity, AttributeModifier.Operation.multiply, -0.5)
    });


    public static final StatusEffect boost_tank = new StatusEffect("boost_tank", new AttributeModifier[]
    {
        AttributeModifier.newInstance("boost_speed", AttributeModifier.velocity, AttributeModifier.Operation.multiply, 3),
                AttributeModifier.newInstance("boost_glow", AttributeModifier.glow, AttributeModifier.Operation.multiply, 1),
                AttributeModifier.newInstance("boost_slip", AttributeModifier.friction, AttributeModifier.Operation.multiply, -0.75),
                AttributeModifier.newInstance("boost_effect", AttributeModifier.ember_effect, AttributeModifier.Operation.add, 1)
    });

    public static final StatusEffect boost_bullet = new StatusEffect("boost_bullet", new AttributeModifier[]
            {
                    AttributeModifier.newInstance("boost_speed", AttributeModifier.velocity, AttributeModifier.Operation.multiply, 1),
                    AttributeModifier.newInstance("boost_glow", AttributeModifier.glow, AttributeModifier.Operation.multiply, 1)
            });

    public static final StatusEffect[] arcade_rampage = createArcadeRampage();

    public static StatusEffect[] createArcadeRampage()
    {
        StatusEffect[] s = new StatusEffect[Arcade.max_power];

        for (int i = 0; i < Arcade.max_power; i++)
        {
            s[i] = new StatusEffect("rampage", "rampage_" + (i + 1), new AttributeModifier[]
                    {
                            AttributeModifier.newInstance("rampage_speed", AttributeModifier.velocity, AttributeModifier.Operation.multiply, (i + 1) / 5.0),
                            AttributeModifier.newInstance("rampage_glow", AttributeModifier.glow, AttributeModifier.Operation.multiply, (i + 1) / 5.0),
                            AttributeModifier.newInstance("rampage_reload", AttributeModifier.reload, AttributeModifier.Operation.multiply, (i + 1) / 5.0),
                            AttributeModifier.newInstance("rampage_recoil", AttributeModifier.recoil, AttributeModifier.Operation.multiply, 1.0 / (1 + (i + 1) / 5.0) - 1),
                            AttributeModifier.newInstance("rampage_bullet_speed", AttributeModifier.bullet_speed, AttributeModifier.Operation.multiply, (i + 1) / 5.0)
                    });
        }

        return s;
    }

    public static class Instance
    {
        // Object recycling system
        private static final Queue<Instance> recycleQueue = new ConcurrentLinkedQueue<>();
        private static final int MAX_POOL_SIZE = 1000; // Prevent memory leaks from excessive pooling

        public StatusEffect effect;
        public double age;

        /**Duration of the Attribute Modifier, leave at 0 for indefinite duration*/
        public double duration;

        /**Age at which the Attribute starts to wear off*/
        public double deteriorationAge;

        /**Age at which the Attribute is at full strength*/
        public double warmupAge;

        private Instance() {}

        public StatusEffect.Instance set(StatusEffect effect, double age, double warmupAge, double deteriorationAge, double maxAge)
        {
            this.effect = effect;
            this.age = age;
            this.deteriorationAge = deteriorationAge;
            this.duration = maxAge;
            this.warmupAge = warmupAge;
            return this;
        }

        public static Instance newInstance(StatusEffect effect, double age, double warmupAge, double deteriorationAge, double maxAge)
        {
            Instance instance = recycleQueue.poll();
            if (instance != null)
                return instance.set(effect, age, warmupAge, deteriorationAge, maxAge);
            return new Instance().set(effect, age, warmupAge, deteriorationAge, maxAge);
        }

        public static void recycle(Instance instance)
        {
            if (instance != null && recycleQueue.size() < MAX_POOL_SIZE)
            {
                instance.reset();
                recycleQueue.offer(instance);
            }
        }

        /**
         * Reset this Instance to default state for reuse
         */
        private void reset()
        {
            this.effect = null;
            this.age = 0;
            this.duration = 0;
            this.deteriorationAge = 0;
            this.warmupAge = 0;
        }

        public double getValue(double in, AttributeModifier.Type type)
        {
            for (AttributeModifier a: effect.attributeModifiers)
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
}
