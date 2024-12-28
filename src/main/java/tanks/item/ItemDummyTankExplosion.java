package tanks.item;

import tanks.Player;
import tanks.tank.Tank;

/**
 * Used as a dummy item for when tanks explode when destroyed
 */
public class ItemDummyTankExplosion extends Item
{
    public static ItemDummyTankExplosion dummy_explosion = new ItemDummyTankExplosion();

    public ItemDummyTankExplosion()
    {
        this.name = "Tank explosion";
        this.icon = "tankeditor/last_stand.png";
        this.supportsHits = true;
    }

    @Override
    public ItemStack<?> getStack(Player p)
    {
        return new ItemStackDummyExplosiveTank(p, this, 0);
    }

    public static class ItemStackDummyExplosiveTank extends ItemStack<ItemDummyTankExplosion>
    {
        public ItemStackDummyExplosiveTank(Player p, ItemDummyTankExplosion item, int max)
        {
            super(p, item, max);
        }

        @Override
        public boolean usable(Tank t)
        {
            return false;
        }
    }
}
