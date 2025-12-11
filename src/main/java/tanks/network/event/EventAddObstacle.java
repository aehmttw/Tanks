package tanks.network.event;

import tanks.Game;
import tanks.obstacle.Obstacle;

import java.lang.reflect.InvocationTargetException;

public class EventAddObstacle extends PersonalEvent
{
    public String name;
    public double posX;
    public double posY;
    public String metadata;

    public EventAddObstacle()
    {

    }

    public EventAddObstacle(Obstacle o)
    {
        this.name = o.name;
        this.posX = o.posX;
        this.posY = o.posY;
        this.metadata = o.getMetadata();
    }

    @Override
    public void execute()
    {
        try
        {
            Obstacle o = Game.registryObstacle.getEntry(this.name).obstacle.getConstructor(String.class, double.class, double.class).newInstance(this.name, this.posX / 50 - 0.5, this.posY / 50 - 0.5);
            o.setMetadata(this.metadata);
            Game.obstacles.add(o);

        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e)
        {
            Game.exitToCrash(e);
        }
    }
}
