package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.tank.Tank;

public class EventTankUpdateColor extends PersonalEvent
{
    public int tank;

    public double red;
    public double green;
    public double blue;

    public double red2;
    public double green2;
    public double blue2;

    public EventTankUpdateColor()
    {

    }

    public EventTankUpdateColor(Tank t)
    {
        tank = t.networkID;

        red = t.colorR;
        green = t.colorG;
        blue = t.colorB;

        red2 = t.turret.colorR;
        green2 = t.turret.colorG;
        blue2 = t.turret.colorB;
    }

    @Override
    public void execute()
    {
        Tank t = Tank.idMap.get(tank);

        if (t == null || this.clientID != null)
            return;

        t.colorR = red;
        t.colorG = green;
        t.colorB = blue;

        t.turret.colorR = red2;
        t.turret.colorG = green2;
        t.turret.colorB = blue2;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(this.tank);
        b.writeDouble(this.red);
        b.writeDouble(this.green);
        b.writeDouble(this.blue);
        b.writeDouble(this.red2);
        b.writeDouble(this.green2);
        b.writeDouble(this.blue2);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.tank = b.readInt();
        this.red = b.readDouble();
        this.green = b.readDouble();
        this.blue = b.readDouble();
        this.red2 = b.readDouble();
        this.green2 = b.readDouble();
        this.blue2 = b.readDouble();
    }
}
