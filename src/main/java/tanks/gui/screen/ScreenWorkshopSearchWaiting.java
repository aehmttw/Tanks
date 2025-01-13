package tanks.gui.screen;

import tanks.Game;

public class ScreenWorkshopSearchWaiting extends ScreenWaiting
{
    public ScreenWorkshopSearchWaiting()
    {
        super("Searching...");
    }

    @Override
    public void update()
    {
        super.update();

        if (Game.steamNetworkHandler.workshop.totalResults >= 0)
        {
            Game.screen = new ScreenWorkshopLevels();
        }
    }
}
