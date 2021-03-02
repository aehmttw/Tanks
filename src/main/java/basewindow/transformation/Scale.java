package basewindow.transformation;

import basewindow.BaseWindow;

public class Scale extends Transformation
{
    public double x;
    public double y;
    public double z;

    public Scale(BaseWindow window, double x, double y, double z)
    {
        super(window);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void apply()
    {
        transform(window, x, y, z);
    }

    @Override
    public void applyToWindow()
    {

    }

    public static void transform(BaseWindow window, double x, double y, double z)
    {
        transform(window, x, 0, 0, 0, 0, y, 0, 0,  0, 0, z, 0,  0, 0, 0, 1);
    }
}
