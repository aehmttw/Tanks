package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Drawing;
import tanks.Game;
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
            for (int i = 0; i < s.shopItemButtons.size(); i++)
            {
                int page = i / (s.rows * 3);
                int offset = 0;

                if (page * s.rows * 3 + s.rows < s.shopItemButtons.size())
                    offset = -190;

                if (page * s.rows * 3 + s.rows * 2 < s.shopItemButtons.size())
                    offset = -380;

                s.shopItemButtons.get(i).posY = Drawing.drawing.interfaceSizeY / 2 + s.yoffset + (i % s.rows) * 60;

                if (i / s.rows % 3 == 0)
                    s.shopItemButtons.get(i).posX = Drawing.drawing.interfaceSizeX / 2 + offset;
                else if (i / s.rows % 3 == 1)
                    s.shopItemButtons.get(i).posX = Drawing.drawing.interfaceSizeX / 2 + offset + 380;
                else
                    s.shopItemButtons.get(i).posX = Drawing.drawing.interfaceSizeX / 2 + offset + 380 * 2;
            }
        }
    }
}
