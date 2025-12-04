package tanks.item;

import tanks.Player;
import tanks.tank.Tank;

/**
 * Used as a dummy item for when blocks explode because tanks run into them
 */
public class ItemDummyBlockExplosion extends Item
{
    public static ItemDummyBlockExplosion dummy_explosion = new ItemDummyBlockExplosion();

    public ItemDummyBlockExplosion()
    {
        this.name = "Block explosion";
        this.icon = new ItemIcon("block_explosion", "block_explosion.png");
        this.supportsHits = true;
    }

    @Override
    public ItemStack<?> getStack(Player p)
    {
        return new ItemStackDummyExplosiveBlock(p, this, 0);
    }

    public static class ItemStackDummyExplosiveBlock extends ItemStack<ItemDummyBlockExplosion>
    {
        public ItemStackDummyExplosiveBlock(Player p, ItemDummyBlockExplosion item, int max)
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
