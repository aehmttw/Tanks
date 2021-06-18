package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Effect;
import tanks.Game;
import tanks.obstacle.Obstacle;
import tanks.obstacle.ObstacleShrubbery;

public class EventObstacleDestroy extends PersonalEvent
{
    public double posX;
    public double posY;

    public EventObstacleDestroy()
    {

    }

    public EventObstacleDestroy(double x, double y)
    {
        this.posX = x;
        this.posY = y;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeDouble(this.posX);
        b.writeDouble(this.posY);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.posX = b.readDouble();
        this.posY = b.readDouble();
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
                Game.removeObstacles.add(o);
            }
        }
    }
}
