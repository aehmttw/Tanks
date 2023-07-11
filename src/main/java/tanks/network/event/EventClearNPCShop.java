package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Movable;
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
    public void write(ByteBuf b)
    {
        b.writeInt(this.id);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.id = b.readInt();
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
