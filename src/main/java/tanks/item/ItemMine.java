package tanks.item;

import tanks.Player;
import tanks.tank.Mine;
import tanks.tank.Tank;
import tanks.tankson.Property;

public class ItemMine extends Item
{
    public static final String item_class_name = "mine";

    @Property(id="mine", category = "none")
    public Mine mine = new Mine();

    public ItemMine()
    {
        this.rightClick = true;
        this.supportsHits = true;
        this.icon = "mine.png";
    }

    public ItemMine(Mine m)
    {
        this();
        this.mine = m;
    }

    @Override
    public ItemStack<?> getStack(Player p)
    {
        return new ItemStackMine(p, this, 0);
    }

    public static class ItemStackMine extends ItemStack<ItemMine>
    {
        public int liveMines;

        public ItemStackMine(Player p, ItemMine item, int max)
        {
            super(p, item, max);
        }

        @Override
        public void use(Tank t)
        {
            Mine m = new Mine(t.posX, t.posY, this.item.mine.timer, t, this);
            this.item.mine.clonePropertiesTo(m);

            t.layMine(m);

            super.use(t);
        }

        @Override
        public boolean usable(Tank t)
        {
            return t != null
                    && (this.item.mine.maxLiveMines <= 0 || this.liveMines < this.item.mine.maxLiveMines)
                    && !(this.cooldown > 0);
        }
    }
}
