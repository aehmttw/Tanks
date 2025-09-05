package tanks.network.event;

import tanks.*;
import tanks.gui.ButtonList;
import tanks.gui.screen.ScreenGame;
import tanks.tank.TankNPC;

import java.util.ArrayList;

public class EventClearNPCShop extends PersonalEvent
{
    public int id;

    public EventClearNPCShop()
    {

    }

    public EventClearNPCShop(int id)
    {
        this.id = id;
    }

    @Override
    public void execute()
    {
        for (Movable m : Game.movables)
        {
            if (m instanceof TankNPC && ((TankNPC) m).networkID == this.id)
            {
                ((TankNPC) m).npcShopList = new ButtonList(new ArrayList<>(), 0, 0, (int) ScreenGame.shopOffset, -30);
                break;
            }
        }
    }
}
