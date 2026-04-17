package lwjglwindow;

import basewindow.BaseWindow;
import basewindow.RenderPass;
import org.lwjgl.opengl.GL13;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.glUseProgram;

public class RenderPassDraw extends RenderPass
{
    // todo - generalize
    public LWJGLWindow window;

    public boolean initialized = false;


    float[] biasMatrix = new float[]
    {
        0.5f, 0.0f, 0.0f, 0.0f,
        0.0f, 0.5f, 0.0f, 0.0f,
        0.0f, 0.0f, 0.5f, 0.0f,
        0.5f, 0.5f, 0.5f, 1.0f
    };

    public RenderPassDraw(LWJGLWindow w)
    {
        super(w, "draw");
        this.window = w;
    }

    @Override
    public void draw()
    {
        float[] projMatrixShadow = new float[16];
        glGetFloatv(GL_PROJECTION_MATRIX, projMatrixShadow);

        this.window.setShader(this.window.shaderDefault);
        this.window.shaderDefault.shaderBase.shadowres.set(this.window.defaultShadowPass.size);
        this.window.shaderDefault.shaderBase.shadow.set(this.window.shadowsEnabled);
        this.window.shaderDefault.shaderBase.width.set((float) this.window.absoluteWidth);
        this.window.shaderDefault.shaderBase.height.set((float) this.window.absoluteHeight);
        this.window.shaderDefault.shaderBase.depth.set((float) this.window.absoluteDepth);

        if (!this.initialized)
        {
            this.initialized = true;
            this.window.setLighting(1.0, 1.0, 0.5, 1.0);
        }

        this.window.drawingShadow = false;

        this.window.loadPerspective();

        float[] projMatrix = new float[16];
        glGetFloatv(GL_PROJECTION_MATRIX, projMatrix);

        this.window.shaderDefault.shaderBase.lightViewProjectionMatrix.set(projMatrixShadow, false);
        this.window.shaderDefault.shaderBase.biasMatrix.set(biasMatrix, false);

        glViewport(0, 0, this.window.frameBufferWidth, this.window.frameBufferHeight);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        this.window.defaultShadowPass.depthFrameBuffer.depthTexture.bind(1);

        this.window.drawer.draw();

        glActiveTexture(GL13.GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, 0);

        // Debug code: draw the depth texture
        /* this.window.textures.put("depth", this.window.defaultShadowPass.depthFrameBuffer.depthTexture.texture);
        this.window.setColor(255, 255, 255);
        this.window.shapeRenderer.drawImage(100, 200, 500, 500, "depth", false);
         */

        glUseProgram(0);
    }
}
