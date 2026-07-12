package tanks.network.event;

import tanks.Game;
import tanks.gui.screen.ScreenPendingJoinParty;

public class EventPendingJoinParty extends PersonalEvent
{
    @Override
    public void execute()
    {
        if (clientID == null)
        {
            Game.screen = new ScreenPendingJoinParty();
            Game.eventsOut.add(new EventSendTankColors(Game.player));
        }
    }
}
