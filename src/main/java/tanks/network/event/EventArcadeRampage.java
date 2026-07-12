package tanks.network.event;

import tanks.Game;
import tanks.minigames.Arcade;

import io.netty.buffer.ByteBuf;

public class EventArcadeRampage extends PersonalEvent
{
    public int power;

    public EventArcadeRampage(int power)
    {
        this.power = power;
    }

    public EventArcadeRampage()
    {

    }

    @Override
    public void execute()
    {
        if (clientID == null && Game.currentLevel instanceof Arcade)
        {
            ((Arcade) Game.currentLevel).setRampage(power);
        }
    }
}
