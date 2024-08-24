package tanks.gui.screen;

import tanks.item.Item;

public interface IShopItemScreen
{
    void addItem(Item.ShopItem i);

    void removeItem(Item.ShopItem i);

    void refreshItems();
}
