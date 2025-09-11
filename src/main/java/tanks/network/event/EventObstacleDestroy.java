package tanks.network.event;

import tanks.Game;
import tanks.obstacle.Obstacle;

public class EventObstacleDestroy extends PersonalEvent
{
    public double posX;
    public double posY;

    public boolean effect;
    public double effectX;
    public double effectY;
    public double radius;
    public String name;

    public EventObstacleDestroy()
    {

    }

    public EventObstacleDestroy(double x, double y, String name)
    {
        this.posX = x;
        this.posY = y;
        this.effect = false;
        this.name = name;
    }

    public EventObstacleDestroy(double x, double y, String name, double ex, double ey, double rad)
    {
        this.posX = x;
        this.posY = y;
        this.effect = true;
        this.effectX = ex;
        this.effectY = ey;
        this.radius = rad;
        this.name = name;
    }
    @Override
    public void execute()
    {
        if (this.clientID != null)
            return;

        Obstacle o = Game.getObstacle(posX, posY);

        if (o == null || !o.name.equals(name))
            return;

        if (effect)
            o.playDestroyAnimation(this.effectX, this.effectY, this.radius);

        Game.removeObstacles.add(o);
    }
}
