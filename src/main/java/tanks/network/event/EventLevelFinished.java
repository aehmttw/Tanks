package tanks.network.event;

import tanks.gui.screen.ScreenGame;

public class EventLevelFinished extends PersonalEvent
{
    public EventLevelFinished()
    {

    }

    @Override
    public void execute()
    {
        if (this.clientID != null)
            return;

        ScreenGame.finished = true;
    }
}
