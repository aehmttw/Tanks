package lwjglwindow;

import basewindow.BaseShapeBatchRenderer;
import basewindow.IBatchRenderableObject;

public class DummyShapeBatchRenderer extends BaseShapeBatchRenderer
{
    public DummyShapeBatchRenderer(boolean dynamic)
    {
        super(dynamic);
    }

    @Override
    public void fillRect(IBatchRenderableObject o, double x, double y, double sX, double sY)
    {

    }

    @Override
    public void fillBox(IBatchRenderableObject o, double x, double y, double z, double sX, double sY, double sZ, byte options)
    {

    }

    @Override
    public void begin(boolean depth)
    {

    }

    @Override
    public void begin(boolean depth, boolean glow)
    {

    }

    @Override
    public void begin(boolean depth, boolean glow, boolean depthMask)
    {

    }

    @Override
    public void stage()
    {

    }

    @Override
    public void end()
    {

    }

    @Override
    public void forceRedraw()
    {

    }

    @Override
    public void draw()
    {

    }

    @Override
    public void setColor(double r, double g, double b, double a, double glow)
    {

    }

    @Override
    public void free()
    {

    }
}
