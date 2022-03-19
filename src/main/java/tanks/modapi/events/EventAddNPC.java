package tanks.modapi.events;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.event.EventCreateCustomTank;
import tanks.hotbar.item.Item;
import tanks.modapi.TankNPC;
import tanks.network.NetworkUtils;
import tanks.tank.Tank;

import java.util.ArrayList;

public class EventAddNPC extends EventCreateCustomTank
{
    public String messages = "";
    public String tag;

    public ArrayList<Item> shop;

    public EventAddNPC()
    {

    }

    public EventAddNPC(TankNPC t)
    {
        super(t);


        if (t.messages != null)
        {
            StringBuilder messages = new StringBuilder();
            for (String m : t.messages)
                messages.append(m).append("\n");
            this.messages = messages.toString();
        }

        this.tag = t.tagName;
        this.shop = t.shopItems;
    }

    @Override
    public void execute()
    {
        if (this.clientID != null)
            return;

        TankNPC npc = new TankNPC(name, posX, posY, angle, messages, tag, red, green, blue);
        npc.networkID = this.id;
        npc.team = null;
        npc.shopItems = this.shop;

        Tank.idMap.put(npc.networkID, npc);
        Game.movables.add(npc);
    }

    @Override
    public void write(ByteBuf b)
    {
        super.write(b);

        NetworkUtils.writeString(b, messages);
        NetworkUtils.writeString(b, tag == null ? "" : tag);

        StringBuilder shopItems = new StringBuilder();

        for (Item i : shop)
            shopItems.append(i.toString()).append("\n");

        NetworkUtils.writeString(b, shopItems.toString());
    }

    @Override
    public void read(ByteBuf b)
    {
        super.read(b);

        messages = NetworkUtils.readString(b).strip();
        tag = NetworkUtils.readString(b);

        if (tag.equals(""))
            tag = null;

        this.shop = new ArrayList<>();
        String[] shopItems = NetworkUtils.readString(b).strip().split("\n");

        if (shopItems[0].length() > 0)
            for (String item : shopItems)
                this.shop.add(Item.parseItem(null, item));
    }
}
