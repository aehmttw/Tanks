package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Movable;
import tanks.Player;
import tanks.gui.screen.ScreenGame;
import tanks.tank.TankNPC;
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
                    TankPlayerRemote t = null;
                    TankNPC n = null;
                    for (Movable m : Game.movables)
                    {
                        if (m instanceof TankPlayerRemote && ((TankPlayerRemote) m).player.clientID.equals(this.clientID))
                            t = (TankPlayerRemote) m;

                        if (m instanceof TankNPC && ((TankNPC) m).networkID == this.id)
                            n = (TankNPC) m;
                    }

                    if (t != null && n != null)
                    {
                        int pr = n.shopItems.get(this.item).price;
                        if (p.hotbar.coins >= pr)
                        {
                            if (p.hotbar.itemBar.addItem(n.shopItems.get(this.item).itemStack))
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
