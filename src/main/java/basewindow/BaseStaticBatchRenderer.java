package basewindow;

public abstract class BaseStaticBatchRenderer
{
    public String texture;

    public abstract void addVertex(float x, float y, float z);

    public void addColor(Color c)
    {
        this.addColor((float) c.red, (float) c.green, (float) c.blue, (float) c.alpha);
    }

    public abstract void addColor(float r, float g, float b, float a);

    public abstract void addTexCoord(float u, float v);

    public abstract void addNormal(float x, float y, float z);

    public abstract void addNormal(float[] n);

    public abstract void addAttributeF(ShaderProgram.Attribute a, float... floats);

    public abstract void addAttributeI(ShaderProgram.Attribute a, int... ints);

    public abstract void stage();

    public abstract void draw(double posX, double posY, double posZ, double sX, double sY, double sZ, double yaw, double pitch, double roll, boolean depth, boolean depthWrite);

    public abstract void free();
}
