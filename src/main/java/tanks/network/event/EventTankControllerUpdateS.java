package tanks.network.event;

import tanks.Game;
import tanks.tank.*;

public class EventTankControllerUpdateS extends EventTankUpdate
{
    public boolean forced;

    public EventTankControllerUpdateS()
    {

    }

    public EventTankControllerUpdateS(Tank t, boolean forced, boolean recoil)
    {
        super(t);
        this.forced = forced;
    }

    @Override
    public void execute()
    {
        Tank t = Tank.idMap.get(this.tank);

        if (this.clientID != null || (!(t instanceof TankRemote) &&
            (!(t instanceof TankPlayerController) || (!this.forced && Game.clientID.equals(((TankPlayerController) t).clientID)))))
            return;

        if (t instanceof TankPlayerController && Game.clientID.equals(((TankPlayerController) t).clientID))
        {
            TankPlayerController p = (TankPlayerController) t;
            p.interpolatedOffX = this.posX - (t.posX - p.interpolatedOffX * (TankPlayerController.interpolationTime - p.interpolatedProgress) / TankPlayerController.interpolationTime);
            p.interpolatedOffY = this.posY - (t.posY - p.interpolatedOffY * (TankPlayerController.interpolationTime - p.interpolatedProgress) / TankPlayerController.interpolationTime);
            p.interpolatedProgress = 0;
        }

        if (t instanceof TankRemote)
            ((TankRemote) t).updatePositions(this.posX, this.posY, this.vX, this.vY, this.angle, this.pitch);

        t.posX = this.posX;
        t.posY = this.posY;
        t.vX = this.vX;
        t.vY = this.vY;

        t.angle = this.angle;
    }
}
