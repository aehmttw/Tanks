package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.minigames.Arcade;

public class EventArcadeEnd extends PersonalEvent
{
    public boolean win;

    public EventArcadeEnd(boolean win)
    {
        this.win = win;
    }

    public EventArcadeEnd()
    {

    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeBoolean(win);
    }

    @Override
    public void read(ByteBuf b)
    {
        win = b.readBoolean();
    }

    @Override
    public void execute()
    {
        if (clientID == null && Game.currentLevel instanceof Arcade)
        {
            ((Arcade) Game.currentLevel).survivedFrenzy = win;
        }
    }
}
