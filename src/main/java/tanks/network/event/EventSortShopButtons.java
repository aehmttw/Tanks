package tanks.network.event;

import tanks.Game;
import tanks.gui.screen.ScreenGame;

public class EventSortShopButtons extends PersonalEvent
{
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
