package tanks.effect;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import tanks.BiConsumer;
import tanks.Game;
import tanks.Movable;
import tanks.Panel;
import tanks.bullet.Bullet;
import tanks.gui.screen.ScreenPartyHost;
import tanks.network.event.EventStatusEffectBegin;
import tanks.network.event.EventStatusEffectDeteriorate;
import tanks.network.event.EventStatusEffectEnd;
import tanks.tank.Tank;

import java.util.Collections;
import java.util.HashSet;

@SuppressWarnings("ForLoopReplaceableByForEach")
public class EffectManager
{
    // Parallel array for status effects
    public ObjectArrayList<StatusEffectProperty> statusEffectProperties = new ObjectArrayList<>();

    public Movable movable;

    public BiConsumer<AttributeModifier, Boolean> addAttributeCallback = (a, b) -> {};

    public HashSet<String> attributeImmunities = new HashSet<>();
    public ObjectArrayList<AttributeModifier> attributes = new ObjectArrayList<>();

    /**
     * Linear search function to find status effect property by status effect reference
     */
    private StatusEffectProperty findStatusEffectProperty(StatusEffect statusEffect)
    {
        for (int i = 0; i < statusEffectProperties.size(); i++)
        {
            StatusEffectProperty prop = statusEffectProperties.get(i);
            if (prop.statusEffect == statusEffect)
                return prop;
        }
        return null;
    }
    public ObjectArrayList<StatusEffect> removeStatusEffects = new ObjectArrayList<>();
    public ObjectArrayList<AttributeModifier> removeAttributes = new ObjectArrayList<>();

    public EffectManager(Movable m)
    {
        this.movable = m;
    }

    public StatusEffect findEffect(String name)
    {
        for (int i = 0; i < statusEffectProperties.size(); i++)
        {
            StatusEffectProperty prop = statusEffectProperties.get(i);
            if (prop.statusEffect.name.equals(name))
                return prop.statusEffect;
        }
        return null;
    }

    public int indexOf(StatusEffect statusEffect)
    {
        for (int i = 0; i < statusEffectProperties.size(); i++)
        {
            StatusEffectProperty prop = statusEffectProperties.get(i);
            if (prop.statusEffect == statusEffect)
                return i;
        }
        return -1;
    }

    public boolean contains(StatusEffect statusEffect)
    {
        return indexOf(statusEffect) >= 0;
    }

    public void addStatusEffect(StatusEffect s, double age, double warmup, double deterioration, double duration)
    {
        if (deterioration > duration)
            throw new RuntimeException("Deterioration age > duration");

        StatusEffect prevEffect = null;
        for (int i = 0; i < statusEffectProperties.size(); i++)
        {
            StatusEffectProperty prop = statusEffectProperties.get(i);
            if (prop.statusEffect.family != null && prop.statusEffect.family.equals(s.family))
                prevEffect = prop.statusEffect;
        }

        if (prevEffect != null)
        {
            int prevIndex = indexOf(prevEffect);
            if (prevIndex >= 0)
            {
                StatusEffectProperty prevProp = statusEffectProperties.remove(prevIndex);
                if (prevProp.instance != null)
                    StatusEffect.Instance.recycle(prevProp.instance);
            }
        }

        boolean dontAdd = false;
        StatusEffectProperty existingProp = findStatusEffectProperty(s);
        if (warmup <= age && existingProp != null)
        {
            StatusEffect.Instance i = existingProp.instance;
            if (i.age >= i.warmupAge && i.age < i.deteriorationAge)
                dontAdd = true;
        }

        if (!dontAdd && (this.movable instanceof Bullet || this.movable instanceof Tank) && ScreenPartyHost.isServer)
            Game.eventsOut.add(new EventStatusEffectBegin(this.movable, s, age, warmup));

        // Remove existing property if it exists, then add new one
        int existingIndex = indexOf(s);
        if (existingIndex >= 0)
        {
            StatusEffectProperty oldProp = statusEffectProperties.remove(existingIndex);
            if (oldProp.instance != null)
                StatusEffect.Instance.recycle(oldProp.instance);
        }

        statusEffectProperties.add(new StatusEffectProperty(s, StatusEffect.Instance.newInstance(s, age, warmup, deterioration, duration)));
    }

    public void update()
    {
        updateAttributes();
        updateStatusEffects();
    }

    public void updateAttributes()
    {
        ObjectArrayList<AttributeModifier> attributeModifiers = this.attributes;
        for (int i = 0, attributeModifiersSize = attributeModifiers.size(); i < attributeModifiersSize; i++)
        {
            AttributeModifier a = attributeModifiers.get(i);
            a.age += Panel.frameFrequency;
            if (a.duration > 0 && a.age > a.duration)
            {
                a.expired = true;
                this.removeAttributes.add(a);
            }
        }

        // Remove expired attributes and recycle them
        ObjectArrayList<AttributeModifier> modifiers = this.removeAttributes;
        for (int i = 0, modifiersSize = modifiers.size(); i < modifiersSize; i++)
        {
            AttributeModifier a = modifiers.get(i);
            this.attributes.remove(a);
            AttributeModifier.recycle(a);
        }

        this.removeAttributes.clear();
    }

    public void addAttribute(AttributeModifier m)
    {
        if (!this.attributeImmunities.contains(m.name))
            this.attributes.add(m);
        this.addAttributeCallback.accept(m, false);
    }

    public void addUnduplicateAttribute(AttributeModifier m)
    {
        if (this.attributeImmunities.contains(m.name))
            return;

        for (int i = 0; i < attributes.size(); i++)
        {
            AttributeModifier a = attributes.get(i);
            if (m.name.equals(a.name))
            {
                AttributeModifier.recycle(a);
                attributes.remove(i);
                i--;
            }
        }

        this.attributes.add(m);
        this.addAttributeCallback.accept(m, true);
    }

