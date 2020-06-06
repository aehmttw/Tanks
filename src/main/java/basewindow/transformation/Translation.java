package basewindow.transformation;

import basewindow.BaseWindow;

public class Translation extends Transformation
{
    public double x;
    public double y;
    public double z;

    public Translation(BaseWindow window, double x, double y, double z)
    {
        super(window);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void apply()
    {
        window.xOffset += x;
        window.yOffset += y;
        window.zOffset += z;
        transform(window, x, y, z);
    }

    public static void transform(BaseWindow window, double x, double y, double z)
    {
        transform(window,
                1, 0, 0, 0, 0, 1, 0, 0,  0, 0, 1, 0,  x * window.absoluteWidth, y * window.absoluteHeight, z * window.absoluteDepth, 1);
    }
}
