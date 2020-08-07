package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Player;
import tanks.gui.screen.ScreenGame;

public class EventPurchaseItem extends PersonalEvent
{
    public int item;

    public EventPurchaseItem()
    {

    }

    public EventPurchaseItem(int item)
    {
        this.item = item;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(item);
    }

    @Override
    public void read(ByteBuf b)
    {
        item = b.readInt();
    }

    @Override
    public void execute()
    {
        if (this.clientID != null && Game.screen instanceof ScreenGame)
        {
            ScreenGame s = (ScreenGame) Game.screen;
            for (int i = 0; i < Game.players.size(); i++)
            {
                if (Game.players.get(i).clientID.equals(this.clientID))
                {
                    Player p = Game.players.get(i);
                    int pr = s.shop.get(this.item).price;
                    if (p.hotbar.coins >= pr)
                    {
                        if (p.hotbar.itemBar.addItem(s.shop.get(this.item)))
                            p.hotbar.coins -= pr;

                        Game.eventsOut.add(new EventUpdateCoins(p));
                    }
                }
            }
        }
    }
}
