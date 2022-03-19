package tanks.event.online;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.event.PersonalEvent;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.gui.screen.ScreenPlayMultiplayer;
import tanks.network.Client;

public class EventSilentDisconnect extends PersonalEvent
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
        if (this.clientID == null)
        {
            Game.cleanUp();
            ScreenPartyLobby.isClient = false;
            Client.handler.ctx.close();
            Game.screen = new ScreenPlayMultiplayer();
        }
    }
}
