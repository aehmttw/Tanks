package lwjglwindow;

import basewindow.BaseStaticBatchRenderer;
import basewindow.ShaderGroup;
import basewindow.transformation.Rotation;
import basewindow.transformation.Scale;
import basewindow.transformation.Translation;
import org.lwjgl.BufferUtils;
import tanks.rendering.StaticTerrainRenderer;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

import static basewindow.BaseShaderUtil.FLOAT;
import static basewindow.BaseShaderUtil.INT;
import static org.lwjgl.opengl.GL11.*;

public class VBOStaticBatchRenderer extends BaseStaticBatchRenderer
{
    public LWJGLWindow window;

    public ShaderGroup shader;
    public int vertVBO = -1;
    public int colVBO = -1;
    public int texVBO = -1;
    public int normVBO = -1;
    public HashMap<ShaderGroup.Attribute, Integer> attributeVBOs = new HashMap<>();

    protected int vertexCount;

    protected FloatBuffer vertices;
    protected FloatBuffer colors;
    protected FloatBuffer texCoords;
    protected FloatBuffer normals;
    protected HashMap<ShaderGroup.Attribute, FloatBuffer> attributeBuffers = new HashMap<>();

    public boolean staged = false;

    protected float colorGlow;

    protected float currentR;
    protected float currentG;
    protected float currentB;
    protected float currentA;
    protected float currentTexU;
    protected float currentTexV;
    protected float currentNormalX;
    protected float currentNormalY;
    protected float currentNormalZ;

    public boolean depth = false;
    public boolean glow = false;
    public boolean depthMask = false;

    protected HashMap<ShaderGroup.Attribute, float[]> floatAttributes = new HashMap<>();

    public VBOStaticBatchRenderer(LWJGLWindow window, ShaderGroup shader, boolean color, String texture, boolean normal, int vertices)
    {
        this.window = window;
        this.vertexCount = vertices;

        this.vertVBO = window.createVBO();
        this.vertices = BufferUtils.createFloatBuffer(vertices * 3);

        this.shader = shader;

        for (ShaderGroup.Attribute a : shader.attributes)
        {
            attributeVBOs.put(a, window.createVBO());
            attributeBuffers.put(a, BufferUtils.createFloatBuffer(vertices * a.count));
            this.floatAttributes.put(a, new float[a.count]);
        }

        if (color)
        {
            this.colVBO = window.createVBO();
            this.colors = BufferUtils.createFloatBuffer(vertices * 4);
        }

        if (texture != null)
        {
            this.texture = texture;
            this.texVBO = window.createVBO();
            this.texCoords = BufferUtils.createFloatBuffer(vertices * 2);
        }

        if (normal)
        {
            this.normVBO = window.createVBO();
            this.normals = BufferUtils.createFloatBuffer(vertices * 3);
        }
    }

    public void setColor(float r, float g, float b, float a)
    {
        this.currentR = r / 255;
        this.currentG = g / 255;
        this.currentB = b / 255;
        this.currentA = a / 255;
    }

    public void setTexCoord(float u, float v)
    {
        this.currentTexU = u;
        this.currentTexV = v;
    }

    public void setNormal(float x, float y, float z)
    {
        this.currentNormalX = x;
        this.currentNormalY = y;
        this.currentNormalZ = z;
    }

    public void addPoint(float x, float y, float z)
    {
        if (staged)
            throw new RuntimeException("Renderer is already staged!");

        this.vertices.put(x);
        this.vertices.put(y);
        this.vertices.put(z);

        if (this.colors != null)
        {
            this.colors.put(this.currentR);
            this.colors.put(this.currentG);
            this.colors.put(this.currentB);
            this.colors.put(this.currentA);
        }

        if (this.texCoords != null)
        {
            this.texCoords.put(this.currentTexU);
            this.texCoords.put(this.currentTexV);
        }

        if (this.normals != null)
        {
            this.normals.put(this.currentNormalX);
            this.normals.put(this.currentNormalY);
            this.normals.put(this.currentNormalZ);
        }

        for (ShaderGroup.Attribute a : attributeBuffers.keySet())
        {
            FloatBuffer b = this.attributeBuffers.get(a);
            float[] vals = this.floatAttributes.get(a);

            for (float f : vals)
            {
                b.put(f);
            }
        }
    }

