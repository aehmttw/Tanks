package tanks.item;

import tanks.*;
import tanks.network.event.EventTankUpdateHealth;
import tanks.tank.Tank;
import tanks.tankson.Property;

public class ItemShield extends Item
{
    public static final String item_class_name = "shield";

    @Property(id = "health_boost", name = "Hitpoint boost", desc = "This item will instantly add this many hitpoints to the tank using it \n \n The default player tank has 1 hitpoint, and the default bullet does 1 hitpoint of damage")
    public double amount;

    @Property(id = "max_extra_health", name = "Max extra hitpoints", desc = "This item will not heal a tank to more than its default hitpoints plus 'max extra hitpoints' \n \n The default player tank has 1 hitpoint, and the default bullet does 1 hitpoint of damage")
    public double max;

    public ItemShield()
    {
        this.rightClick = true;
        this.icon = "shield.png";
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

            if (t.health > this.item.max + t.baseHealth)
                t.health = this.item.max + t.baseHealth;

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
