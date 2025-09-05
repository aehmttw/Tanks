package tanks.network.event;

import tanks.*;
import tanks.bullet.Bullet;
import tanks.obstacle.*;
import tanks.tank.Tank;

public class EventObstacleBoostPanelEffect extends PersonalEvent
{
    public boolean isTank;
    public int networkID;
    public double posX;
    public double posY;

    public EventObstacleBoostPanelEffect(Movable m, Obstacle o)
    {
        this.posX = o.posX;
        this.posY = o.posY;

        if (m instanceof Tank)
        {
            this.networkID = ((Tank) m).networkID;
            this.isTank = true;
        }
        else
        {
            this.networkID = ((Bullet) m).networkID;
            this.isTank = false;
        }
    }

    public EventObstacleBoostPanelEffect()
    {

    }

    @Override
    public void execute()
    {
        if (clientID != null)
            return;

        Movable m;

        if (isTank)
            m = Tank.idMap.get(this.networkID);
        else
            m = Bullet.idMap.get(this.networkID);

        if (m == null)
            return;

        Obstacle o = Game.getObstacle(this.posX, this.posY);
        if (o instanceof ObstacleBoostPanel)
            ((ObstacleBoostPanel) o).addEntryEffect(m);
    }
}
