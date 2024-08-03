package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.screen.ScreenGame;
import tanks.item.legacy.ItemRemote;
import tanks.network.NetworkUtils;

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
    public void write(ByteBuf b)
    {
        b.writeInt(item);
        NetworkUtils.writeString(b, name);
        NetworkUtils.writeString(b, description);
        b.writeInt(price);
        NetworkUtils.writeString(b, icon);
    }

    @Override
    public void read(ByteBuf b)
    {
        item = b.readInt();
        name = NetworkUtils.readString(b);
        description = NetworkUtils.readString(b);
        price = b.readInt();
        icon = NetworkUtils.readString(b);
    }

    @Override
    public void execute()
    {
        if (clientID == null && Game.screen instanceof ScreenGame)
        {
            Button b = new Button(0, 0, 350, 40, name, () -> Game.eventsOut.add(new EventPurchaseItem(item)));

            b.setSubtext(description, price);

            ((ScreenGame) Game.screen).shopItemButtons.add(b);

            ItemRemote i = new ItemRemote();
            i.name = name;
            i.icon = icon;

            ((ScreenGame) Game.screen).shop.add(i);
        }
    }
}
