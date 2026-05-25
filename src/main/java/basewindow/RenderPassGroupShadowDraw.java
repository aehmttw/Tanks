package basewindow;

import lwjglwindow.FrameBuffer;

public class RenderPassGroupShadowDraw
{
    public BaseWindow window;
    public RenderPassShadowMap shadowPass;
    public RenderPassDraw drawPass;
    public ShaderGroupShadowDraw shaderGroup;

    /**
     * If set, will draw to framebuffer instead of drawing to the screen.
     * Textures from the framebuffer will be bound to "image" and "depth" names.
     */
    public boolean drawToFramebuffer = false;

    /** Set if we are sampling depth maps only, for example, to see shadows cast by a light source */
    public boolean drawingShadow = false;

    /**
     * How many render passes have run before this one this frame for the main drawing.
     * Useful if you need to check if this is the first time something is being drawn this frame.
     */
    public int currentPassNumber = -1;

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

    public void draw()
    {
        this.currentPassNumber = 0;
        if (shadowsEnabled)
        {
            this.drawingShadow = true;
            this.shadowPass.draw();
            this.currentPassNumber++;
        }

        this.drawingShadow = false;
        this.drawPass.draw();

        this.currentPassNumber = -1;
    }
}
