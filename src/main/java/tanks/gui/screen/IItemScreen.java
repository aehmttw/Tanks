package tanks.gui.screen;

import tanks.item.legacy.Item;

public interface IItemScreen
{
    void addItem(Item i);

    void removeItem(Item i);

    void refreshItems();
}
