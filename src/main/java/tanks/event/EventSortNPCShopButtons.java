package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Movable;
import tanks.tank.TankNPC;

public class EventSortNPCShopButtons extends PersonalEvent
{
    public int id;

    public EventSortNPCShopButtons()
    {
    }

    public EventSortNPCShopButtons(int id)
    {
        this.id = id;
    }

    public void write(ByteBuf b)
    {
        b.writeInt(this.id);
    }

    public void read(ByteBuf b)
    {
        this.id = b.readInt();
    }

    public void execute()
    {
        for (Movable m : Game.movables)
        {
            if (m instanceof TankNPC && ((TankNPC) m).networkID == this.id)
            {
                ((TankNPC) m).npcShopList.sortButtons();
                break;
            }
        }

    }
}
