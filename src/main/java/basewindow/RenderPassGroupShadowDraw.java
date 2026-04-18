package basewindow;

import lwjglwindow.FrameBuffer;
import lwjglwindow.RenderPassDraw;

public class RenderPassGroupShadowDraw
{
    public BaseWindow window;
    public RenderPassShadowMap shadowPass;
    public RenderPassDraw drawPass;
    public ShaderGroupShadowDraw shaderGroup;

    public boolean shadowsEnabled = true;
    public double shadowQuality = 1.25;
    protected int size = 2048;

    public BaseFrameBuffer depthFrameBuffer;

    public RenderPassGroupShadowDraw(BaseWindow w)
    {
        this.window = w;
        this.shadowPass = new RenderPassShadowMap(this);
        this.drawPass = new RenderPassDraw(this);
        this.createFrameBuffer();
    }

    public int getShadowMapSize()
    {
        return size;
    }

    public void createFrameBuffer()
    {
        if (this.depthFrameBuffer != null)
        {
            this.size = (int) (this.shadowQuality * Math.max(this.window.absoluteHeight, this.window.absoluteWidth));
            this.depthFrameBuffer.free();
        }

        this.depthFrameBuffer = new FrameBuffer();
        this.depthFrameBuffer.createDepthTexture(this.size, this.size);
        this.depthFrameBuffer.initialize();
    }


    public void loadFrameBuffer()
    {
        int s = (int) (Math.max(this.window.absoluteHeight, this.window.absoluteWidth) * this.shadowQuality);

        if (s > 0 && s != this.size)
            this.createFrameBuffer();

        this.window.loadPerspective();
        this.depthFrameBuffer.bind();
        this.window.setViewport(0, 0, size, size);

        this.window.clearDepth();
    }

    public void setLighting(double light, double glowLight, double shadow, double glowShadow)
    {
        if (this.window.currentRenderPass == drawPass)
        {
            ShaderBase sb = ((ShaderBase) (this.window.currentShaderStage.shader));
            sb.light.set((float) light);
            sb.glowLight.set((float) glowLight);
            sb.shade.set((float) shadow);
            sb.glowShade.set((float) glowShadow);
        }
    }

    public void draw()
    {
        if (shadowsEnabled)
            this.shadowPass.draw();

        this.drawPass.draw();
    }
}
