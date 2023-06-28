package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.tank.Tank;

public class EventTankUpdateColor extends PersonalEvent implements IStackableEvent
{
    public int tank;

    public double red;
    public double green;
    public double blue;

    public double red2;
    public double green2;
    public double blue2;

    public boolean tertiaryColor;
    public double red3;
    public double green3;
    public double blue3;

    public EventTankUpdateColor()
    {

    }

    public EventTankUpdateColor(Tank t)
    {
        tank = t.networkID;

        red = t.colorR;
        green = t.colorG;
        blue = t.colorB;

        red2 = t.secondaryColorR;
        green2 = t.secondaryColorG;
        blue2 = t.secondaryColorB;

        tertiaryColor = t.enableTertiaryColor;
        red3 = t.tertiaryColorR;
        green3 = t.tertiaryColorG;
        blue3 = t.tertiaryColorB;
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

        t.secondaryColorR = red2;
        t.secondaryColorG = green2;
        t.secondaryColorB = blue2;

        t.enableTertiaryColor = tertiaryColor;
        t.tertiaryColorR = red3;
        t.tertiaryColorG = green3;
        t.tertiaryColorB = blue3;
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
        b.writeBoolean(this.tertiaryColor);
        b.writeDouble(this.red3);
        b.writeDouble(this.green3);
        b.writeDouble(this.blue3);
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
        this.tertiaryColor = b.readBoolean();
        this.red3 = b.readDouble();
        this.green3 = b.readDouble();
        this.blue3 = b.readDouble();
    }

    @Override
    public int getIdentifier()
    {
        return this.tank;
    }
}
