package tanks.gui.screen;

import tanks.hotbar.item.Item;

public interface IItemScreen
{
    void addItem(Item i);

    void removeItem(Item i);

    void refreshItems();
}
