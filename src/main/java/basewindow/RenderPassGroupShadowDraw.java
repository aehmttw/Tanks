package basewindow;


public class RenderPassGroupShadowDraw
{
    public BaseWindow window;
    /** Camera used by this group's primary scene pass. */
    public Camera sceneCamera;
    /** Orthographic light-space camera used to render this group's shadow map. */
    public Camera shadowCamera;
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
    /** Visible fraction of the shadow map; smaller values include more offscreen casters. */
    public double shadowCoverage = 0.8;
    protected int size = 2048;

    public BaseFrameBuffer depthFrameBuffer;

    public double light;
    public double shadow;
    protected float[] lightColor = new float[3];

    public RenderPassGroupShadowDraw(BaseWindow w)
    {
        this.window = w;
        this.sceneCamera = new Camera("scene", 0, w.absoluteWidth, 0, w.absoluteHeight, w.absoluteDepth);
        this.shadowCamera = new Camera("shadow", 0, w.absoluteWidth, 0, w.absoluteHeight, w.absoluteDepth);
        this.shadowCamera.worldAdjustment.values[0][2] = 0.5;
        this.shadowCamera.worldAdjustment.values[1][2] = 0.5;
        this.shadowCamera.updateView();
        this.window.currentCamera = this.sceneCamera;
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

        this.depthFrameBuffer = this.window.createFrameBuffer();
        this.depthFrameBuffer.createDepthTexture(this.size, this.size);
        this.depthFrameBuffer.bindDepthTexture(this.window, "depth");
        this.depthFrameBuffer.initialize();
    }


    public void loadFrameBuffer()
    {
        int s = (int) (Math.max(this.window.absoluteHeight, this.window.absoluteWidth) * this.shadowQuality);

        if (s > 0 && s != this.size)
            this.createFrameBuffer();

        Camera.Bounds bounds = this.sceneCamera.getBoundsAtDepth(0);
        double left = bounds.left;
        double right = bounds.right;
        double top = bounds.top;
        double bottom = bounds.bottom;

        if (this.shadowCoverage > 0)
        {
            double centerX = (left + right) / 2.0;
            double centerY = (top + bottom) / 2.0;
            double halfWidth = (right - left) / (2.0 * this.shadowCoverage);
            double halfHeight = (bottom - top) / (2.0 * this.shadowCoverage);

            left = centerX - halfWidth;
            right = centerX + halfWidth;
            top = centerY - halfHeight;
            bottom = centerY + halfHeight;
        }

        // Keep the orthographic projection on a texel grid so sub-texel camera
        // movement does not cause the shadow map to continuously swim.
        double width = right - left;
        double height = bottom - top;
        double texelWidth = width / Math.max(1, this.size);
        double texelHeight = height / Math.max(1, this.size);
        double centerX = Math.round((left + right) / (2.0 * texelWidth)) * texelWidth;
        double centerY = Math.round((top + bottom) / (2.0 * texelHeight)) * texelHeight;

        left = centerX - width / 2.0;
        right = centerX + width / 2.0;
        top = centerY - height / 2.0;
        bottom = centerY + height / 2.0;

        this.shadowCamera.setProjectionType(Camera.Projection.orthographic);
        this.shadowCamera.resetToBounds(left, right, top, bottom, this.window.absoluteDepth);
        // The shadow camera is positioned at absoluteDepth. This covers the same
        // world-space z range as the legacy shadow glOrtho(-absoluteDepth, absoluteDepth).
        this.shadowCamera.setClipPlanes(this.window.absoluteDepth * 0.1, this.window.absoluteDepth * 2);
        this.shadowCamera.posZ = this.window.absoluteDepth;
        this.shadowCamera.updateView();
        this.window.currentCamera = this.shadowCamera;

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

    public void setLightColor(Color c)
    {
        this.lightColor[0] = (float) c.red / 255;
        this.lightColor[1] = (float) c.green / 255;
        this.lightColor[2] = (float) c.blue / 255;
    }
}
