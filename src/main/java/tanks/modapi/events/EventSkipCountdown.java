package tanks.modapi.events;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.event.PersonalEvent;
import tanks.gui.screen.ScreenGame;

public class EventSkipCountdown extends PersonalEvent
{
    @Override
    public void execute()
    {
        if (Game.screen instanceof ScreenGame)
            ((ScreenGame) Game.screen).playing = true;
        else
            ScreenGame.skipCountdown = true;
    }

    @Override
    public void write(ByteBuf b) {

    }

    @Override
    public void read(ByteBuf b) {

    }
}
