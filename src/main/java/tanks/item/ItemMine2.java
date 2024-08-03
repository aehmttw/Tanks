package tanks.item;

import tanks.Player;
import tanks.tank.Mine;
import tanks.tank.Tank;

public class ItemMine2 extends Item2
{
    public static final String item_class_name = "mine";

    public Mine mine;

    public ItemMine2()
    {
        this.rightClick = true;
        this.supportsHits = true;
    }

    public ItemMine2(Mine m)
    {
        this();
        this.mine = m;
    }

    @Override
    public ItemStack<?> getStack(Player p)
    {
        return new ItemStackMine(p, this, 0);
    }

    public static class ItemStackMine extends ItemStack<ItemMine2>
    {
        public int liveMines;

        public ItemStackMine(Player p, ItemMine2 item, int max)
        {
            super(p, item, max);
        }

        @Override
        public void use(Tank t)
        {
            Mine m = new Mine(t.posX, t.posY, this.item.mine.timer, t, this);
            this.item.mine.cloneProperties(m);

            t.layMine(m);

            super.use(t);
        }

        @Override
        public boolean usable(Tank t)
        {
            return t != null
                    && (this.item.mine.maxLiveMines <= 0 || this.liveMines < this.item.mine.maxLiveMines)
                    && !(this.cooldown > 0)
                    && this.stackSize > 0;
        }
    }
}
