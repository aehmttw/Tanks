package tanks.gui.screen;

import tanks.hotbar.item.Item;

public abstract class ItemScreen extends Screen
{
    public abstract void removeItem(Item i);

    public abstract void refreshItems();
}
