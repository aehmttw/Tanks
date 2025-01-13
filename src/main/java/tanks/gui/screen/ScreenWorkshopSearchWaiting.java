package tanks.gui.screen;

import tanks.Game;

public class ScreenWorkshopSearchWaiting extends ScreenWaitingCancelable
{
    public ScreenWorkshopSearchWaiting()
    {
        super("Searching...");
        this.previous = new ScreenSteamWorkshop();
    }

    @Override
    public void update()
    {
        super.update();

        if (Game.steamNetworkHandler.workshop.totalResults >= 0)
        {
            Game.screen = new ScreenWorkshopCreations();
        }
    }
}
