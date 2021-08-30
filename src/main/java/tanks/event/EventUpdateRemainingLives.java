package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Player;
import tanks.network.NetworkUtils;

import java.util.UUID;

public class EventUpdateRemainingLives extends PersonalEvent
{
    public UUID playerID;
    public int lives;

    public EventUpdateRemainingLives(Player p)
    {
        this.playerID = p.clientID;
        this.lives = p.remainingLives;
    }

    public EventUpdateRemainingLives()
    {

    }

    @Override
    public void write(ByteBuf b)
    {
        NetworkUtils.writeString(b, playerID.toString());
        b.writeInt(lives);
    }

    @Override
    public void read(ByteBuf b)
    {
        playerID = UUID.fromString(NetworkUtils.readString(b));
        lives = b.readInt();
    }

    @Override
    public void execute()
    {
        if (clientID == null && playerID.equals(Game.clientID))
        {
            Game.player.remainingLives = lives;
        }
    }
}
