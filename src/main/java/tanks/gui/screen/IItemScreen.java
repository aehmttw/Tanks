package tanks.gui.screen;

import tanks.hotbar.item.Item;

public interface IItemScreen
{
    public abstract void removeItem(Item i);

    public abstract void refreshItems();
}
