package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Movable;
import tanks.network.event.EventAddShopItem;
import tanks.network.event.EventPurchaseItem;
import tanks.gui.Button;
import tanks.gui.screen.ScreenGame;
import tanks.hotbar.item.ItemRemote;
import tanks.tank.TankNPC;

public class EventAddNPCShopItem extends EventAddShopItem
{
    public int id;

    public EventAddNPCShopItem()
    {

    }

    public EventAddNPCShopItem(int item, String name, String desc, int price, String icon, int npcId)
    {
        super(item, name, desc, price, icon);
        this.id = npcId;
    }

    @Override
    public void write(ByteBuf b)
    {
        super.write(b);
        b.writeInt(this.id);
    }

    @Override
    public void read(ByteBuf b)
    {
        super.read(b);
        this.id = b.readInt();
    }

    @Override
    public void execute()
    {
        if (clientID == null && Game.screen instanceof ScreenGame)
        {
            ItemRemote i = new ItemRemote();
            i.name = name;
            i.icon = icon;

            Button b = new Button(0, 0, 350, 40, name, () -> Game.eventsOut.add(new EventPurchaseItem(item)));
            b.subtext = description;
            b.image = i.icon;
            b.imageXOffset = -145;
            b.imageSizeX = 30;
            b.imageSizeY = 30;

            for (Movable m : Game.movables)
            {
                if (m instanceof TankNPC && ((TankNPC) m).networkID == this.id)
                {
                    ((TankNPC) m).shopItems.add(i);
                    break;
                }
            }
        }
    }
}
