package tanks.modapi.events;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.event.PersonalEvent;
import tanks.gui.screen.ScreenGame;

public class EventCustomLevelEndConditionMet extends PersonalEvent
{
    @Override
    public void execute()
    {
        if (this.clientID == null && Game.screen instanceof ScreenGame)
            ((ScreenGame) Game.screen).remoteLevelEndConditionMet = true;
    }

    @Override
    public void write(ByteBuf b) {

    }

    @Override
    public void read(ByteBuf b) {

    }
}
