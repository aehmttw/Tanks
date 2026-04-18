package lwjglwindow;

import basewindow.RenderPass;
import basewindow.RenderPassGroupShadowDraw;

public class RenderPassDraw extends RenderPass
{
    public RenderPassGroupShadowDraw passGroup;

    public boolean initialized = false;
    protected float[] projMatrixShadow = new float[16];

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
    }

    @Override
    public void draw()
    {
        super.draw();

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

        this.window.drawingShadow = false;

        this.window.loadPerspective();

        this.window.shaderDefault.shaderBase.lightViewProjectionMatrix.set(projMatrixShadow, false);
        this.window.shaderDefault.shaderBase.biasMatrix.set(biasMatrix, false);

        this.window.setViewport(0, 0, this.window.frameBufferWidth, this.window.frameBufferHeight);

        this.window.clearDepth();
        this.window.clearColor();

        this.passGroup.depthFrameBuffer.bindDepthTexture(1);
        this.window.drawer.draw();

        // Debug code: draw the depth texture
        /* this.window.textures.put("depth", this.window.defaultShadowPass.depthFrameBuffer.depthTexture.texture);
        this.window.setColor(255, 255, 255);
        this.window.shapeRenderer.drawImage(100, 200, 500, 500, "depth", false);
         */
    }
}
