package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.bullet.Bullet;
import tanks.obstacle.Obstacle;

public class EventObstacleHit extends PersonalEvent
{
    public double posX;
    public double posY;

    public double bulletX;
    public double bulletY;

    public EventObstacleHit()
    {

    }

    public EventObstacleHit(Obstacle o, Bullet b)
    {
        this.posX = o.posX;
        this.posY = o.posY;

        this.bulletX = b.posX;
        this.bulletY = b.posY;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeDouble(this.posX);
        b.writeDouble(this.posY);

        b.writeDouble(this.bulletX);
        b.writeDouble(this.bulletY);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.posX = b.readDouble();
        this.posY = b.readDouble();

        this.bulletX = b.readDouble();
        this.bulletY = b.readDouble();
    }

    @Override
    public void execute()
    {
        if (this.clientID != null)
            return;

        for (int i = 0; i < Game.obstacles.size(); i++)
        {
            Obstacle o = Game.obstacles.get(i);
            if (o.posX == this.posX && o.posY == this.posY)
            {
                o.reactToHit(this.bulletX, this.bulletY);
            }
        }
    }
}
