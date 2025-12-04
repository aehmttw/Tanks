package tanks.network.event;

import tanks.*;
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
