package tanks.network.event;

import basewindow.Color;
import tanks.*;
import tanks.tank.*;

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
            Team tm = Game.currentLevel.teamsMap.get(this.team);
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
}
