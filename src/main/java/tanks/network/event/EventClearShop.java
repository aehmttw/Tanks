package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.gui.ButtonList;
import tanks.gui.screen.ScreenGame;

import java.util.ArrayList;

public class EventClearShop extends PersonalEvent
{
    public EventClearShop()
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
        if (Game.screen instanceof ScreenGame)
        {
            ((ScreenGame) Game.screen).npcShopList = new ButtonList(new ArrayList<>(), 0, 0, (int) ScreenGame.shopOffset, -30);

            ((ScreenGame) Game.screen).shopList = new ButtonList(new ArrayList<>(), 0, 0, (int) ScreenGame.shopOffset, -30);
            ((ScreenGame) Game.screen).shopItemButtons = new ArrayList<>();
            ((ScreenGame) Game.screen).shop = new ArrayList<>();
        }
    }
}
