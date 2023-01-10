package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.gui.screen.ScreenArcadeBonuses;
import tanks.network.NetworkUtils;

public class EventArcadeBonuses extends PersonalEvent
{
    public ScreenArcadeBonuses.Bonus bonus1;
    public ScreenArcadeBonuses.Bonus bonus2;
    public ScreenArcadeBonuses.Bonus bonus3;

    public EventArcadeBonuses()
    {

    }

    public EventArcadeBonuses(ScreenArcadeBonuses.Bonus b1, ScreenArcadeBonuses.Bonus b2, ScreenArcadeBonuses.Bonus b3)
    {
        this.bonus1 = b1;
        this.bonus2 = b2;
        this.bonus3 = b3;
    }

    public void writeBonus(ScreenArcadeBonuses.Bonus bonus, ByteBuf b)
    {
        NetworkUtils.writeString(b, bonus.name);
        b.writeInt(bonus.value);
        b.writeDouble(bonus.red);
        b.writeDouble(bonus.green);
        b.writeDouble(bonus.blue);
    }

    public ScreenArcadeBonuses.Bonus readBonus(ByteBuf b)
    {
        return new ScreenArcadeBonuses.Bonus(NetworkUtils.readString(b), b.readInt(), b.readDouble(), b.readDouble(), b.readDouble());
    }


    @Override
    public void write(ByteBuf b)
    {
        writeBonus(bonus1, b);
        writeBonus(bonus2, b);
        writeBonus(bonus3, b);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.bonus1 = readBonus(b);
        this.bonus2 = readBonus(b);
        this.bonus3 = readBonus(b);
    }

    @Override
    public void execute()
    {
        if (clientID == null)
        {
            Game.screen = new ScreenArcadeBonuses(bonus1, bonus2, bonus3);
        }
    }
}
