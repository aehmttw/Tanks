package tanks.network.event;

import tanks.Game;
import tanks.tank.Tank;

public class EventTankControllerAddVelocity extends PersonalEvent
{
    public int tank;
    public double vX;
    public double vY;
    public boolean recoil;

    public EventTankControllerAddVelocity()
    {

    }

    public EventTankControllerAddVelocity(Tank t, double vX, double vY, boolean recoil)
    {
        this.tank = t.networkID;
        this.vX = vX;
        this.vY = vY;
        this.recoil = recoil;
    }

    @Override
    public void execute()
    {
        Tank t = Tank.idMap.get(this.tank);

        if (t == Game.playerTank && this.clientID == null)
        {
            t.vX += this.vX;
            t.vY += this.vY;

            if (recoil)
            {
                t.recoilSpeed = t.getSpeed();
                t.tookRecoil = true;
                t.inControlOfMotion = false;
            }
        }
    }

}
