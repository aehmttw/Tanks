package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Player;
import tanks.network.NetworkUtils;

import java.util.UUID;

public class EventUpdateCoins extends PersonalEvent
{
    public UUID playerID;
    public int coins;

    public EventUpdateCoins()
    {

    }

    public EventUpdateCoins(Player p)
    {
        this.playerID = p.clientID;
        this.coins = p.hotbar.coins;
    }

    @Override
    public void write(ByteBuf b)
    {
        NetworkUtils.writeString(b, this.playerID.toString());
        b.writeInt(this.coins);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.playerID = UUID.fromString(NetworkUtils.readString(b));
        this.coins = b.readInt();
    }

    @Override
    public void execute()
    {
        if (this.clientID == null && this.playerID.equals(Game.clientID))
        {
            Game.player.hotbar.coins = coins;
        }
    }
}
