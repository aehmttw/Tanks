package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.tank.Explosion;

public class EventExplosion extends PersonalEvent
{
    public double posX;
    public double posY;
    public double radius;
    public boolean destroysObstacles;

    public EventExplosion()
    {

    }

    public EventExplosion(Explosion e)
    {
        this.posX = e.posX;
        this.posY = e.posY;
        this.radius = e.radius;
        this.destroysObstacles = e.destroysObstacles;
    }

    @Override
    public void execute()
    {
        if (clientID == null)
        {
            Explosion e = new Explosion(this.posX, this.posY, this.radius, 0, destroysObstacles, Game.dummyTank);
            e.explode();
        }
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeDouble(this.posX);
        b.writeDouble(this.posY);
        b.writeDouble(this.radius);
        b.writeBoolean(this.destroysObstacles);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.posX = b.readDouble();
        this.posY = b.readDouble();
        this.radius = b.readDouble();
        this.destroysObstacles = b.readBoolean();
    }
}
