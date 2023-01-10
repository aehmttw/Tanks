package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.gui.ButtonList;
import tanks.gui.screen.ScreenGame;
import tanks.minigames.Arcade;

import java.util.ArrayList;

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
    public void write(ByteBuf b)
    {
        b.writeInt(power);
    }

    @Override
    public void read(ByteBuf b)
    {
        power = b.readInt();
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
