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
    private static final ObjectArrayList<String> toRemove = new ObjectArrayList<>();
    private static final ObjectArrayList<AttributeModifier> expiredAttributes = new ObjectArrayList<>();
    private static final ObjectArrayList<StatusEffect.Instance> expiredStatusEffects = new ObjectArrayList<>();
    private static final BiConsumer<AttributeModifier, Boolean> empty = (a, b) -> {};

    public Movable movable;

    public Object2ObjectOpenHashMap<String, AttributeModifier> attributes = new Object2ObjectOpenHashMap<>();
    public HashSet<String> attributeImmunities = new HashSet<>();
    private final ObjectCollection<AttributeModifier> attValues = attributes.values();

    public HashMap<AttributeModifier.Type, AttributeModifier.Instance> typeInstances = new HashMap<>();
    public Object2ObjectOpenHashMap<String, StatusEffect.Instance> statusEffects = new Object2ObjectOpenHashMap<>();
    private final ObjectCollection<StatusEffect.Instance> stValues = statusEffects.values();

    public BiConsumer<AttributeModifier, Boolean> addAttributeCallback = empty;

    public EffectManager(Movable m)
    {
        this.movable = m;
    }

    public void update()
    {
        updateAttributes();
        updateStatusEffects();
    }

    public long lastEventTime = 0;

    /**
     * Increment ages of status effects, remove them if past duration,
     * and rely on each effect’s attribute modifiers to auto-update partial intensities.
     */
    public void updateStatusEffects()
    {
        double frameFrequency = movable.affectedByFrameFrequency ? Panel.frameFrequency : 1;

        for (StatusEffect.Instance inst : stValues)
        {
            double oldAge = inst.age;

            // Check for transition into deterioration
            if (oldAge < inst.deteriorationAge && oldAge + frameFrequency >= inst.deteriorationAge
                    && ScreenPartyHost.isServer && (movable instanceof Bullet || movable instanceof Tank) &&
                        System.currentTimeMillis() - lastEventTime > 100)
            {
                Game.eventsOut.add(new EventStatusEffectDeteriorate(movable, inst.effect, inst.duration - inst.deteriorationAge));
                lastEventTime = System.currentTimeMillis();
            }

            // Advance age
            if (inst.duration <= 0 || inst.age + frameFrequency <= inst.duration)
                inst.age += frameFrequency;
            else
                expiredStatusEffects.add(inst);
        }

       for (StatusEffect.Instance e : expiredStatusEffects)
       {
           removeStatusEffect(e.effect.name);

           if (movable instanceof Bullet || movable instanceof Tank)
               Game.eventsOut.add(new EventStatusEffectEnd(movable, e.effect));
       }
       expiredStatusEffects.clear();
    }

    /**
     * Returns the final value for a given attribute type,
     * factoring in all active attributes in the attributes map.
     */
    public double getAttributeValue(AttributeModifier.Type type, double baseValue)
    {
        AttributeModifier.Instance inst = typeInstances.get(type);
        if (inst != null)
            baseValue = inst.apply(baseValue);
        return baseValue;
    }

    /**
     * Retrieves the group of attributes for a given Type
     */
    public AttributeModifier.Instance getAttribute(AttributeModifier.Type type)
    {
        AttributeModifier.Instance instance = typeInstances.get(type);
        if (instance == null || instance.isEmpty())
            return null;
        return instance;
    }

    /**
     * Remove attribute by name
     */
    public void removeAttribute(String name)
    {
        AttributeModifier a = attributes.remove(name);
        if (a == null)
            return;

        typeInstances.get(a.type).attributeList.remove(a);
    }

    /**
     * Remove all attributes of a given type (both normal & status-effect attributes).
     */
    public void removeAttribute(AttributeModifier.Type type)
    {
        for (Map.Entry<String, AttributeModifier> e : attributes.entrySet())
        {
            if (e.getValue().type == type)
                toRemove.add(e.getKey());
        }
        for (String k : toRemove)
            removeAttribute(k);

        typeInstances.remove(type);
        toRemove.clear();
    }

    /**
     * Adds a normal attribute if not immune.
     */
    public void addAttribute(AttributeModifier m)
    {
        if (this.attributeImmunities.contains(m.name))
            return;

        attributes.put(m.name, m);
        typeInstances.computeIfAbsent(m.type, AttributeModifier.Instance::new).attributeList.add(m);
        addAttributeCallback.accept(m, false);
    }

    /**
     * Removes any previous attribute with the same name, then adds the new one if not immune.
     */
    public void addUnduplicateAttribute(AttributeModifier m)
    {
        if (this.attributeImmunities.contains(m.name))
            return;

        AttributeModifier old = attributes.remove(m.name);
        if (old != null)
        {
            AttributeModifier.Instance inst = typeInstances.get(old.type);
            if (inst != null)
                inst.attributeList.remove(old);
        }
        else
            addAttributeCallback.accept(m, true);

        attributes.put(m.name, m);
        typeInstances.computeIfAbsent(m.type, AttributeModifier.Instance::new).attributeList.add(m);
    }

    public void addImmunities(String... immunities)
    {
        Collections.addAll(attributeImmunities, immunities);
    }

    /**
     * Creates an effect instance, also spawns a StatusEffectAttributeModifier
     * for each attribute in the effect, then adds them to the attributes map.
     */
    public void addStatusEffect(StatusEffect s, double warmup, double deterioration, double duration)
    {
        this.addStatusEffect(s, 0, warmup, deterioration, duration);
    }

    public void addStatusEffect(StatusEffect s, double age, double warmup, double deterioration, double duration)
    {
        if (deterioration > duration)
            throw new RuntimeException("Deterioration age > duration");

        // If there's an existing effect in the same "family", remove it
        StatusEffect prevEffect = null;
        for (StatusEffect.Instance inst : stValues)
        {
            if (inst.effect.family != null && inst.effect.family.equals(s.family))
                prevEffect = inst.effect;
        }
        if (prevEffect != null)
            removeStatusEffect(prevEffect.name);

        // Possibly skip if partially overlapping, etc. (same logic as before)
        StatusEffect.Instance existing = this.statusEffects.get(s.name);
        boolean skipAddition = false;
        if (warmup <= age && existing != null)
        {
            if (existing.age >= existing.warmupAge && existing.age < existing.deteriorationAge)
                skipAddition = true;
        }

        // Fire network event
        if (!skipAddition && (movable instanceof Bullet || movable instanceof Tank) && ScreenPartyHost.isServer)
            Game.eventsOut.add(new EventStatusEffectBegin(movable, s, age, warmup));

        // Create the new instance
        StatusEffect.Instance newInst = new StatusEffect.Instance(s, age, warmup, deterioration, duration);
        this.statusEffects.put(s.name, newInst);

        // Insert each attribute from the StatusEffect as a dynamic attribute
        for (AttributeModifier baseMod : s.attributeModifiers)
        {
            StatusEffectAttributeModifier sm = new StatusEffectAttributeModifier(newInst, baseMod);
            // Add it to the normal attributes map so that it is fully integrated
            this.addUnduplicateAttribute(sm);
        }
    }

    /**
     * Removes the status effect and all its associated attribute modifiers from the map.
     */
    public void removeStatusEffect(String effectName)
    {
        StatusEffect.Instance inst = this.statusEffects.remove(effectName);
        if (inst != null)
        {
            // Also remove all attribute modifiers belonging to this effect
            // We know their names start with "statusEffect:<effectName>_"
            for (Map.Entry<String, AttributeModifier> e : attributes.entrySet())
            {
                if (e.getKey().startsWith("statusEffect:" + effectName + "_"))
                    toRemove.add(e.getKey());
            }
            for (String k : toRemove)
                removeAttribute(k);

            toRemove.clear();
        }
    }

    /**
     * Updates normal attributes: increments age, removes if expired, etc.
     * (Status-effect-based attributes will also get updated if they override update().)
     */
    public void updateAttributes()
    {
        for (AttributeModifier a : attValues)
        {
            a.update();
            if (a.expired)
                expiredAttributes.add(a);
        }

        for (AttributeModifier expired : expiredAttributes)
        {
            if (attributes.get(expired.name) != expired)
                continue;

            removeAttribute(expired.name);

            // Also remove from typeInstances if present
            AttributeModifier.Instance inst = typeInstances.get(expired.type);
            if (inst != null)
                inst.attributeList.remove(expired);
        }

        expiredAttributes.clear();
    }

    /**
     * A subclass of {@linkplain AttributeModifier} that is driven by a {@linkplain StatusEffect.Instance}.<br>
     * This allows each effect's partial intensity to be directly updated as the effect ages.<br>
     * Mainly serves the purpose of copying the base modifier.
     */
    public static class StatusEffectAttributeModifier extends AttributeModifier
    {
        public StatusEffect.Instance instance;
        /** the original modifier from the StatusEffect, none of its properties should change (other than age) */
        public AttributeModifier baseModifier;

        public StatusEffectAttributeModifier(StatusEffect.Instance inst, AttributeModifier baseMod)
        {
            // We build a name to differentiate it from normal attributes:
            // e.g. "statusEffect:<effectName>_<baseMod.name>"
            super("statusEffect:" + inst.effect.name + "_" + baseMod.name,
                    baseMod.type, baseMod.operation, baseMod.value);
            this.instance = inst;
            this.baseModifier = baseMod;
        }

        @Override
        public double getValue(double in)
        {
            baseModifier.age = instance.age;
            baseModifier.duration = instance.duration;
            baseModifier.deteriorationAge = instance.deteriorationAge;
            baseModifier.warmupAge = instance.warmupAge;

            return baseModifier.getValue(in);
        }
    }
}
