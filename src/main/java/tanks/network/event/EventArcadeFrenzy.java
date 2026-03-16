package tanks.network.event;

import tanks.Drawing;
import tanks.Game;
import tanks.minigames.Arcade;

import io.netty.buffer.ByteBuf;

public class EventArcadeFrenzy extends PersonalEvent
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
        if (Game.currentLevel instanceof Arcade && clientID == null)
        {
            ((Arcade) Game.currentLevel).frenzy = true;
            ((Arcade) Game.currentLevel).frenzyTime = ((Arcade) Game.currentLevel).age;
            Drawing.drawing.playSound("rampage.ogg");
        }
    }
}
