package tanks.network.event;

import tanks.Game;
import tanks.gui.screen.ScreenGame;

import java.util.UUID;

public class EventPlayerAutoReadyConfirm extends PersonalEvent
{
    public UUID playerID;

    public EventPlayerAutoReadyConfirm()
    {

    }

    public EventPlayerAutoReadyConfirm(UUID p)
    {
        this.playerID = p;
    }

    @Override
    public void execute()
    {
        if (this.clientID == null && Game.clientID.equals(playerID))
        {
            if (Game.screen instanceof ScreenGame)
                ((ScreenGame) Game.screen).ready = true;
        }
    }
}
