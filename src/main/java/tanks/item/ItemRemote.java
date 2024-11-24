package tanks.item;

import tanks.Player;
import tanks.tank.Tank;

public class ItemRemote extends Item
{
    public int bounces;

    public double lifespan;
    public double rangeMin;
    public double rangeMax;
    public boolean showTrace;

    public boolean hideShootStick = false;

    @Override
    public ItemStack<?> getStack(Player p)
    {
        return new ItemStackRemote(p, this, 0);
    }

    public static class ItemStackRemote extends Item.ItemStack<ItemRemote>
    {
        public ItemStackRemote(Player p, ItemRemote item, int max)
        {
            super(p, item, max);
        }

        @Override
        public boolean usable(Tank t)
        {
            return false;
        }
    }

    public static ItemStackRemote getRemoteItem()
    {
        return new ItemStackRemote(null, new ItemRemote(), 0);
    }
}
