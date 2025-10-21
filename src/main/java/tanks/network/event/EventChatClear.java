package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.gui.screen.ScreenPartyLobby;

public class EventChatClear extends PersonalEvent
{
    public EventChatClear()
    {

    }

    @Override
    public void execute()
    {
        if (this.clientID == null)
        {
            ScreenPartyLobby.chat.clear();
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
