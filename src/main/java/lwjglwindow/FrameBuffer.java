package lwjglwindow;

import basewindow.BaseFrameBuffer;
import org.lwjgl.opengl.GL13;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.EXTFramebufferObject.*;
import static org.lwjgl.opengl.GL20.*;

public class FrameBuffer extends BaseFrameBuffer
{
    protected int framebuffer;
    public ArrayList<FrameBufferTexture> colorTextures = new ArrayList<>();
    public FrameBufferTexture depthTexture = null;

    public boolean initialized = false;

    public FrameBuffer()
    {
        this.framebuffer = glGenFramebuffersEXT();
    }

    public void createDepthTexture(int sizeX, int sizeY)
    {
        if (this.initialized)
            throw new AssertionError("Can't add textures to already initialized frame buffer!");

        this.depthTexture = new FrameBufferTexture(sizeX, sizeY, FrameBufferTexture.Type.DEPTH);
    }

    public void addColorTexture(int sizeX, int sizeY, boolean alpha)
    {
        if (this.initialized)
            throw new AssertionError("Can't add textures to already initialized frame buffer!");

        this.colorTextures.add(new FrameBufferTexture(sizeX, sizeY, alpha ? FrameBufferTexture.Type.RGBA : FrameBufferTexture.Type.RGB));

        if (this.colorTextures.size() > GL_MAX_COLOR_ATTACHMENTS_EXT)
            throw new AssertionError("The maximum number of color textures for a framebuffer (" + GL_MAX_COLOR_ATTACHMENTS_EXT
            + ") has been exceeded!");
    }

    public void initialize()
    {
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, framebuffer);
        glReadBuffer(GL_NONE);
        glDrawBuffer(GL_NONE);

        if (depthTexture != null)
            glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_DEPTH_ATTACHMENT_EXT, GL_TEXTURE_2D, depthTexture.texture, 0);

        for (int i = 0; i < colorTextures.size(); i++)
        {
            glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT + i, GL_TEXTURE_2D, colorTextures.get(i).texture, 0);
        }

        int fboStatus = glCheckFramebufferStatusEXT(GL_FRAMEBUFFER_EXT);

        if (fboStatus != GL_FRAMEBUFFER_COMPLETE_EXT)
            throw new AssertionError("Could not create FBO: " + fboStatus);

        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
    }

    public void free()
    {
        if (depthTexture != null)
            glDeleteTextures(depthTexture.texture);

        for (FrameBufferTexture t: colorTextures)
            glDeleteTextures(t.texture);

        glDeleteFramebuffersEXT(framebuffer);
    }

    public void bind()
    {
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, framebuffer);
    }

    @Override
    public void bindDepthTexture(int target)
    {
        this.depthTexture.bind(target);
    }

    @Override
    public void bindColorTexture(int which, int target)
    {
        this.colorTextures.get(which).bind(target);
    }

    public static class FrameBufferTexture
    {
        public enum Type { RGB, RGBA, DEPTH }
        protected int texture;

        public FrameBufferTexture(int sizeX, int sizeY, Type t)
        {
            this.texture = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, this.texture);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);

            int format = 0;
            int intFormat = 0;

            switch (t)
            {
                case RGB:
                    format = GL_RGB;
                    intFormat = GL_RGB8;
                    break;
                case RGBA:
                    format = GL_RGBA;
                    intFormat = GL_RGBA8;
                    break;
                case DEPTH:
                    format = GL_DEPTH_COMPONENT;
                    intFormat = GL_DEPTH_COMPONENT24;
                    break;
                default:
                    throw new RuntimeException("Invalid render buffer type!");
            }

            glTexImage2D(GL_TEXTURE_2D, 0,
                    intFormat,
                    sizeX, sizeY, 0,
                    format,
                    GL_FLOAT,
                    (ByteBuffer) null);
            glBindTexture(GL_TEXTURE_2D, 0);
        }

        public void bind(int number)
        {
            glActiveTexture(GL13.GL_TEXTURE0 + number);
            glBindTexture(GL_TEXTURE_2D, this.texture);
            glActiveTexture(GL13.GL_TEXTURE0);
        }
    }
}
