package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.ItemDrop;
import tanks.item.Item;
import tanks.network.NetworkUtils;

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
    public void write(ByteBuf b)
    {
        b.writeInt(this.id);
        NetworkUtils.writeString(b, item);
        b.writeDouble(this.posX);
        b.writeDouble(this.posY);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.id = b.readInt();
        this.item = NetworkUtils.readString(b);
        this.posX = b.readDouble();
        this.posY = b.readDouble();
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