    public void addStatusEffect(StatusEffect s, double warmup, double deterioration, double duration)
    {
        this.addStatusEffect(s, 0, warmup, deterioration, duration);
    }

    public void updateStatusEffects()
    {
        double frameFrequency = this.movable.affectedByFrameFrequency ? Panel.frameFrequency : 1;

        for (int j = 0; j < statusEffectProperties.size(); j++)
        {
            StatusEffectProperty prop = statusEffectProperties.get(j);
            StatusEffect s = prop.statusEffect;
            StatusEffect.Instance i = prop.instance;

            if (i.age < i.deteriorationAge && i.age + frameFrequency >= i.deteriorationAge && ScreenPartyHost.isServer && (this.movable instanceof Bullet || this.movable instanceof Tank))
            {
                Game.eventsOut.add(new EventStatusEffectDeteriorate(this.movable, s, i.duration - i.deteriorationAge));
            }

            if (i.duration <= 0 || i.age + frameFrequency <= i.duration)
                i.age += frameFrequency;
            else
            {
                this.removeStatusEffects.add(s);

                if (this.movable instanceof Bullet || this.movable instanceof Tank)
                    Game.eventsOut.add(new EventStatusEffectEnd(this.movable, s));
            }
        }

        for (StatusEffect s: this.removeStatusEffects)
        {
            int index = indexOf(s);
            if (index >= 0)
            {
                StatusEffectProperty prop = statusEffectProperties.remove(index);
                if (prop.instance != null)
                    StatusEffect.Instance.recycle(prop.instance);
            }
        }

        removeStatusEffects.clear();
    }

    public double getAttributeValue(AttributeModifier.Type type, double value)
    {
        for (int i = 0, attributesSize = attributes.size(); i < attributesSize; i++)
        {
            AttributeModifier a = attributes.get(i);
            if (!a.expired && a.type.equals(type))
                value = a.getValue(value);
        }

        for (int i = 0; i < statusEffectProperties.size(); i++)
        {
            StatusEffectProperty prop = statusEffectProperties.get(i);
            value = prop.instance.getValue(value, type);
        }

        return value;
    }

    /** Returns the attribute modifier object of the same type, or null if it doesn't exist.
     * @apiNote The attribute modifier object returned is mutable. Create a copy using
     * {@link AttributeModifier#copy copy} if you want to modify it, and make sure to
     * {@link AttributeModifier#recycle recycle} it when you're done.
     */
    public AttributeModifier getAttribute(AttributeModifier.Type type)
    {
        AttributeModifier best = null;
        double bestTime = Double.MIN_VALUE;

        for (int i = 0, attributesSize = attributes.size(); i < attributesSize; i++)
        {
            AttributeModifier a = attributes.get(i);
            if (!a.expired && a.type.equals(type))
            {
                if (a.deteriorationAge - a.age > bestTime || a.deteriorationAge <= 0)
                {
                    bestTime = a.deteriorationAge - a.age;
                    best = a;

                    if (a.deteriorationAge <= 0)
                        bestTime = Double.MAX_VALUE;
                }
            }
        }

        for (int j = 0; j < statusEffectProperties.size(); j++)
        {
            StatusEffectProperty prop = statusEffectProperties.get(j);
            StatusEffect s = prop.statusEffect;
            StatusEffect.Instance i = prop.instance;

            if (i != null)
            {
                for (AttributeModifier a : s.attributeModifiers)
                {
                    if (a.type.equals(type))
                    {
                        if (i.deteriorationAge - i.age > bestTime || a.deteriorationAge <= 0)
                        {
                            bestTime = i.deteriorationAge - i.age;
                            best = a;
                            if (a.deteriorationAge <= 0)
                                bestTime = Double.MAX_VALUE;
                        }
                    }
                }
            }
        }

        return best;
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean removeStatusEffect(String effect)
    {
        boolean removed = false;
        for (int i = 0; i < statusEffectProperties.size(); i++)
        {
            StatusEffectProperty prop = statusEffectProperties.get(i);
            if (prop.statusEffect.name.equals(effect))
            {
                statusEffectProperties.remove(i);
                if (prop.instance != null)
                {
                    StatusEffect.Instance.recycle(prop.instance);
                    removed = true;
                }
                i--; // Adjust index since we removed an element
            }
        }
        return removed;
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean removeAttribute(AttributeModifier.Type type)
    {
        boolean removed = false;
        for (int i = 0; i < attributes.size(); i++)
        {
            AttributeModifier a = attributes.get(i);
            if (a.type.equals(type))
            {
                attributes.remove(i);
                AttributeModifier.recycle(a);
                removed = true;
                i--;
            }
        }
        return removed;
    }

    public void recycle()
    {
        for (StatusEffectProperty i : this.statusEffectProperties)
            StatusEffect.Instance.recycle(i.instance);

        for (AttributeModifier a: this.attributes)
            AttributeModifier.recycle(a);

        statusEffectProperties.clear();
        attributes.clear();
    }

    // Property class for parallel arrays
    public static class StatusEffectProperty
    {
        public StatusEffect statusEffect;
        public StatusEffect.Instance instance;

        public StatusEffectProperty(StatusEffect statusEffect, StatusEffect.Instance instance)
        {
            this.statusEffect = statusEffect;
            this.instance = instance;
        }
    }

    public void addImmunities(String... immunities)
    {
        Collections.addAll(this.attributeImmunities, immunities);
    }
}