    @Override
    public void setNormal(float[] n)
    {
        this.setNormal(n[0], n[1], n[2]);
    }

    public void setAttribute(ShaderGroup.Attribute a, float... floats)
    {
        float[] attribute = this.floatAttributes.get(a);
        int index = 0;
        for (float f: floats)
        {
            attribute[index] = f;
            index++;
        }
    }

    public void setGlow(float g)
    {
        this.colorGlow = g;
    }

    public void stage()
    {
        if (this.staged)
            return;

        this.vertices.flip();
        this.window.vertexBufferData(this.vertVBO, this.vertices);

        if (colVBO >= 0)
        {
            this.colors.flip();
            this.window.vertexBufferData(this.colVBO, this.colors);
        }

        if (texVBO >= 0)
        {
            this.texCoords.flip();
            this.window.vertexBufferData(this.texVBO, this.texCoords);
        }

        if (normVBO >= 0)
        {
            this.normals.flip();
            this.window.vertexBufferData(this.normVBO, this.normals);
        }

        for (ShaderGroup.Attribute a: this.shader.attributes)
        {
            Buffer b = this.attributeBuffers.get(a);
            b.flip();

            this.window.vertexBufferData(this.attributeVBOs.get(a), b);
        }

        this.staged = true;
    }

    public void draw()
    {
        if (!staged)
            this.stage();

        this.shader.setVertexBuffer(vertVBO);
        this.shader.setColorBuffer(colVBO);
        this.shader.setTexCoordBuffer(texVBO);
        this.shader.setNormalBuffer(normVBO);

        for (ShaderGroup.Attribute a: this.shader.attributes)
            this.shader.setCustomBuffer(a, this.attributeVBOs.get(a), a.count);

        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        Translation.transform(window, posX / window.absoluteWidth, posY / window.absoluteHeight, posZ / window.absoluteDepth);
        Rotation.transform(window, -pitch, -roll, -yaw);
        Scale.transform(window, sX, sY, sZ);

        if (depth)
            window.enableDepthtest();

        if (depthMask)
            window.enableDepthmask();
        else
            window.disableDepthmask();

        if (this.glow)
            window.setGlowBlendFunc();
        else
            window.setTransparentBlendFunc();

        if (this.texture != null)
            window.setTexture(this.texture, false);

        this.window.setColor(255, 255, 255, 255, this.colorGlow);

        this.shader.drawVBO(this.vertices.limit() / 3);

        window.disableDepthtest();
        window.disableTexture();
        window.setTransparentBlendFunc();
        window.enableDepthmask();

        glPopMatrix();
    }

    @Override
    public void free()
    {
        this.window.freeVBO(this.vertVBO);

        if (this.colVBO >= 0)
            this.window.freeVBO(this.colVBO);

        if (this.normVBO >= 0)
            this.window.freeVBO(this.normVBO);

        if (this.texVBO >= 0)
            this.window.freeVBO(this.texVBO);

        for (ShaderGroup.Attribute a: this.shader.attributes)
            this.window.freeVBO(this.attributeVBOs.get(a));

        this.attributeVBOs.clear();
    }

    public void settings(boolean depth)
    {
        this.settings(depth, false);
    }

    public void settings(boolean depth, boolean glow)
    {
        this.settings(depth, glow, !(glow));
    }

    public void settings(boolean depth, boolean glow, boolean depthMask)
    {
        this.depth = depth;
        this.glow = glow;
        this.depthMask = depthMask;
    }
}
