package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.bullet.Bullet;
import tanks.bullet.BulletInstant;

public class EventBulletInstantWaypoint extends PersonalEvent
{
    public int bullet;
    public double posX;
    public double posY;


    public EventBulletInstantWaypoint()
    {

    }

    public EventBulletInstantWaypoint(BulletInstant b, double x, double y)
    {
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

}
