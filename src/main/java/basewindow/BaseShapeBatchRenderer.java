package basewindow;

public abstract class BaseShapeBatchRenderer
{
    public final boolean dynamic;

    public double posX = 0;
    public double posY = 0;
    public double posZ = 0;
    public double sX = 1;
    public double sY = 1;
    public double sZ = 1;
    public double yaw = 0;
    public double pitch = 0;
    public double roll = 0;

    public float offX;
    public float offY;
    public float offZ;

    public BaseShapeBatchRenderer(boolean dynamic)
    {
        this.dynamic = dynamic;
    }

    public abstract void fillRect(IBatchRenderableObject o, double x, double y, double sX, double sY);

    public abstract void fillBox(IBatchRenderableObject o, double x, double y, double z, double sX, double sY, double sZ, byte options);

    public void setPosition(double x, double y, double z)
    {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
    }

    public void setScale(double x, double y, double z)
    {
        this.sX = x;
        this.sY = y;
        this.sZ = z;
    }

    public void setRotation(double yaw, double pitch, double roll)
    {
        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;
    }

    public abstract void begin(boolean depth);

    public abstract void begin(boolean depth, boolean glow);

    public abstract void begin(boolean depth, boolean glow, boolean depthMask);

    public abstract void stage();

    public abstract void end();

    public abstract void forceRedraw();

    public abstract void draw();

    public abstract void setColor(double r, double g, double b, double a, double glow);

    public abstract void free();
}
