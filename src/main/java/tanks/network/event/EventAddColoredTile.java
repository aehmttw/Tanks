package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Drawing;
import tanks.Game;
import tanks.obstacle.ObstacleColorFlashing;

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
    public void execute()
    {
        Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA);

        for (int i = 0; i < length; i++)
        {
            for (int j = 0; j < width; j++)
            {
                ObstacleColorFlashing c = new ObstacleColorFlashing("bob", this.posX + i, this.posY + j, this.colorR, this.colorG, this.colorB, this.colorA);
                c.flashing = flashing;
                c.flashSpeedMultiplier = flashSpeedMultiplier;
                Game.obstacles.add(c);
            }
        }
    }
}
