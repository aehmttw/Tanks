package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.bullet.Bullet;
import tanks.bullet.BulletInstant;
import tanks.tank.Tank;

public class EventBulletDestroyed extends PersonalEvent
{
    public int bullet;

    public EventBulletDestroyed()
    {

    }

    public EventBulletDestroyed(Bullet b)
    {
        this.bullet = b.networkID;
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

        b.destroy = true;

        if (!Bullet.freeIDs.contains(b.networkID))
        {
            Bullet.freeIDs.add(b.networkID);
            Bullet.idMap.remove(b.networkID);
        }
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(this.bullet);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.bullet = b.readInt();
    }
}
