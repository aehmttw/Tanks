package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Panel;
import tanks.Player;
import tanks.gui.screen.ScreenGame;
import tanks.network.NetworkUtils;

public class EventSetupHotbar extends PersonalEvent
{
    public boolean items;
    public boolean coins;

    public EventSetupHotbar()
    {

    }

    public EventSetupHotbar(Player p)
    {
        this.items = p.hotbar.enabledItemBar;
        this.coins = p.hotbar.enabledCoins;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeBoolean(this.items);
        b.writeBoolean(this.coins);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.items = b.readBoolean();
        this.coins = b.readBoolean();
    }

    @Override
    public void execute()
    {
        if (this.clientID == null)
        {
            Game.player.hotbar.enabledItemBar = this.items;
            Game.player.hotbar.enabledCoins = this.coins;
        }
    }
}
