package lwjglwindow;

import org.lwjgl.opengl.GL13;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.EXTFramebufferObject.*;
import static org.lwjgl.opengl.GL20.*;

public class ShaderHandler
{
    public int size = 2048;
    public double quality = 1.25;

    public LWJGLWindow window;

    public int fbo;

    public boolean initialized;

    float[] biasMatrix = new float[]
            {
                    0.5f, 0.0f, 0.0f, 0.0f,
                    0.0f, 0.5f, 0.0f, 0.0f,
                    0.0f, 0.0f, 0.5f, 0.0f,
                    0.5f, 0.5f, 0.5f, 1.0f
            };

    public ShaderHandler(LWJGLWindow window)
    {
        this.window = window;

    }

    public void renderShadowMap()
    {
        this.window.currentRenderPass = this.window.defaultShadowPass;
        this.window.currentRenderPass.draw();
    }

    public void renderNormal()
    {
        this.window.currentRenderPass = this.window.defaultDrawPass;
        this.window.currentRenderPass.draw();
    }
}
