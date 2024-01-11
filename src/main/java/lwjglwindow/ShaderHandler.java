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
    public int depthTexture;

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

        this.createDepthTexture(size);
        this.createFbo();
    }

    public void createDepthTexture(int size)
    {
        depthTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, depthTexture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, size, size, 0, GL_DEPTH_COMPONENT, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void createFbo()
    {
        fbo = glGenFramebuffersEXT();
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, fbo);
        glBindTexture(GL_TEXTURE_2D, depthTexture);
        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);
        glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_DEPTH_ATTACHMENT_EXT, GL_TEXTURE_2D, depthTexture, 0);
        int fboStatus = glCheckFramebufferStatusEXT(GL_FRAMEBUFFER_EXT);

        if (fboStatus != GL_FRAMEBUFFER_COMPLETE_EXT)
            throw new AssertionError("Could not create FBO: " + fboStatus);

        glBindTexture(GL_TEXTURE_2D, 0);
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
    }

    public void renderShadowMap()
    {
        this.window.drawingShadow = true;

        int s = (int) (Math.max(this.window.absoluteHeight, this.window.absoluteWidth) * this.quality);
        if (s > 0 && s != this.size)
        {
            this.size = (int) (this.quality * Math.max(this.window.absoluteHeight, this.window.absoluteWidth));
            this.createDepthTexture(size);
            this.createFbo();
        }

        this.window.setShader(this.window.shaderDefault.shaderShadowMap);

        this.window.loadPerspective();

        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, fbo);
        glViewport(0, 0, size, size);

        glClear(GL_DEPTH_BUFFER_BIT);

        this.window.drawer.draw();

        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
        glUseProgram(0);
    }

    public void renderNormal()
    {
        float[] projMatrixShadow = new float[16];
        glGetFloatv(GL_PROJECTION_MATRIX, projMatrixShadow);

        this.window.setShader(this.window.shaderDefault.shaderBase);
        this.window.shaderDefault.shaderBase.shadowres.set(this.size);
        //this.window.shaderDefault.shaderBase.lightVec.set((float) this.window.lightVec[0], (float) this.window.lightVec[1], (float) this.window.lightVec[2]);
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

        glViewport(0, 0, this.window.w[0], this.window.h[0]);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glActiveTexture(GL13.GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, this.depthTexture);

        this.window.drawer.draw();

        glActiveTexture(GL13.GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, 0);

//        glBindTexture(GL_TEXTURE_2D, depthTexture);
//        this.window.textures.put("depth", depthTexture);
//        this.window.setColor(255, 255, 255);
//        this.window.shapeRenderer.drawImage(100, 200, 500, 500, "depth", false);

        glUseProgram(0);
    }
}
