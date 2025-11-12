package tanks.network.event;

import tanks.*;
import tanks.item.Item;

public class EventItemDrop extends PersonalEvent
{
    public int id;
    public String item;
    public double posX;
    public double posY;

    public EventItemDrop()
    {

    }

    public EventItemDrop(ItemDrop id)
    {
        this.item = id.item.toString();
        this.id = id.networkID;
        this.posX = id.posX;
        this.posY = id.posY;
    }

    @Override
    public void execute()
    {
        if (this.clientID == null)
        {
            ItemDrop id = new ItemDrop(this.posX, this.posY, Item.ItemStack.fromString(null, this.item));
            id.setNetworkID(this.id);
            Game.movables.add(id);
        }
    }
}
