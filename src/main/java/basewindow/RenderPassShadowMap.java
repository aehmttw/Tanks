package basewindow;

public class RenderPassShadowMap extends RenderPass
{
    public RenderPassGroupShadowDraw passGroup;

    public RenderPassShadowMap(RenderPassGroupShadowDraw pg)
    {
        super(pg.window, "shadowmap");
        this.passGroup = pg;
    }

    @Override
    public void draw()
    {
        super.draw();

        this.window.drawingShadow = true;

        this.window.setShader(this.window.shaderDefault);
        this.passGroup.loadFrameBuffer();
        this.window.drawer.draw();

        this.window.stopFrameBuffer();
    }
}
