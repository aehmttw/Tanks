package basewindow.transformation;

import basewindow.BaseWindow;

public class Shear extends Transformation
{
    public double xy;
    public double xz;
    public double yx;
    public double yz;
    public double zx;
    public double zy;

    public Shear(BaseWindow window, double xy, double xz, double yx, double yz, double zx, double zy)
    {
        super(window);
        this.xy = xy;
        this.xz = xz;
        this.yx = yx;
        this.yz = yz;
        this.zx = zx;
        this.zy = zy;
    }

    public void apply()
    {
        transform(window, xy, xz, yx, yz, zx, zy);
    }

    @Override
    public void applyToWindow()
    {

    }

    public static void transform(BaseWindow window, double xy, double xz, double yx, double yz, double zx, double zy)
    {
        transform(window, 1, xy, xz, 0, yx, 1, yz, 0,  zx, zy, 1, 0,  0, 0, 0, 1);
    }
}
