package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Movable;
import tanks.bullet.Laser;
import tanks.tank.Tank;

public class EventTankMimicLaser extends PersonalEvent implements IStackableEvent
{
    public int tank;
    public int tank2;
    public double range;

    public EventTankMimicLaser()
    {

    }

    public EventTankMimicLaser(Tank t, Tank t2, double range)
    {
        this.tank = t.networkID;

        if (t2 == null)
            this.tank2 = -1;
        else
            this.tank2 = t2.networkID;

        this.range = range;
    }

    @Override
    public void execute()
    {
        Tank t = Tank.idMap.get(tank);
        Tank t2 = Tank.idMap.get(tank2);

        for (Movable m: Game.movables)
        {
            if (m instanceof Laser && (((Laser) m).tank1 == t || ((Laser) m).tank2 == t))
                Game.removeMovables.add(m);
        }

        if (this.clientID == null && t != null && t2 != null)
        {
            Laser laser = new Laser(t.posX, t.posY, t.size / 2, t2.posX, t2.posY, t2.size / 2,
                    (this.range - Movable.distanceBetween(t, t2)) / this.range * 10, t2.getAngleInDirection(t.posX, t.posY),
                    t2.colorR, t2.colorG, t2.colorB);
            laser.tank1 = t;
            laser.tank2 = t2;
            Game.movables.add(laser);
        }
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(this.tank);
        b.writeInt(this.tank2);
        b.writeDouble(this.range);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.tank = b.readInt();
        this.tank2 = b.readInt();
        this.range = b.readDouble();
    }

    @Override
    public int getIdentifier()
    {
        return this.tank;
    }
}
