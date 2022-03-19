package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.event.PersonalEvent;
import tanks.network.NetworkUtils;
import tanks.obstacle.Obstacle;

import java.lang.reflect.InvocationTargetException;

public class EventAddObstacle extends PersonalEvent
{
    public String name;
    public double posX;
    public double posY;
    public double stackHeight;
    public double startHeight;

    public EventAddObstacle()
    {

    }

    public EventAddObstacle(Obstacle o)
    {
        this.name = o.name;
        this.posX = o.posX;
        this.posY = o.posY;
        this.stackHeight = o.stackHeight;
        this.startHeight = o.startHeight;
    }

    @Override
    public void write(ByteBuf b)
    {
        NetworkUtils.writeString(b, this.name);
        b.writeDouble(this.posX);
        b.writeDouble(this.posY);
        b.writeDouble(this.stackHeight);
        b.writeDouble(this.startHeight);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.name = NetworkUtils.readString(b);
        this.posX = b.readDouble();
        this.posY = b.readDouble();
        this.stackHeight = b.readDouble();
        this.startHeight = b.readDouble();
    }

    @Override
    public void execute()
    {
        try
        {
            Obstacle o = Game.registryObstacle.getEntry(this.name).obstacle.getConstructor(String.class, double.class, double.class).newInstance(this.name, this.posX / 50 - 0.5, this.posY / 50 - 0.5);
            o.stackHeight = this.stackHeight;
            o.startHeight = this.startHeight;
            Game.obstacles.add(o);

        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e)
        {
            Game.exitToCrash(e);
        }
    }
}
