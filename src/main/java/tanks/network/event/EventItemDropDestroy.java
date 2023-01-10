package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.ItemDrop;

public class EventItemDropDestroy extends PersonalEvent
{
    public int itemDrop;

    public EventItemDropDestroy()
    {

    }

    public EventItemDropDestroy(ItemDrop id)
    {
        this.itemDrop = id.networkID;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(this.itemDrop);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.itemDrop = b.readInt();
    }

    @Override
    public void execute()
    {
        if (clientID == null && ItemDrop.idMap.get(itemDrop) != null)
        {
            ItemDrop id = ItemDrop.idMap.get(itemDrop);
            id.destroy = true;
        }
    }
}
