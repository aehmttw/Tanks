package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.gui.screen.ScreenGame;
import tanks.network.NetworkUtils;
import tanks.translation.Translation;

public class EventLoadCrusadeHotbar extends PersonalEvent
{
    public String title;
    public String subtitle;

    int index;
    boolean translate;

    public EventLoadCrusadeHotbar()
    {

    }

    public EventLoadCrusadeHotbar(String title, String subtitle, int index, boolean translate)
    {
        this.title = title;
        this.subtitle = subtitle;
        this.index = index;
        this.translate = translate;
    }

    @Override
    public void write(ByteBuf b)
    {
        NetworkUtils.writeString(b, this.title);
        NetworkUtils.writeString(b, this.subtitle);
        b.writeInt(this.index);
        b.writeBoolean(this.translate);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.title = NetworkUtils.readString(b);
        this.subtitle = NetworkUtils.readString(b);
        this.index = b.readInt();
        this.translate = b.readBoolean();
    }

    @Override
    public void execute()
    {
        if (this.clientID == null)
        {
            Game.player.hotbar.enabledItemBar = true;
            Game.player.hotbar.itemBar.showItems = true;
            Game.player.hotbar.enabledCoins = true;
        }

        if (this.translate)
            ((ScreenGame)(Game.screen)).title = Translation.translate(this.title, this.index);
        else
            ((ScreenGame)(Game.screen)).title = this.title;

        ((ScreenGame)(Game.screen)).subtitle = this.subtitle;
    }
}
