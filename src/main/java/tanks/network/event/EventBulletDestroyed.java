package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.bullet.Bullet;
import tanks.bullet.BulletInstant;

public class EventBulletDestroyed extends PersonalEvent
{
    public int bullet;
    public double posX;
    public double posY;

    public EventBulletDestroyed()
    {

    }

    public EventBulletDestroyed(Bullet b)
    {
        this.bullet = b.networkID;
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
        b.writeDouble(this.posX);
        b.writeDouble(this.posY);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.bullet = b.readInt();
        this.posX = b.readDouble();
        this.posY = b.readDouble();
    }
}
