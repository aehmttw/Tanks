package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.item.Item;
import tanks.network.NetworkUtils;
import tanks.tank.TankNPC;

import java.util.ArrayList;

public class EventAddNPC extends EventTankCustomCreate
{
    public String messages = "";
    public String tag;

    public ArrayList<Item.ShopItem> shop;

    public EventAddNPC()
    {

    }

    public EventAddNPC(TankNPC t)
    {
        super(t);

        StringBuilder messages = new StringBuilder();
        for (String m : t.messages)
            messages.append(m).append("\n");
        this.messages = messages.toString();

        this.tag = t.tagName;
        this.shop = t.shopItems;
    }

    @Override
    public void execute()
    {
        if (this.clientID != null)
            return;

        TankNPC npc = new TankNPC(name, posX, posY, angle, messages, tag, red, green, blue);
        npc.setNetworkID(id);
        npc.team = null;
        npc.shopItems = this.shop;
        Game.movables.add(npc);
    }

    @Override
    public void write(ByteBuf b)
    {
        super.write(b);

        NetworkUtils.writeString(b, messages);
        NetworkUtils.writeString(b, tag);

        StringBuilder shopItems = new StringBuilder();

        for (Item.ShopItem i : shop)
            shopItems.append(i.toString()).append("\n");

        NetworkUtils.writeString(b, shopItems.toString());
    }

    @Override
    public void read(ByteBuf b)
    {
        super.read(b);

        messages = NetworkUtils.readString(b);
        tag = NetworkUtils.readString(b);

        this.shop = new ArrayList<>();
        String[] shopItems = NetworkUtils.readString(b).split("\n");
        for (String item : shopItems)
            this.shop.add(Item.CrusadeShopItem.fromString(item));
    }
}
