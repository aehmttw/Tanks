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
        posX = b.posX;
        posY = b.posY;
        posZ = b.posZ;
        size = b.size;
        maxAge = b.maxAge;
        color1 = b.color;
        color2 = b.color2;
    }

    @Override
    public void execute()
    {
        if (this.clientID == null)
            Game.movables.add(new BulletReboundIndicator(posX, posY, posZ, size, maxAge, color1, color2));
    }
}
