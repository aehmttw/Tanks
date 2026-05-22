package basewindow;

import org.lwjgl.opengl.GL11;

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

        // Light/shadow level
        this.drawFrameBuffer.addColorTexture(pg.window, 1, false);

        this.drawFrameBuffer.createDepthTexture(pg.window);
    }

    @Override
    public void draw()
    {
        super.draw();

        if (this.passGroup.drawToFramebuffer)
        {
            this.drawFrameBuffer.resizeToWindow(this.window);
            this.drawFrameBuffer.bind();
        }

        this.window.getProjectionMatrix(projMatrixShadow);

        this.window.setShader(this.window.shaderDefault);
        this.window.shaderDefault.shaderBase.shadowres.set(this.passGroup.getShadowMapSize());
        this.window.shaderDefault.shaderBase.shadow.set(this.passGroup.shadowsEnabled);
        this.window.shaderDefault.shaderBase.width.set((float) this.window.absoluteWidth);
        this.window.shaderDefault.shaderBase.height.set((float) this.window.absoluteHeight);
        this.window.shaderDefault.shaderBase.depth.set((float) this.window.absoluteDepth);

        if (!this.initialized)
        {
            this.initialized = true;
            this.window.mainRenderPasses.setLighting(1.0, 1.0, 0.5, 1.0);
        }

        this.window.loadPerspective();

        this.window.shaderDefault.shaderBase.lightViewProjectionMatrix.set(projMatrixShadow, false);
        this.window.shaderDefault.shaderBase.biasMatrix.set(biasMatrix, false);

        this.window.setViewport(0, 0, this.window.frameBufferWidth, this.window.frameBufferHeight);

        this.window.clearDepth();
        this.window.clearColor();

        this.passGroup.depthFrameBuffer.bindDepthTexture(1);
        this.window.drawer.drawSinglePass(this);

        this.window.stopFrameBuffer();

        this.window.setShader(this.passGroup.shaderGroup);

        if (this.passGroup.drawToFramebuffer)
        {
            this.drawFrameBuffer.bindColorTexture(this.window, 0, "image");
            this.drawFrameBuffer.bindDepthTexture(this.window, "depth");

            this.window.setColor(255, 255, 255);
            this.window.shapeRenderer.drawImage(0, window.absoluteHeight, window.absoluteWidth, -window.absoluteHeight, "image", false);
        }

        // Debug code: draw the depth texture
        /* this.window.textures.put("depth", this.window.defaultShadowPass.depthFrameBuffer.depthTexture.texture);
        this.window.setColor(255, 255, 255);
        this.window.shapeRenderer.drawImage(100, 200, 500, 500, "depth", false);
         */
    }
}
