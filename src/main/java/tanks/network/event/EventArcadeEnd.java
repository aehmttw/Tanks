package tanks.network.event;

import tanks.Game;
import tanks.minigames.Arcade;

public class EventArcadeEnd extends PersonalEvent
{
    public boolean win;

    public EventArcadeEnd(boolean win)
    {
        this.win = win;
    }

    public EventArcadeEnd()
    {

    }

    @Override
    public void execute()
    {
        if (clientID == null && Game.currentLevel instanceof Arcade)
        {
            ((Arcade) Game.currentLevel).survivedFrenzy = win;
        }
    }
}
