package basewindow;

public abstract class BaseShapeBatchRenderer
{
    public boolean hidden = false;

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

    public abstract void delete(IBatchRenderableObject o);

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

    public abstract void beginAdd(IBatchRenderableObject o);

    public abstract void addPoint(float x, float y, float z);

    public abstract void setColor(float r, float g, float b, float a);

    public abstract void setGlow(float g);

    public void setColor(float r, float g, float b, float a, float glow)
    {
        this.setColor(r, g, b, a);
        this.setGlow(glow);
    }

    public abstract void addAttribute(ShaderGroup.Attribute attribute);

    public abstract void setAttribute(ShaderGroup.Attribute a, float... floats);

    public abstract void settings(boolean depth);

    public abstract void settings(boolean depth, boolean glow);

    public abstract void settings(boolean depth, boolean glow, boolean depthMask);

    public abstract void stage();

    public abstract void draw();

    public abstract void free();

    public double rotateX(double px, double py, double posX, double rotation)
    {
        return (py * Math.cos(rotation) - px * Math.sin(rotation)) + posX;
    }

    public double rotateY(double px, double py, double posY, double rotation)
    {
        return (px * Math.cos(rotation) + py * Math.sin(rotation)) + posY;
    }

}
