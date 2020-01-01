package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Panel;
import tanks.gui.screen.ScreenGame;
import tanks.network.NetworkUtils;

public class EventLoadCrusadeHotbar extends PersonalEvent
{
    public String title;

    public EventLoadCrusadeHotbar()
    {

    }

    public EventLoadCrusadeHotbar(String title)
    {
        this.title = title;
    }

    @Override
    public void write(ByteBuf b)
    {
        NetworkUtils.writeString(b, this.title);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.title = NetworkUtils.readString(b);
    }

    @Override
    public void execute()
    {
        if (this.clientID == null)
        {
            Panel.panel.hotbar.enabledItemBar = true;
            Panel.panel.hotbar.currentItemBar = Game.player.crusadeItemBar;
            Panel.panel.hotbar.enabledCoins = true;
            Panel.panel.hotbar.currentCoins = Game.player.coins;
        }

        ((ScreenGame)(Game.screen)).title = this.title;
    }
}
