package tanks.network.event;

import tanks.Game;
import tanks.gui.screen.*;
import tanks.network.Client;
import tanks.network.NetworkUtils;

import io.netty.buffer.ByteBuf;

import java.util.UUID;

public class EventKick extends PersonalEvent
{
    public String reason;

    public EventKick()
    {

    }

    public EventKick(String reason)
    {
        this.reason = reason;
    }

    // Rejections must also be readable by peers using the previous protocol.
    @Override
    public void write(ByteBuf b)
    {
        NetworkUtils.writeString(b, reason);
        NetworkUtils.writeString(b, clientID == null ? null : clientID.toString());
    }

    @Override
    public void read(ByteBuf b)
    {
        reason = NetworkUtils.readString(b);
        String id = NetworkUtils.readString(b);
        clientID = id == null ? null : UUID.fromString(id);
    }

    @Override
    public void execute()
    {
        if (this.clientID == null)
        {
            Game.cleanUp();
            ScreenPartyLobby.isClient = false;

            Client.handler.close();

            if (!(Game.screen instanceof ScreenKicked))
                Game.screen = new ScreenKicked(reason);
        }
    }
}
