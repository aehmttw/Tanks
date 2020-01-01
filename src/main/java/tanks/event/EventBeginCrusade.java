package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Panel;
import tanks.hotbar.Coins;
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
            Game.player.coins = new Coins();
            Game.player.crusadeItemBar = new ItemBar(Game.player);
            Game.player.crusadeItemBar.hotbar = Panel.panel.hotbar;
        }
    }
}
