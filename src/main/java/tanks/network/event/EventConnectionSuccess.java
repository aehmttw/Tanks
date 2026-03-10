package tanks.network.event;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.ChatBox;
import tanks.gui.screen.ScreenPartyLobby;

import io.netty.buffer.ByteBuf;

public class EventConnectionSuccess extends PersonalEvent
{
    public EventConnectionSuccess()
    {

    }

    @Override
    public void execute()
    {
        if (this.clientID == null)
        {
            Game.screen = new ScreenPartyLobby();
            Game.eventsOut.add(new EventSendTankColors(Game.player));
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
