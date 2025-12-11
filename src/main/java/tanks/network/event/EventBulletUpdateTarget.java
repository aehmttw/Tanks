package tanks.network.event;

import tanks.bullet.Bullet;
import tanks.tank.Tank;

public class EventBulletUpdateTarget extends PersonalEvent
{
    public int bullet;
    public int target;

    public EventBulletUpdateTarget()
    {

    }

    public EventBulletUpdateTarget(Bullet b)
    {
        this.bullet = b.networkID;

        if (b.homingTarget == null)
            this.target = -1;
        else
            this.target = b.homingTarget.networkID;
    }

    @Override
    public void execute()
    {
        Bullet b = Bullet.idMap.get(this.bullet);

        if (this.clientID == null && b != null)
        {
            b.homingTarget = Tank.idMap.get(this.target);
        }
    }
}
