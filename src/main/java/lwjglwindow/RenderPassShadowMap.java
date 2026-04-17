package lwjglwindow;

import basewindow.BaseWindow;
import basewindow.RenderPass;

import static org.lwjgl.opengl.EXTFramebufferObject.GL_FRAMEBUFFER_EXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glBindFramebufferEXT;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glUseProgram;

public class RenderPassShadowMap extends RenderPass
{
    public int size = 2048;
    public double quality = 1.25;

    public FrameBuffer depthFrameBuffer;

    public RenderPassShadowMap(BaseWindow w)
    {
        super(w, "shadowmap");
        this.depthFrameBuffer = new FrameBuffer();
        this.depthFrameBuffer.createDepthTexture(this.size, this.size);
        this.depthFrameBuffer.initialize();
    }

    @Override
    public void draw()
    {
        this.window.drawingShadow = true;

        int s = (int) (Math.max(this.window.absoluteHeight, this.window.absoluteWidth) * this.quality);

        //todo - destroy the old one?
        if (s > 0 && s != this.size)
        {
            this.size = (int) (this.quality * Math.max(this.window.absoluteHeight, this.window.absoluteWidth));
            this.depthFrameBuffer = new FrameBuffer();
            this.depthFrameBuffer.createDepthTexture(this.size, this.size);
            this.depthFrameBuffer.initialize();
        }

        this.window.setShader(this.window.shaderDefault);

        this.window.loadPerspective();

        depthFrameBuffer.bind();
        glViewport(0, 0, size, size);

        glClear(GL_DEPTH_BUFFER_BIT);

        this.window.drawer.draw();

        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
        glUseProgram(0);
    }
}
