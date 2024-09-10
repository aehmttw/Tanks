package tanks.item;

import tanks.*;
import tanks.network.event.EventTankUpdateHealth;
import tanks.tank.Tank;
import tanks.tankson.Property;

public class ItemShield extends Item
{
    public static final String item_class_name = "shield";

    @Property(id = "health_boost", name = "Health boost")
    public double amount;

    @Property(id = "max_extra_health", name = "Max extra health")
    public double max;

    public ItemShield()
    {
        this.rightClick = true;
    }

    @Override
    public ItemStack<?> getStack(Player p)
    {
        return new ItemStackShield(p, this, 0);
    }

    public static class ItemStackShield extends ItemStack<ItemShield>
    {
        public ItemStackShield(Player p, ItemShield item, int max)
        {
            super(p, item, max);
        }

        @Override
        public void use(Tank t)
        {
            t.health += this.item.amount;

            if (t.health > this.item.max)
                t.health = this.item.max;

            Game.eventsOut.add(new EventTankUpdateHealth(t));

            Drawing.drawing.playGlobalSound("shield.ogg");

            if (t.health > 6 && (int) (t.health - this.item.amount) != (int) (t.health))
            {
                Effect e = Effect.createNewEffect(t.posX, t.posY, t.posZ + t.size * 0.75, Effect.EffectType.shield);
                e.size = t.size;
                e.radius = t.health - 1;
                Game.effects.add(e);
            }

            super.use(t);
        }

        @Override
        public boolean usable(Tank t)
        {
            return (this.item.max <= 0 || t.health < this.item.max) && this.cooldown <= 0;
        }
    }
}
