package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Drawing;
import tanks.Game;
import tanks.obstacle.ObstacleColor;

public class EventAddColoredTile extends PersonalEvent
{
    public double posX;
    public double posY;
    public double length;
    public double width;
    public double colorR;
    public double colorG;
    public double colorB;
    public double colorA;
    public boolean flashing;
    public double flashSpeedMultiplier;

    public EventAddColoredTile()
    {

    }

    public EventAddColoredTile(double x, double y, double length, double width, double r, double g, double b, double a, boolean flashing, double flashSpeedMultiplier)
    {
        this.posX = x;
        this.posY = y;
        this.length = length;
        this.width = width;
        this.colorR = r;
        this.colorG = g;
        this.colorB = b;
        this.colorA = a;
        this.flashing = flashing;
        this.flashSpeedMultiplier = flashSpeedMultiplier;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeDouble(this.posX);
        b.writeDouble(this.posY);
        b.writeDouble(this.length);
        b.writeDouble(this.width);
        b.writeDouble(this.colorR);
        b.writeDouble(this.colorG);
        b.writeDouble(this.colorB);
        b.writeDouble(this.colorA);
        b.writeBoolean(this.flashing);
        b.writeDouble(this.flashSpeedMultiplier);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.posX = b.readDouble();
        this.posY = b.readDouble();
        this.length = b.readDouble();
        this.width = b.readDouble();
        this.colorR = b.readDouble();
        this.colorG = b.readDouble();
        this.colorB = b.readDouble();
        this.colorA = b.readDouble();
        this.flashing = b.readBoolean();
        this.flashSpeedMultiplier = b.readDouble();
    }

    @Override
    public void execute()
    {
        Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA);

        for (int i = 0; i < length; i++)
        {
            for (int j = 0; j < width; j++)
            {
                ObstacleColor c = new ObstacleColor("bob", this.posX + i, this.posY + j, this.colorR, this.colorG, this.colorB, this.colorA);
                c.flashing = flashing;
                c.flashSpeedMultiplier = flashSpeedMultiplier;
                Game.obstacles.add(c);
            }
        }
    }
}
