package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Player;
import tanks.gui.screen.ScreenPartyHost;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.network.ConnectedPlayer;
import tanks.network.NetworkUtils;
import tanks.network.ServerHandler;

import java.util.UUID;

public class EventChangeUsername extends PersonalEvent
{
    public UUID id;
    public String username;

    public EventChangeUsername() {}

    public EventChangeUsername(UUID id, String s)
    {
        this.id = id;
        this.username = s;
    }

    @Override
    public void execute()
    {
        if (this.id == null)
            return;

        if (ScreenPartyHost.isServer)
        {
            for (ServerHandler s : ScreenPartyHost.server.connections)
            {
                if (s.clientID.equals(this.id))
                {
                    if (Game.enableChatFilter)
                        s.username = Game.chatFilter.filterChat(username);
                    else
                        s.username = username;
                }
            }
        }
        else
        {
            for (ConnectedPlayer p : ScreenPartyLobby.connections)
            {
                if (p.clientID.equals(this.id))
                {
                    if (Game.enableChatFilter)
                        p.username = Game.chatFilter.filterChat(username);
                    else
                        p.username = username;
                    break;
                }
            }
        }

        for (Player p : Game.players)
        {
            if (p.clientID.equals(this.clientID))
            {
                p.username = username;
                break;
            }
        }
    }

    @Override
    public void write(ByteBuf b)
    {
        NetworkUtils.writeString(b, this.id.toString());
        NetworkUtils.writeString(b, this.username);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.id = UUID.fromString(NetworkUtils.readString(b));
        this.username = NetworkUtils.readString(b);
    }
}
