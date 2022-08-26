package tanks.modapi.events;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.event.PersonalEvent;
import tanks.modapi.ModAPI;
import tanks.network.NetworkUtils;
import tanks.obstacle.Obstacle;

import java.lang.reflect.Constructor;

public class EventFillObstacle extends PersonalEvent
{
    public int startX;
    public int startY;
    public int endX;
    public int endY;
    public String registryName;
    public double stackHeight;
    public double startHeight;

    public EventFillObstacle()
    {
    }

    public EventFillObstacle(int startX, int startY, int endX, int endY, String registryName, double stackHeight, double startHeight)
    {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.registryName = registryName;
        this.stackHeight = stackHeight;
        this.startHeight = startHeight;
    }

    @Override
    public void execute()
    {
        try
        {
            Constructor<? extends Obstacle> c = Game.registryObstacle.getEntry(registryName).obstacle
                    .getConstructor(String.class, double.class, double.class);

            for (int x = startX; x <= endX; x++)
            {
                for (int y = startY; y <= endY; y++)
                {
                    Obstacle o = c.newInstance(registryName, x, y);
                    o.stackHeight = stackHeight;
                    o.startHeight = startHeight;
                    ModAPI.addObject(o);
                }
            }
        }
        catch (Exception e)
        {
            Game.exitToCrash(e);
        }
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(this.startX);
        b.writeInt(this.startY);
        b.writeInt(this.endX);
        b.writeInt(this.endY);

        NetworkUtils.writeString(b, this.registryName);
        b.writeDouble(this.stackHeight);
        b.writeDouble(this.startHeight);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.startX = b.readInt();
        this.startY = b.readInt();
        this.endX = b.readInt();
        this.endY = b.readInt();

        this.registryName = NetworkUtils.readString(b);
        this.stackHeight = b.readDouble();
        this.startHeight = b.readDouble();
    }
}
