package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.tank.Explosion;
import tanks.tank.Tank;

public class EventExplosion extends PersonalEvent
{
    public int tank;
    public double posX;
    public double posY;
    public double radius;
    public boolean destroysObstacles;

    public EventExplosion()
    {

    }

    public EventExplosion(Explosion e)
    {
        this.tank = e.tank.networkID;
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
            Tank t = Tank.idMap.get(tank);

            if (tank == -1)
                t = Game.dummyTank;

            if (t == null)
                return;

            Explosion e = new Explosion(this.posX, this.posY, this.radius, 0, destroysObstacles, t);
            e.explode();
        }
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(this.tank);
        b.writeDouble(this.posX);
        b.writeDouble(this.posY);
        b.writeDouble(this.radius);
        b.writeBoolean(this.destroysObstacles);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.tank = b.readInt();
        this.posX = b.readDouble();
        this.posY = b.readDouble();
        this.radius = b.readDouble();
        this.destroysObstacles = b.readBoolean();
    }
}
