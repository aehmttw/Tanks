package tanks.modapi.events;

import io.netty.buffer.ByteBuf;
import tanks.event.PersonalEvent;
import tanks.gui.ButtonList;
import tanks.gui.screen.ScreenGame;
import tanks.modapi.TankNPC;
import tanks.tank.Tank;

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
        TankNPC t = (TankNPC) Tank.idMap.get(this.id);
        t.npcShopList = new ButtonList(new ArrayList<>(), 0, 0, (int) ScreenGame.shopOffset, -30);
    }
}
