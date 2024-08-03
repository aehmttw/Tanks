package tanks.item;

import tanks.Player;
import tanks.tank.Tank;

public class ItemRemote2 extends Item2
{
    public int bounces;
    public double lifeSpan;

    @Override
    public ItemStack<?> getStack(Player p)
    {
        return new ItemStackRemote(p, this, 0);
    }

    public static class ItemStackRemote extends Item2.ItemStack<ItemRemote2>
    {
        public ItemStackRemote(Player p, ItemRemote2 item, int max)
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
