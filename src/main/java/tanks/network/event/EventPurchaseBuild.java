package tanks.network.event;

import tanks.Game;
import tanks.network.NetworkUtils;

import io.netty.buffer.ByteBuf;

public class EventPurchaseBuild extends PersonalEvent
{
    public String name;

    public EventPurchaseBuild()
    {

    }

    public EventPurchaseBuild(String name)
    {
        this.name = name;
    }

    @Override
    public void write(ByteBuf b)
    {
        NetworkUtils.writeString(b, this.name);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.name = NetworkUtils.readString(b);
    }

    @Override
    public void execute()
    {
        if (this.clientID == null)
        {
            Game.player.ownedBuilds.add(name);
        }
    }
}
