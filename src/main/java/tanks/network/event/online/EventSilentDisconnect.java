package tanks.network.event.online;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.gui.screen.ScreenPlayMultiplayer;
import tanks.network.Client;
import tanks.network.event.PersonalEvent;

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
