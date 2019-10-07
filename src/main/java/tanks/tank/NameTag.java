package tanks.tank;

import tanks.Drawing;
import tanks.Game;
import tanks.IDrawable;
import tanks.Movable;

public class NameTag implements IDrawable
{
    public Movable movable;
    public double ox;
    public double oy;
    public double oz;
    public double size = 20;
    public String name;
    public int drawLevel = 9;

    public double colorR;
    public double colorG;
    public double colorB;

    public NameTag(Movable m, double ox, double oy, double oz, String name, double r, double g, double b)
    {
        this.movable = m;
        this.ox = ox;
        this.oy = oy;
        this.oz = oz;
        this.name = name;

        this.colorR = r;
        this.colorG = g;
        this.colorB = b;
    }

    @Override
    public void draw()
    {
        Drawing.drawing.setFontSize(size);
        Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB);

        if (Game.enable3d)
            Drawing.drawing.drawText(movable.posX + ox, movable.posY + oy, movable.posZ + oz, name);
        else
            Drawing.drawing.drawText(movable.posX + ox, movable.posY + oy, name);
    }
}
