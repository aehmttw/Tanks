package lwjglwindow;

import basewindow.BaseFrameBuffer;
import basewindow.BaseWindow;

import org.lwjgl.opengl.GL13;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.ARBTextureFloat.GL_RGB16F_ARB;
import static org.lwjgl.opengl.ARBTextureFloat.GL_RGBA16F_ARB;
import static org.lwjgl.opengl.ARBTextureRG.*;
import static org.lwjgl.opengl.EXTFramebufferObject.*;
import static org.lwjgl.opengl.GL20.*;

public class FrameBuffer extends BaseFrameBuffer
{
    protected int framebuffer;
    public ArrayList<FrameBufferTexture> colorTextures = new ArrayList<>();
    public FrameBufferTexture depthTexture = null;

    public boolean initialized = false;

    protected int lastWindowSizeX = -1;
    protected int lastWindowSizeY = -1;

    public FrameBuffer()
    {
        this.framebuffer = glGenFramebuffersEXT();
    }

    public void createDepthTexture(int sizeX, int sizeY)
    {
        if (this.initialized)
            throw new AssertionError("Can't add textures to already initialized frame buffer!");

        this.depthTexture = new FrameBufferTexture(sizeX, sizeY, 1, FrameBufferTexture.Type.DEPTH);
    }

    @Override
    public void createDepthTexture(BaseWindow w)
    {
        this.createDepthTexture(w.frameBufferWidth, w.frameBufferHeight);
        this.lastWindowSizeX = w.frameBufferWidth;
        this.lastWindowSizeY = w.frameBufferHeight;
    }

    public void addColorTexture(int sizeX, int sizeY, int channels, boolean fp)
    {
        if (this.initialized)
            throw new AssertionError("Can't add textures to already initialized frame buffer!");

        this.colorTextures.add(new FrameBufferTexture(sizeX, sizeY, channels, fp ? FrameBufferTexture.Type.FLOAT : FrameBufferTexture.Type.INT));

        if (this.colorTextures.size() > GL_MAX_COLOR_ATTACHMENTS_EXT)
            throw new AssertionError("The maximum number of color textures for a framebuffer (" + GL_MAX_COLOR_ATTACHMENTS_EXT +
            ") has been exceeded!");
    }

    @Override
    public void addColorTexture(BaseWindow w, int channels, boolean fp)
    {
        this.addColorTexture(w.frameBufferWidth, w.frameBufferHeight, channels, fp);
        this.lastWindowSizeX = w.frameBufferWidth;
        this.lastWindowSizeY = w.frameBufferHeight;
    }

    @Override
    public void resizeToWindow(BaseWindow w)
    {
        if (this.lastWindowSizeX != w.frameBufferWidth || this.lastWindowSizeY != w.frameBufferHeight)
        {
            this.free();
            this.framebuffer = glGenFramebuffersEXT();

            if (depthTexture != null)
                this.depthTexture = new FrameBufferTexture(w.frameBufferWidth, w.frameBufferHeight, 1, FrameBufferTexture.Type.DEPTH);

            for (int i = 0; i < colorTextures.size(); i++)
            {
                this.colorTextures.set(i, new FrameBufferTexture(w.frameBufferWidth, w.frameBufferHeight, this.colorTextures.get(i).channels, this.colorTextures.get(i).type));
            }
            this.initialize();

            this.lastWindowSizeX = w.frameBufferWidth;
            this.lastWindowSizeY = w.frameBufferHeight;
        }
    }

    public void initialize()
    {
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, framebuffer);
        glReadBuffer(GL_NONE);

        if (depthTexture != null)
            glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_DEPTH_ATTACHMENT_EXT, GL_TEXTURE_2D, depthTexture.texture, 0);


        int[] bufs = new int[colorTextures.size()];
        for (int i = 0; i < colorTextures.size(); i++)
        {
            glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT + i, GL_TEXTURE_2D, colorTextures.get(i).texture, 0);
            bufs[i] = GL_COLOR_ATTACHMENT0_EXT + i;
        }

        if (colorTextures.size() > 0)
            glDrawBuffers(bufs);
        else
            glDrawBuffer(GL_NONE);

        int fboStatus = glCheckFramebufferStatusEXT(GL_FRAMEBUFFER_EXT);

        if (fboStatus != GL_FRAMEBUFFER_COMPLETE_EXT)
            throw new AssertionError("Could not create FBO: " + fboStatus);

        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);

        this.initialized = true;
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
        if (!initialized)
            throw new RuntimeException("Uninitialized framebuffer bound!");

        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, framebuffer);
    }

    @Override
    public void bindDepthTexture(int target)
    {
        this.depthTexture.bind(target);
    }

    @Override
    public void bindDepthTexture(BaseWindow w, String name)
    {
        ((LWJGLWindow) w).textures.put(name, this.depthTexture.texture);
    }

    @Override
    public void bindColorTexture(int which, int target)
    {
        this.colorTextures.get(which).bind(target);
    }

    @Override
    public void bindColorTexture(BaseWindow w, int which, String name)
    {
        ((LWJGLWindow) w).textures.put(name, this.colorTextures.get(which).texture);
    }

    public static class FrameBufferTexture
    {
        public enum Type { INT, FLOAT, DEPTH }

        protected int texture;
        protected Type type;
        protected int channels;

        public FrameBufferTexture(int sizeX, int sizeY, int channels, Type t)
        {
            if (channels > 4 || channels <= 0)
                throw new RuntimeException("Texture channel count must be between 1 and 4! Got " + channels);

            this.type = t;
            this.channels = channels;
            this.texture = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, this.texture);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);

            int format = -1;
            int intFormat = -1;

            if (type == Type.DEPTH)
            {
                format = GL_DEPTH_COMPONENT;
                intFormat = GL_DEPTH_COMPONENT24;
            }
            else if (channels == 1)
            {
                format = GL_RED;

                if (t == Type.INT)
                    intFormat = GL_R8;
                else if (t == Type.FLOAT)
                    intFormat = GL_R16F;
            }
            else if (channels == 2)
            {
                format = GL_RG;

                if (t == Type.INT)
                    intFormat = GL_RG8;
                else if (t == Type.FLOAT)
                    intFormat = GL_RG16F;
            }
            else if (channels == 3)
            {
                format = GL_RGB;

                if (t == Type.INT)
                    intFormat = GL_RGB8;
                else if (t == Type.FLOAT)
                    intFormat = GL_RGB16F_ARB;
            }
            else
            {
                format = GL_RGBA;

                if (t == Type.INT)
                    intFormat = GL_RGBA8;
                else if (t == Type.FLOAT)
                    intFormat = GL_RGBA16F_ARB;
            }

            glGetError();
            glTexImage2D(GL_TEXTURE_2D, 0,
                    intFormat,
                    sizeX, sizeY, 0,
                    format,
                    t == Type.FLOAT ? GL_FLOAT : GL_UNSIGNED_BYTE,
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
