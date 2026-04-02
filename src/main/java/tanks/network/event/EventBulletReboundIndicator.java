package tanks.network.event;

import basewindow.Color;
import tanks.Game;
import tanks.bullet.BulletReboundIndicator;

public class EventBulletReboundIndicator extends PersonalEvent
{
    public double posX, posY, posZ, size, maxAge;
    public Color color1, color2;

    public EventBulletReboundIndicator()
    {

    }

    public EventBulletReboundIndicator(BulletReboundIndicator b)
    {
        this.posX = b.posX;
        this.posY = b.posY;
        this.posZ = b.posZ;
        this.size = b.size;
        this.maxAge = b.maxAge;
        this.color1 = b.color;
        this.color2 = b.color2;
    }

    @Override
    public void execute()
    {
        if (this.clientID == null)
            Game.movables.add(new BulletReboundIndicator(
                this.posX,
                this.posY,
                this.posZ,
                this.size,
                this.maxAge,
                this.color1,
                this.color2
            ));
    }
}
