package tanks.network.event;

import tanks.bullet.Bullet;

public class EventBulletUpdate extends PersonalEvent
{
    public int bullet;
    public double posX;
    public double posY;
    public double vX;
    public double vY;

    public EventBulletUpdate()
    {

    }

    public EventBulletUpdate(Bullet b)
    {
        this.bullet = b.networkID;
        this.posX = b.posX;
        this.posY = b.posY;
        this.vX = b.vX;
        this.vY = b.vY;
    }

    @Override
    public void execute()
    {
        Bullet b = Bullet.idMap.get(this.bullet);

        if (b != null && this.clientID == null)
        {
            b.posX = this.posX;
            b.posY = this.posY;
            b.vX = this.vX;
            b.vY = this.vY;
        }
    }

}
