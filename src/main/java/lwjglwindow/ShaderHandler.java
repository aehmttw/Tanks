package lwjglwindow;

import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.EXTFramebufferObject.*;
import static org.lwjgl.opengl.GL20.*;

public class ShaderHandler
{
    public int size = 2048;
    public double quality = 1.25;

    public LWJGLWindow window;

    public int shadowProgram;
    public int shadowProgramVPUniform;
    public int normalProgram;
    public int normalProgramBiasUniform;
    public int normalProgramVPUniform;
    public int normalProgramLVPUniform;
    public int normalProgramLightPosition;
    public int normalProgramLightLookAt;
    public int fbo;
    public int depthTexture;
    public int samplerLocation;

    public int resolutionFlag;
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
        try
        {
            this.window = window;

            this.createDepthTexture(size);
            this.createFbo();
            this.createShadowProgram();
            this.createNormalProgram();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void createDepthTexture(int size)
    {
        depthTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, depthTexture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
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

    public void createShadowProgram() throws Exception
    {
        shadowProgram = glCreateProgram();

        int vshader = window.createShader("/shaders/shadow_map.vert", GL_VERTEX_SHADER);
        int fshader = window.createShader("/shaders/shadow_map.frag", GL_FRAGMENT_SHADER);

        glAttachShader(shadowProgram, vshader);
        glAttachShader(shadowProgram, fshader);
        GL20.glBindAttribLocation(shadowProgram, 6, "bones");
        glLinkProgram(shadowProgram);

        int linked = glGetProgrami(shadowProgram, GL_LINK_STATUS);
        String programLog = glGetProgramInfoLog(shadowProgram);

        if (programLog.trim().length() > 0)
            System.err.println(programLog);

        if (linked == 0)
            throw new AssertionError("Could not link program");

        this.initShadowProgram();
    }

    public void initShadowProgram()
    {
        glUseProgram(shadowProgram);
        shadowProgramVPUniform = glGetUniformLocation(shadowProgram, "viewProjectionMatrix");

        this.window.shadowMapBonesEnabledFlag = GL20.glGetUniformLocation(shadowProgram, "bonesEnabled");
        this.window.shadowMapBoneMatricesFlag = GL20.glGetUniformLocation(shadowProgram, "boneMatrices");

        glUseProgram(0);
    }

    public void createNormalProgram() throws Exception
    {
        normalProgram = glCreateProgram();

        int vshader = window.createShader("/shaders/main.vert", GL_VERTEX_SHADER);
        int fshader = window.createShader("/shaders/main.frag", GL_FRAGMENT_SHADER);

        glAttachShader(normalProgram, vshader);
        glAttachShader(normalProgram, fshader);
        GL20.glBindAttribLocation(normalProgram, 6, "bones");
        glLinkProgram(normalProgram);

        int linked = glGetProgrami(normalProgram, GL_LINK_STATUS);
        String programLog = glGetProgramInfoLog(normalProgram);

        if (programLog.trim().length() > 0)
            System.err.println(programLog);

        if (linked == 0)
            throw new AssertionError("Could not link program");

        this.initNormalProgram();
    }

    public void initNormalProgram()
    {
        glUseProgram(normalProgram);
        samplerLocation = glGetUniformLocation(normalProgram, "depthTexture");
        normalProgramBiasUniform = glGetUniformLocation(normalProgram, "biasMatrix");
        normalProgramVPUniform = glGetUniformLocation(normalProgram, "viewProjectionMatrix");
        normalProgramLVPUniform = glGetUniformLocation(normalProgram, "lightViewProjectionMatrix");
        normalProgramLightPosition = glGetUniformLocation(normalProgram, "lightPosition");
        normalProgramLightLookAt = glGetUniformLocation(normalProgram, "lightLookAt");
        glUniform1i(samplerLocation, 1);
        this.window.textureFlag = GL20.glGetUniformLocation(normalProgram, "texture");
        this.window.depthFlag = GL20.glGetUniformLocation(normalProgram, "depthtest");
        this.window.glowFlag = GL20.glGetUniformLocation(normalProgram, "glow");
        this.resolutionFlag = GL20.glGetUniformLocation(normalProgram, "shadowres");

        this.window.lightFlag = GL20.glGetUniformLocation(normalProgram, "light");
        this.window.glowLightFlag = GL20.glGetUniformLocation(normalProgram, "glowLight");
        this.window.shadeFlag = GL20.glGetUniformLocation(normalProgram, "shade");
        this.window.glowShadeFlag = GL20.glGetUniformLocation(normalProgram, "glowShade");

        this.window.shadowFlag = GL20.glGetUniformLocation(normalProgram, "shadow");
        this.window.vboFlag = GL20.glGetUniformLocation(normalProgram, "vbo");
        this.window.vboColorFlag = GL20.glGetUniformLocation(normalProgram, "originalColor");

        this.window.bonesEnabledFlag = GL20.glGetUniformLocation(normalProgram, "bonesEnabled");
        this.window.boneMatricesFlag = GL20.glGetUniformLocation(normalProgram, "boneMatrices");

        glUseProgram(0);
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

        glUseProgram(shadowProgram);

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

        glUseProgram(normalProgram);
        glUniform1i(this.resolutionFlag, this.size);

        if (this.window.shadowsEnabled)
            glUniform1i(this.window.shadowFlag, 1);
        else
            glUniform1i(this.window.shadowFlag, 0);

        if (!this.initialized)
        {
            this.initialized = true;
            this.window.setLighting(1.0, 1.0, 0.75, 1.0);
        }

        this.window.drawingShadow = false;

        this.window.loadPerspective();

        float[] projMatrix = new float[16];
        glGetFloatv(GL_PROJECTION_MATRIX, projMatrix);

        glUniformMatrix4fv(normalProgramVPUniform, false, projMatrix);
        glUniformMatrix4fv(normalProgramLVPUniform, false, projMatrixShadow);
        glUniformMatrix4fv(normalProgramBiasUniform, false, biasMatrix);
        glUniform3f(normalProgramLightPosition, 0, 0, (float) (this.window.absoluteDepth));
        glUniform3f(normalProgramLightLookAt, (float) this.window.absoluteWidth, (float) this.window.absoluteHeight, 0);

        glViewport(0, 0, (int) this.window.w[0], (int) this.window.h[0]);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glActiveTexture(GL13.GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, this.depthTexture);

        this.window.drawer.draw();

        glActiveTexture(GL13.GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, 0);

        glUseProgram(0);
    }
}
