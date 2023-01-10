package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Drawing;
import tanks.ItemDrop;
import tanks.tank.Tank;

public class EventItemPickup extends PersonalEvent
{
    public int itemDrop;
    public int tank;

    public EventItemPickup()
    {

    }

    public EventItemPickup(ItemDrop id, Tank pickup)
    {
        this.itemDrop = id.networkID;
        this.tank = pickup.networkID;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(this.itemDrop);
        b.writeInt(this.tank);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.itemDrop = b.readInt();
        this.tank = b.readInt();
    }

    @Override
    public void execute()
    {
        if (clientID == null && ItemDrop.idMap.get(itemDrop) != null && Tank.idMap.get(tank) != null)
        {
            ItemDrop id = ItemDrop.idMap.get(itemDrop);
            id.pickup = Tank.idMap.get(tank);
            id.destroy = true;
            Drawing.drawing.playSound("bullet_explode.ogg", 1.6f);
        }
    }
}
