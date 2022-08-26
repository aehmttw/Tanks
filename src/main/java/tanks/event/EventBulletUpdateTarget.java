package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.bullet.Bullet;
import tanks.bullet.BulletHoming;
import tanks.tank.Tank;

public class EventBulletUpdateTarget extends PersonalEvent
{
    public int bullet;
    public int target;

    public EventBulletUpdateTarget()
    {

    }

    public EventBulletUpdateTarget(BulletHoming b)
    {
        this.bullet = b.networkID;

        if (b.target == null)
            this.target = -1;
        else
            this.target = b.target.networkID;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(this.bullet);
        b.writeInt(this.target);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.bullet = b.readInt();
        this.target = b.readInt();
    }

    @Override
    public void execute()
    {
        Bullet b = Bullet.idMap.get(this.bullet);

        if (b instanceof BulletHoming && this.clientID == null)
        {
            ((BulletHoming) b).target = Tank.idMap.get(this.target);
        }
    }
}
