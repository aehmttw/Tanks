package tanks.network.event;

import tanks.bullet.*;

public class EventBulletInstantWaypoint extends PersonalEvent implements IStackableEvent
{
    public int tank, bullet, targetIndex;
    public double posX, posY;
    public boolean zeroCooldown;


    public EventBulletInstantWaypoint()
    {

    }

    public EventBulletInstantWaypoint(BulletInstant b, double x, double y, int targetIndex)
    {
        this.tank = b.tank.networkID;
        this.bullet = b.networkID;
        this.zeroCooldown = b.item.item.cooldownBase <= 0;
        this.targetIndex = targetIndex;
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
    public boolean isStackable()
    {
        return zeroCooldown;
    }

    public int getIdentifier()
    {
        return IStackableEvent.f(IStackableEvent.f(tank) + targetIndex);
    }
}
