package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.obstacle.Obstacle;

public class EventObstacleDestroy extends PersonalEvent
{
    public double posX;
    public double posY;

    boolean effect;
    public double effectX;
    public double effectY;
    public double radius;

    public EventObstacleDestroy()
    {

    }

    public EventObstacleDestroy(double x, double y)
    {
        this.posX = x;
        this.posY = y;
        effect = false;
    }

    public EventObstacleDestroy(double x, double y, double ex, double ey, double rad)
    {
        this.posX = x;
        this.posY = y;
        effect = true;
        this.effectX = ex;
        this.effectY = ey;
        this.radius = rad;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeDouble(this.posX);
        b.writeDouble(this.posY);
        b.writeBoolean(this.effect);
        b.writeDouble(this.effectX);
        b.writeDouble(this.effectY);
        b.writeDouble(this.radius);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.posX = b.readDouble();
        this.posY = b.readDouble();
        this.effect = b.readBoolean();
        this.effectX = b.readDouble();
        this.effectY = b.readDouble();
        this.radius = b.readDouble();
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
                if (effect)
                    o.playDestroyAnimation(this.effectX, this.effectY, this.radius);

                Game.removeObstacles.add(o);
            }
        }
    }
}
