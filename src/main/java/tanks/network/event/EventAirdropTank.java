package tanks.network.event;

import tanks.Game;
import tanks.Team;
import tanks.tank.Crate;
import tanks.tank.Tank;
import tanks.tank.TankRemote;

public class EventAirdropTank extends EventTankCreate
{
    public EventAirdropTank()
    {

    }

    public EventAirdropTank(Tank t)
    {
        super(t);
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
            Game.movables.add(new Crate(new TankRemote(t)));
        }
    }
}
