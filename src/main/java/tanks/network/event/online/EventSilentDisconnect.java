package tanks.network.event.online;

import tanks.Game;
import tanks.gui.screen.*;
import tanks.network.Client;
import tanks.network.event.PersonalEvent;

import io.netty.buffer.ByteBuf;

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
