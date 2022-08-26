package tanks.modapi.events;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Player;
import tanks.event.EventPurchaseItem;
import tanks.event.EventUpdateCoins;
import tanks.gui.screen.ScreenGame;
import tanks.modapi.TankNPC;
import tanks.tank.Tank;
import tanks.tank.TankPlayerRemote;

public class EventPurchaseNPCItem extends EventPurchaseItem
{
    public int id;

    public EventPurchaseNPCItem()
    {

    }

    public EventPurchaseNPCItem(int item, int id)
    {
        super(item);
        this.id = id;
    }

    @Override
    public void write(ByteBuf b)
    {
        super.write(b);
        b.writeInt(id);
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
        if (this.clientID != null && Game.screen instanceof ScreenGame)
        {
            for (Player p : Game.players)
            {
                if (p.clientID.equals(this.clientID))
                {
                    TankPlayerRemote t = (TankPlayerRemote) Tank.idMap.get(p.tank.networkID);
                    TankNPC n = (TankNPC) Tank.idMap.get(this.id);

                    if (t != null && n != null)
                    {
                        int pr = n.shopItems.get(this.item).price;
                        if (p.hotbar.coins >= pr)
                        {
                            if (p.hotbar.itemBar.addItem(n.shopItems.get(this.item)))
                                p.hotbar.coins -= pr;

                            Game.eventsOut.add(new EventUpdateCoins(p));
                            break;
                        }
                    }
                }
            }
        }
    }
}
