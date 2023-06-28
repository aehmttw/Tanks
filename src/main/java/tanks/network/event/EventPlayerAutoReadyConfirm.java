package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.gui.screen.ScreenGame;
import tanks.network.NetworkUtils;

import java.util.UUID;

public class EventPlayerAutoReadyConfirm extends PersonalEvent
{
    public UUID playerID;

    public EventPlayerAutoReadyConfirm()
    {

    }

    public EventPlayerAutoReadyConfirm(UUID p)
    {
        this.playerID = p;
    }

    @Override
    public void execute()
    {
        if (this.clientID == null && Game.clientID.equals(playerID))
        {
            if (Game.screen instanceof ScreenGame)
                ((ScreenGame) Game.screen).ready = true;
        }
    }

    @Override
    public void write(ByteBuf b)
    {
        NetworkUtils.writeString(b, playerID.toString());
    }

    @Override
    public void read(ByteBuf b)
    {
        this.playerID = UUID.fromString(NetworkUtils.readString(b));
    }
}
