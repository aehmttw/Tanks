package basewindow.transformation;

import basewindow.BaseWindow;

public class ScaleAboutPoint extends Transformation
{
    public double x;
    public double y;
    public double z;

    public double posX;
    public double posY;
    public double posZ;

    public ScaleAboutPoint(BaseWindow window, double x, double y, double z, double posX, double posY, double posZ)
    {
        super(window);

        this.x = x;
        this.y = y;
        this.z = z;

        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
    }

    public void apply()
    {
        transform(window, x, y, z, posX, posY, posZ);
    }

    @Override
    public void applyToWindow()
    {

    }

    public static void transform(BaseWindow window, double x, double y, double z, double posX, double posY, double posZ)
    {
        transform(window, 1, 0, 0, 0,  0, 1, 0, 0,  0, 0, 1, 0,  posX * window.absoluteWidth, posY * window.absoluteHeight, posZ * window.absoluteDepth, 1);

        transform(window, x, 0, 0, 0, 0, y, 0, 0,  0, 0, z, 0,  0, 0, 0, 1);

        transform(window, 1, 0, 0, 0,  0, 1, 0, 0,  0, 0, 1, 0,  -posX * window.absoluteWidth, -posY * window.absoluteHeight, -posZ * window.absoluteDepth, 1);
    }
}
