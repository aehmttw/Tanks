package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.gui.screen.ScreenPendingJoinParty;

public class EventPendingJoinParty extends PersonalEvent
{
    @Override
    public void write(ByteBuf b)
    {

    }

    @Override
    public void read(ByteBuf b)
    {

    }

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
