package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Crusade;
import tanks.Game;
import tanks.gui.screen.ScreenGame;

public class EventSetLevelVersus extends PersonalEvent
{
    public EventSetLevelVersus()
    {

    }

    @Override
    public void execute()
    {
        if (this.clientID == null && Game.screen instanceof ScreenGame)
        {
            ((ScreenGame) Game.screen).isVersus = true;
        }
    }

    @Override
    public void write(ByteBuf b)
    {

    }

    @Override
    public void read(ByteBuf b)
    {

    }
}
