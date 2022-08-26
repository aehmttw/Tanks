package tanks.modapi.menus;

import tanks.Drawing;
import tanks.modapi.ModAPI;

public class CustomShape extends FixedMenu
{
    public enum types
    {fillRect, fillOval, drawRect, drawOval}

    public types type;

    public int posX;
    public int posY;
    public int sizeX;
    public int sizeY;
    public int colorR;
    public int colorB;
    public int colorG;
    public double colorA;

    private final long defineTime = System.currentTimeMillis();

    public CustomShape(types type, int x, int y, int sizeX, int sizeY, int r, int g, int b)
    {
        this(type, x, y, sizeX, sizeY, 0, r, g, b, 255);
    }

    public CustomShape(types type, int x, int y, int sizeX, int sizeY, int r, int g, int b, int a)
    {
        this(type, x, y, sizeX, sizeY, 0, r, g, b, a);
    }

    public CustomShape(types type, int x, int y, int sizeX, int sizeY, int duration, int r, int g, int b, int a)
    {
        this.type = type;
        this.posX = x;
        this.posY = y;
        this.sizeX = sizeX;
        this.sizeY = sizeY;

        this.duration = duration;
        this.colorR = r;
        this.colorG = g;
        this.colorB = b;
        this.colorA = a;
    }

    @Override
    public void draw()
    {
        Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA);

        double x = Drawing.drawing.interfaceSizeX / 2 + this.posX;
        double y = Drawing.drawing.interfaceSizeY / 2 + this.posY;

        switch (this.type)
        {
            case fillRect:
                Drawing.drawing.fillInterfaceRect(x, y, this.sizeX, this.sizeY);
                break;
            case fillOval:
                ModAPI.fixedShapes.fillOval(x, y, this.sizeX, this.sizeY);
                break;
            case drawRect:
                ModAPI.fixedShapes.drawRect(x, y, this.sizeX, this.sizeY);
                break;
            case drawOval:
                ModAPI.fixedShapes.drawOval(x, y, this.sizeX, this.sizeY);
                break;
        }
    }

    @Override
    public void update()
    {
        if (this.duration > 0 && System.currentTimeMillis() - defineTime > duration * 10L)
            ModAPI.removeMenus.add(this);
    }
}
