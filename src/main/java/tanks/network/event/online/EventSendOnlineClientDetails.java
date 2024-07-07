package tanks.network.event.online;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.network.NetworkUtils;
import tanks.network.event.EventKick;
import tanks.network.event.PersonalEvent;
import tanksonline.AccessCode;
import tanksonline.PlayerMap;
import tanksonline.TanksOnlineServerHandler;
import tanksonline.screen.ScreenAccessCodeExpired;
import tanksonline.screen.ScreenHome;
import tanksonline.screen.ScreenInsertAccessCode;

import java.util.UUID;

public class EventSendOnlineClientDetails extends PersonalEvent implements IOnlineServerEvent
{
    public int version;
    public UUID clientID;
    public String username;
    public UUID computerID;

    public EventSendOnlineClientDetails()
    {

    }

    public EventSendOnlineClientDetails(int version, UUID clientID, String username, UUID computerID)
    {
        this.version = version;
        this.clientID = clientID;
        this.username = username;
        this.computerID = computerID;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(this.version);
        NetworkUtils.writeString(b, clientID.toString());
        NetworkUtils.writeString(b, username);
        NetworkUtils.writeString(b, computerID.toString());
    }

    @Override
    public void read(ByteBuf b)
    {
        this.version = b.readInt();
        this.clientID = UUID.fromString(NetworkUtils.readString(b));
        this.username = NetworkUtils.readString(b);
        this.computerID = UUID.fromString(NetworkUtils.readString(b));
    }

    @Override
    public void execute()
    {

    }

    @Override
    public void execute(TanksOnlineServerHandler s)
    {
        if (this.clientID == null || !Game.isOnlineServer)
            return;

        if (this.version != Game.network_protocol)
        {
            s.sendEventAndClose(new EventKick("You must be using " + Game.version + " to connect to Tanks Online!"));
            return;
        }

        if (Game.usernameInvalid(this.username) || this.username.equals(""))
        {
            s.sendEventAndClose(new EventKick("Invalid username!"));
            return;
        }

        if (!Game.chatFilter.filterChat(this.username).equals(this.username))
        {
            s.sendEventAndClose(new EventKick("Please pick a different username!"));
            return;
        }

        s.clientID = this.clientID;
        s.computerID = this.computerID;
        s.username = this.username;
        s.rawUsername = this.username;

        synchronized (s.server.connections)
        {
            for (int i = 0; i < s.server.connections.size(); i++)
            {
                if (this.clientID.equals(s.server.connections.get(i).clientID))
                {
                    s.sendEventAndClose(new EventKick("You are already connected to TanksOnline!"));
                    return;
                }
            }

            s.server.connections.add(s);
        }

        synchronized (PlayerMap.instance)
        {
            PlayerMap.instance.setupPlayer(s.computerID, s.username);
            PlayerMap.instance.save();

            AccessCode ac = PlayerMap.instance.getPlayer(s.computerID).accessCode;
            if (ac != null && ac.valid())
            {
                ScreenHome sc = new ScreenHome(s);
                sc.setScreen();
            }
            else if (ac != null && System.currentTimeMillis() > ac.expiration)
            {
                ScreenAccessCodeExpired sc = new ScreenAccessCodeExpired(s);
                sc.setScreen();
            }
            else
            {
                ScreenInsertAccessCode sc = new ScreenInsertAccessCode(s);
                sc.setScreen();
            }
        }
    }
}
