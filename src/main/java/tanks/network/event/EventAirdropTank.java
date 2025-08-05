package tanks.network.event;

import basewindow.Color;
import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Team;
import tanks.network.NetworkUtils;
import tanks.tank.Crate;
import tanks.tank.Tank;
import tanks.tank.TankRemote;

public class EventAirdropTank extends EventTankCreate
{
    public Color color = new Color();
    public Color color2 = new Color();
    public double height;

    public EventAirdropTank()
    {

    }

    public EventAirdropTank(Tank t, double height)
    {
        super(t);

        this.color.set(t.color);
        this.color2.set(t.secondaryColor);
        this.height = height;
    }

    @Override
    public void execute()
    {
        if (this.clientID == null)
        {
            Tank t = Game.registryTank.getEntry(this.type).getTank(this.posX, this.posY, this.angle);
            Team tm = (Team) Game.currentLevel.teamsMap.get(this.team);
            if (this.team.equals("**"))
            {
                tm = Game.enemyTeam;
            }

            t.team = tm;
            t.color.set(this.color);
            t.secondaryColor.set(this.color2);
            Game.movables.add(new Crate(new TankRemote(t), height));
        }
    }

    @Override
    public void read(ByteBuf b)
    {
        super.read(b);
        NetworkUtils.readColor(b, this.color);
        NetworkUtils.readColor(b, this.color2);
        this.height = b.readDouble();
    }

    @Override
    public void write(ByteBuf b)
    {
        super.write(b);
        NetworkUtils.writeColor(b, this.color);
        NetworkUtils.writeColor(b, this.color2);
        b.writeDouble(this.height);
    }
}
