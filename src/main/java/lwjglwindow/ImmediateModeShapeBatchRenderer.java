package lwjglwindow;

import basewindow.BaseShapeBatchRenderer;
import basewindow.BaseWindow;
import basewindow.IBatchRenderableObject;

public class ImmediateModeShapeBatchRenderer extends BaseShapeBatchRenderer
{
    public BaseWindow window;

    public ImmediateModeShapeBatchRenderer(BaseWindow window)
    {
        this.window = window;
    }

    @Override
    public void fillRect(IBatchRenderableObject o, double x, double y, double sX, double sY)
    {
        window.shapeRenderer.fillRect(x, y, sX, sY);
    }

    @Override
    public void fillBox(IBatchRenderableObject o, double x, double y, double z, double sX, double sY, double sZ, byte options)
    {
        window.shapeRenderer.fillBox(x, y, z, sX, sY, sZ, options);
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
        window.setColor(r, g, b, a, glow);
    }
}
