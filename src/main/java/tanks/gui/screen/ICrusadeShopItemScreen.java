package tanks.gui.screen;

import tanks.item.Item;

public interface ICrusadeShopItemScreen
{
    void addItem(Item.CrusadeShopItem i);

    void removeItem(Item.CrusadeShopItem i);

    void refreshItems();
}
