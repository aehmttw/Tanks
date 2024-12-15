package tanks.tank;

import tanks.Game;
import tanks.obstacle.Obstacle;

import java.util.HashSet;

public interface IAvoidObject
{
    HashSet<IAvoidObject> avoidances = new HashSet<>();

    double getRadius();

    double getSeverity(double posX, double posY);

    static boolean exists(Obstacle o)
    {
        if (o == null)
            return false;

        return Game.getObstacle(o.posX, o.posY) == o;
    }
}
