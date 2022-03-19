package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.ButtonList;
import tanks.gui.screen.ScreenGame;

public class EventSortShopButtons extends PersonalEvent
{

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
        if (Game.screen instanceof ScreenGame && this.clientID == null)
        {
            ScreenGame s = (ScreenGame) Game.screen;
            s.initializeShopList();
        }
    }
}
