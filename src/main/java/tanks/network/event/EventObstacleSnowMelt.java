package tanks.network.event;

import tanks.Game;
import tanks.obstacle.*;

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
