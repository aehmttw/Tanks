package tanks.network.event;

import tanks.Game;
import tanks.gui.Button;
import tanks.gui.screen.ScreenGame;
import tanks.item.*;

public class EventAddShopItem extends PersonalEvent
{
    public int item;
    public String name;
    public String description;
    public int price;
    public String icon;

    public EventAddShopItem()
    {

    }

    public EventAddShopItem(int item, String name, String desc, int price, String icon)
    {
        this.item = item;
        this.name = name;
        this.description = desc;
        this.price = price;
        this.icon = icon;
    }

    @Override
    public void execute()
    {
        if (clientID == null && Game.screen instanceof ScreenGame)
        {
            Button b = new Button(0, 0, 350, 40, name, () -> Game.eventsOut.add(new EventPurchaseItem(item)));

            b.setSubtext(description, price);

            ((ScreenGame) Game.screen).shopItemButtons.add(b);

            Item.ShopItem i = new Item.ShopItem(ItemRemote.getRemoteItem());
            i.itemStack.item.name = name;
            i.itemStack.item.icon = icon;
            i.price = price;

            ((ScreenGame) Game.screen).shop.add(i);
        }
    }
}
