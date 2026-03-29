package tanks.network.event;

import tanks.gui.screen.ScreenPartyLobby;

import io.netty.buffer.ByteBuf;

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
