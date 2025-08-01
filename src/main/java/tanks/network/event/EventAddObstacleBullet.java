package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.network.NetworkUtils;
import tanks.obstacle.Obstacle;
import tanks.obstacle.ObstacleStackable;

import java.lang.reflect.InvocationTargetException;

public class EventAddObstacleBullet extends PersonalEvent
{
    public double posX;
    public double posY;
    public double colorR;
    public double colorG;
    public double colorB;
    public boolean success;

    public EventAddObstacleBullet()
    {

    }

    public EventAddObstacleBullet(Obstacle o, boolean success)
    {
        this.posX = o.posX;
        this.posY = o.posY;
        this.colorR = o.colorR;
        this.colorG = o.colorG;
        this.colorB = o.colorB;
        this.success = success;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeDouble(this.posX);
        b.writeDouble(this.posY);
        b.writeDouble(this.colorR);
        b.writeDouble(this.colorG);
        b.writeDouble(this.colorB);
        b.writeBoolean(this.success);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.posX = b.readDouble();
        this.posY = b.readDouble();
        this.colorR = b.readDouble();
        this.colorG = b.readDouble();
        this.colorB = b.readDouble();
        this.success = b.readBoolean();
    }

    @Override
    public void execute()
    {
        ObstacleStackable o = new ObstacleStackable("normal", this.posX / 50 - 0.5, this.posY / 50 - 0.5);
        o.colorR = this.colorR;
        o.colorG = this.colorG;
        o.colorB = this.colorB;

        for (int i = 0; i < o.stackColorR.length; i++)
        {
            o.stackColorR[i] = this.colorR;
            o.stackColorG[i] = this.colorG;
            o.stackColorB[i] = this.colorB;
        }

        o.setUpdate(true);
        o.shouldClip = true;
        o.clipFrames = 2;

        if (success)
            Game.addObstacle(o);
        else
            o.playDestroyAnimation(this.posX, this.posY, Game.tile_size);
    }
}
