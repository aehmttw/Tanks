package tanks.effect;

import it.unimi.dsi.fastutil.objects.*;
import tanks.*;
import tanks.bullet.Bullet;
import tanks.gui.screen.ScreenPartyHost;
import tanks.network.event.EventStatusEffectBegin;
import tanks.network.event.EventStatusEffectDeteriorate;
import tanks.network.event.EventStatusEffectEnd;
import tanks.tank.Tank;

import java.util.*;

public class EffectManager
{
    public Movable movable;

    public BiConsumer<AttributeModifier, Boolean> addAttributeCallback = (a, b) -> {};

    public HashMap<String, AttributeModifier> attributeImmunities = new HashMap<>();
    public ObjectArraySet<AttributeModifier> attributes = new ObjectArraySet<>();
    public Object2ObjectArrayMap<StatusEffect, StatusEffect.Instance> statusEffects = new Object2ObjectArrayMap<>();
    public ObjectArrayList<StatusEffect> removeStatusEffects = new ObjectArrayList<>();
    public ObjectArrayList<AttributeModifier> removeAttributes = new ObjectArrayList<>();

    public EffectManager(Movable m)
    {
        this.movable = m;
    }

    public void update()
    {
        updateAttributes();
        updateStatusEffects();
    }

    public void updateAttributes()
    {
        for (AttributeModifier a : this.attributes)
        {
            if (a.duration > 0 && a.age > a.duration)
            {
                a.expired = true;
                this.removeAttributes.add(a);
            }
        }

        // Remove expired attributes and recycle them
        for (AttributeModifier a : this.removeAttributes)
        {
            this.attributes.remove(a);
            AttributeModifier.recycle(a);
        }

        this.removeAttributes.clear();
    }

    public void addAttribute(AttributeModifier m)
    {
        if (!this.attributeImmunities.containsKey(m.name))
            this.attributes.add(m);
        this.addAttributeCallback.accept(m, false);
    }

    public void addUnduplicateAttribute(AttributeModifier m)
    {
        if (this.attributeImmunities.containsKey(m.name))
            return;

        this.attributes.remove(m);      // will be removed if name is equal, as defined by AttributeModifier.equals()
        this.attributes.add(m);
        this.addAttributeCallback.accept(m, true);
    }

    public void addStatusEffect(StatusEffect s, double warmup, double deterioration, double duration)
    {
        this.addStatusEffect(s, 0, warmup, deterioration, duration);
    }

    public void addStatusEffect(StatusEffect s, double age, double warmup, double deterioration, double duration)
    {
        if (deterioration > duration)
            throw new RuntimeException("Deterioration age > duration");

        StatusEffect prevEffect = null;
        for (StatusEffect e: this.statusEffects.keySet())
        {
            if (e.family != null && e.family.equals(s.family))
                prevEffect = e;
        }

        if (prevEffect != null)
        {
            StatusEffect.Instance prevInstance = this.statusEffects.remove(prevEffect);
            if (prevInstance != null)
                StatusEffect.Instance.recycle(prevInstance);
        }

        boolean dontAdd = false;
        if (warmup <= age && this.statusEffects.get(s) != null)
        {
            StatusEffect.Instance i = this.statusEffects.get(s);
            if (i.age >= i.warmupAge && i.age < i.deteriorationAge)
                dontAdd = true;
        }

        if (!dontAdd && (this.movable instanceof Bullet || this.movable instanceof Tank) && ScreenPartyHost.isServer)
            Game.eventsOut.add(new EventStatusEffectBegin(this.movable, s, age, warmup));

        this.statusEffects.put(s, StatusEffect.Instance.newInstance(s, age, warmup, deterioration, duration));
    }

    public void updateStatusEffects()
    {
        double frameFrequency = this.movable.affectedByFrameFrequency ? Panel.frameFrequency : 1;

        for (StatusEffect s: this.statusEffects.keySet())
        {
            StatusEffect.Instance i = this.statusEffects.get(s);

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
            StatusEffect.Instance instance = this.statusEffects.remove(s);
            if (instance != null)
                StatusEffect.Instance.recycle(instance);
        }

        removeStatusEffects.clear();
    }

    public double getAttributeValue(AttributeModifier.Type type, double value)
    {
        for (AttributeModifier a : attributes)
        {
            if (!a.expired && a.type.equals(type))
                value = a.getValue(value);
        }

        for (StatusEffect.Instance s : this.statusEffects.values())
            value = s.getValue(value, type);

        return value;
    }

    public AttributeModifier getAttribute(AttributeModifier.Type type)
    {
        AttributeModifier best = null;
        double bestTime = Double.MIN_VALUE;

        for (AttributeModifier a : attributes)
        {
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

        for (StatusEffect s : this.statusEffects.keySet())
        {
            StatusEffect.Instance i = this.statusEffects.get(s);

            if (i != null)
            {
                for (AttributeModifier a : s.attributeModifiers)
                {
                    if (a.type.equals(type))
                    {
                        if (i.deteriorationAge - i.age > bestTime || a.deteriorationAge <= 0)
                        {
                            bestTime = i.deteriorationAge - i.age;
                            best = AttributeModifier.obtain(a.type, a.effect, a.value);
                            best.warmupAge = i.warmupAge;
                            best.deteriorationAge = i.deteriorationAge;
                            best.age = i.age;
                            best.duration = i.duration;

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
    public boolean removeAttribute(AttributeModifier.Type type)
    {
        boolean removed = false;
        for (AttributeModifier a : new ObjectArraySet<>(attributes))
        {
            if (a.type.equals(type))
            {
                attributes.remove(a);
                AttributeModifier.recycle(a);
                removed = true;
            }
        }
        return removed;
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean removeStatusEffect(String effect)
    {
        boolean removed = false;
        for (StatusEffect e : statusEffects.keySet())
        {
            if (e.name.equals(effect))
            {
                StatusEffect.Instance instance = statusEffects.remove(e);
                if (instance != null)
                {
                    StatusEffect.Instance.recycle(instance);
                    removed = true;
                }
            }
        }
        return removed;
    }

    public void recycle()
    {
        for (StatusEffect.Instance i: this.statusEffects.values())
            StatusEffect.Instance.recycle(i);

        for (AttributeModifier a: this.attributes)
            AttributeModifier.recycle(a);

        statusEffects.clear();
        attributes.clear();
    }
}
