package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.obstacle.Obstacle;
import tanks.obstacle.ObstacleSnow;

public class EventObstacleSnowMelt extends PersonalEvent implements IStackableEvent
{
    public double posX;
    public double posY;
    public double depth;

    public EventObstacleSnowMelt()
    {

    }

    public EventObstacleSnowMelt(double x, double y, double depth)
    {
        this.posX = x;
        this.posY = y;
        this.depth = depth;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeDouble(this.posX);
        b.writeDouble(this.posY);
        b.writeDouble(this.depth);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.posX = b.readDouble();
        this.posY = b.readDouble();
        this.depth = b.readDouble();
    }

    @Override
    public void execute()
    {
        if (this.clientID != null)
            return;

        for (int i = 0; i < Game.obstacles.size(); i++)
        {
            Obstacle o = Game.obstacles.get(i);
            if (o instanceof ObstacleSnow && o.posX == this.posX && o.posY == this.posY)
            {
                ((ObstacleSnow) o).depth = this.depth;

                if (depth < 0)
                    Game.removeObstacles.add(o);
            }
        }
    }

    @Override
    public int getIdentifier()
    {
        return IStackableEvent.f((int) (this.posX + IStackableEvent.f((int) this.posY)));
    }
}
