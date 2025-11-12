package tanks.network.event;

import tanks.*;
import tanks.gui.Button;
import tanks.gui.screen.ScreenGame;
import tanks.item.*;
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
    public void execute()
    {
        if (clientID == null && Game.screen instanceof ScreenGame)
        {
            Item.ShopItem i = new Item.ShopItem(ItemRemote.getRemoteItem());
            i.itemStack.item.name = name;
            i.itemStack.item.icon = icon;

            Button b = new Button(0, 0, 350, 40, name, () -> Game.eventsOut.add(new EventPurchaseItem(item)));
            b.subtext = description;
            b.image = i.itemStack.item.icon;
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
