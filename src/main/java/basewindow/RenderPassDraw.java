package basewindow;

public class RenderPassDraw extends RenderPass
{
    public RenderPassGroupShadowDraw passGroup;

    public boolean initialized = false;
    protected float[] projMatrixShadow = new float[16];

    public BaseFrameBuffer drawFrameBuffer;

    float[] biasMatrix = new float[]
    {
        0.5f, 0.0f, 0.0f, 0.0f,
        0.0f, 0.5f, 0.0f, 0.0f,
        0.0f, 0.0f, 0.5f, 0.0f,
        0.5f, 0.5f, 0.5f, 1.0f
    };

    public RenderPassDraw(RenderPassGroupShadowDraw pg)
    {
        super(pg.window, "draw");
        this.passGroup = pg;
        this.drawFrameBuffer = pg.window.createFrameBuffer();

        // Main drawing
        this.drawFrameBuffer.addColorTexture(pg.window, 3, false);

        // Glow
        this.drawFrameBuffer.addColorTexture(pg.window, 3, false);

        // Light/shadow
        this.drawFrameBuffer.addColorTexture(pg.window, 3, false);

        this.drawFrameBuffer.createDepthTexture(pg.window);
    }

    @Override
    public void draw()
    {
        super.draw();

        int viewportY = 0;
        if (this.window.absoluteHeight > 0)
            viewportY = (int) Math.round(this.window.interfaceCameraHeightOffset * this.window.frameBufferHeight / this.window.absoluteHeight);

        int viewportHeight = Math.max(1, this.window.frameBufferHeight - viewportY);

        if (this.passGroup.drawToFramebuffer)
        {
            this.drawFrameBuffer.resizeToWindow(this.window);
            this.drawFrameBuffer.bind();
        }

        this.window.getProjectionMatrix(projMatrixShadow);

        this.window.setShader(this.window.shaderDefault);
        this.window.shaderDefault.shaderBase.drawToFramebuffer.set(this.passGroup.drawToFramebuffer);
        this.window.shaderDefault.shaderBase.shadowres.set(this.passGroup.getShadowMapSize());
        this.window.shaderDefault.shaderBase.shadow.set(this.passGroup.shadowsEnabled);

        if (!this.initialized)
            this.initialized = true;

        this.window.currentCamera = this.window.baseCamera;
        this.window.loadPerspective();

        this.window.shaderDefault.shaderBase.lightViewProjectionMatrix.set(projMatrixShadow, false);
        this.window.shaderDefault.shaderBase.biasMatrix.set(biasMatrix, false);

        this.window.shaderDefault.shaderBase.shadowLight.set((float) this.passGroup.shadow);
        this.window.shaderDefault.shaderBase.baseLight.set((float) this.passGroup.light);
        this.window.shaderDefault.shaderBase.lightColor.set(this.passGroup.lightColor);

        if (this.passGroup.drawToFramebuffer)
            this.window.setViewport(0, 0, this.window.frameBufferWidth, this.window.frameBufferHeight);
        else
            this.window.setViewport(0, viewportY, this.window.frameBufferWidth, viewportHeight);

        this.window.clearDepth();
        this.window.clearColor();

        this.passGroup.depthFrameBuffer.bindDepthTexture(1);
        this.passGroup.depthFrameBuffer.bindDepthTexture(this.window, "shadow_depth");
        this.window.drawer.drawSinglePass(this);

        this.window.stopFrameBuffer();

        this.window.setShader(this.passGroup.shaderGroup);

        if (this.passGroup.drawToFramebuffer)
        {
            this.drawFrameBuffer.bindColorTexture(this.window, 0, "image");
            this.drawFrameBuffer.bindDepthTexture(this.window, "depth");

            this.window.currentCamera = this.window.screenCamera;
            this.window.transformations.clear();
            this.window.loadPerspective();

            this.window.setViewport(0, viewportY, this.window.frameBufferWidth, viewportHeight);
            this.window.setColor(255, 255, 255);
            this.window.shapeRenderer.drawImage(0, window.absoluteHeight, window.absoluteWidth, -window.absoluteHeight, "image", false);
        }
    }
}
