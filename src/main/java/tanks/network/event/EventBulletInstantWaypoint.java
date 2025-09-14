package tanks.network.event;

import tanks.bullet.*;

public class EventBulletInstantWaypoint extends PersonalEvent implements IStackableEvent
{
    public int tank, bullet;
    public double posX, posY;


    public EventBulletInstantWaypoint()
    {

    }

    public EventBulletInstantWaypoint(BulletInstant b, double x, double y)
    {
        this.tank = b.tank.networkID;
        this.bullet = b.networkID;
        this.posX = x;
        this.posY = y;
    }


    @Override
    public void execute()
    {
        Bullet b = Bullet.idMap.get(this.bullet);

        if (b instanceof BulletInstant && this.clientID == null)
        {
            ((BulletInstant) b).xTargets.add(this.posX);
            ((BulletInstant) b).yTargets.add(this.posY);
        }
    }

    @Override
    public int getIdentifier()
    {
        return tank;
    }
}
