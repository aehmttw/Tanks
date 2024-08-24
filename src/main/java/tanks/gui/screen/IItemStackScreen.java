package tanks.gui.screen;

import tanks.item.Item;

public interface IItemStackScreen
{
    void addItem(Item.ItemStack<?> i);

    void removeItem(Item.ItemStack<?> i);

    void refreshItems();
}
