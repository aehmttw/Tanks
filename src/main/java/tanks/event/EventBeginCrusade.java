package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Panel;
import tanks.hotbar.ItemBar;

public class EventBeginCrusade extends PersonalEvent
{
    public EventBeginCrusade()
    {

    }

    @Override
    public void write(ByteBuf b)
    {

    }

    @Override
    public void read(ByteBuf b)
    {

    }

    @Override
    public void execute()
    {
        if (this.clientID == null)
        {
            Game.player.hotbar.coins = 0;
            Game.player.hotbar.itemBar = new ItemBar(Game.player);
        }
    }
}
