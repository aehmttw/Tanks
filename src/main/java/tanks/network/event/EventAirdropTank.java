package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Team;
import tanks.tank.Crate;
import tanks.tank.Tank;
import tanks.tank.TankRemote;

public class EventAirdropTank extends EventTankCreate
{
    public double colorR;
    public double colorG;
    public double colorB;
    public double colorR2;
    public double colorG2;
    public double colorB2;

    public EventAirdropTank()
    {

    }

    public EventAirdropTank(Tank t)
    {
        super(t);

        this.colorR = t.colorR;
        this.colorG = t.colorG;
        this.colorB = t.colorB;
        this.colorR2 = t.secondaryColorR;
        this.colorG2 = t.secondaryColorG;
        this.colorB2 = t.secondaryColorB;
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
            t.colorR = colorR;
            t.colorG = colorG;
            t.colorB = colorB;
            t.secondaryColorR = colorR2;
            t.secondaryColorG = colorG2;
            t.secondaryColorB = colorB2;
            Game.movables.add(new Crate(new TankRemote(t)));
        }
    }

    @Override
    public void read(ByteBuf b)
    {
        super.read(b);
        this.colorR = b.readDouble();
        this.colorG = b.readDouble();
        this.colorB = b.readDouble();
        this.colorR2 = b.readDouble();
        this.colorG2 = b.readDouble();
        this.colorB2 = b.readDouble();
    }

    @Override
    public void write(ByteBuf b)
    {
        super.write(b);
        b.writeDouble(this.colorR);
        b.writeDouble(this.colorG);
        b.writeDouble(this.colorB);
        b.writeDouble(this.colorR2);
        b.writeDouble(this.colorG2);
        b.writeDouble(this.colorB2);
    }
}
