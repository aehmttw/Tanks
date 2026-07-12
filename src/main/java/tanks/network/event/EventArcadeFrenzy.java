package tanks.network.event;

import tanks.*;
import tanks.minigames.Arcade;

public class EventArcadeFrenzy extends PersonalEvent
{
    @Override
    public void execute()
    {
        if (Game.currentLevel instanceof Arcade && clientID == null)
        {
            ((Arcade) Game.currentLevel).frenzy = true;
            ((Arcade) Game.currentLevel).frenzyTime = ((Arcade) Game.currentLevel).age;
            Drawing.drawing.playSound("rampage.ogg");
        }
    }
}
