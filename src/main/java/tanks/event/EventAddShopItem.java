package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.screen.ScreenGame;
import tanks.hotbar.Item;
import tanks.network.NetworkUtils;

public class EventAddShopItem extends PersonalEvent
{
    public int item;
    public String name;
    public String description;

    public EventAddShopItem()
    {

    }

    public EventAddShopItem(int item, String name, String desc)
    {
        this.item = item;
        this.name = name;
        this.description = desc;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(item);
        NetworkUtils.writeString(b, name);
        NetworkUtils.writeString(b, description);
    }

    @Override
    public void read(ByteBuf b)
    {
        item = b.readInt();
        name = NetworkUtils.readString(b);
        description = NetworkUtils.readString(b);
    }

    @Override
    public void execute()
    {
        if (clientID == null && Game.screen instanceof ScreenGame)
        {
            ((ScreenGame) Game.screen).shopItemButtons.add(new Button(0, 0, 350, 40, name, new Runnable()
            {
                @Override
                public void run()
                {
                    Game.eventsOut.add(new EventPurchaseItem(item));
                }
            }, description));


        }
    }
}
