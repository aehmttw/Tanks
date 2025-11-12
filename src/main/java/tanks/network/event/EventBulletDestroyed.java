package tanks.network.event;

import tanks.bullet.*;

public class EventBulletDestroyed extends PersonalEvent implements IStackableEvent
{
    public int tank, bullet;
    public double posX, posY;
    public boolean zeroCooldown;

    public EventBulletDestroyed()
    {

    }

    public EventBulletDestroyed(Bullet b)
    {
        this.tank = b.tank.networkID;
        this.bullet = b.networkID;
        this.zeroCooldown = b.item.item.cooldownBase <= 0;
        this.posX = b.posX;
        this.posY = b.posY;
    }

    @Override
    public void execute()
    {
        if (this.clientID != null)
            return;

        Bullet b = Bullet.idMap.get(bullet);

        if (b == null)
            return;

        if (b instanceof BulletInstant)
            ((BulletInstant) b).remoteShoot();
        else
        {
            b.posX = posX;
            b.posY = posY;
        }

        b.destroy = true;
        Bullet.idMap.remove(b.networkID);
    }

    @Override
    public boolean isStackable()
    {
        return zeroCooldown;
    }

    @Override
    public int getIdentifier()
    {
        return tank;
    }
}
