package lwjglwindow;

import basewindow.BaseShapeBatchRenderer;
import basewindow.IBatchRenderableObject;
import basewindow.ShaderGroup;
import basewindow.transformation.Rotation;
import basewindow.transformation.Scale;
import basewindow.transformation.Translation;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.lwjgl.BufferUtils;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;

@SuppressWarnings("ForLoopReplaceableByForEach")
public class VBOStaticBatchRenderer extends BaseShapeBatchRenderer
{
    // Property class for parallel arrays
    public static class AttributeProperty
    {
        public int vboId;
        public FloatBuffer buffer;
        public float[] floatArray;
        public ShaderGroup.Attribute attribute;

        public AttributeProperty(int vboId, FloatBuffer buffer, float[] floatArray, ShaderGroup.Attribute attribute)
        {
            this.vboId = vboId;
            this.buffer = buffer;
            this.floatArray = floatArray;
            this.attribute = attribute;
        }
    }

    public LWJGLWindow window;

    public ShaderGroup shader;
    public int vertVBO, colVBO = -1, texVBO = -1, normVBO = -1;

    // Parallel arrays with ID map
    public Object2IntOpenHashMap<ShaderGroup.Attribute> attributeToId = new Object2IntOpenHashMap<>();
    public ArrayList<AttributeProperty> attributeProperties = new ArrayList<>();

    protected int vertexCount;
    protected FloatBuffer vertices, colors, texCoords, normals;

    public boolean staged = false;

    protected float colorGlow;

    protected float currentR, currentG, currentB, currentA;
    protected float currentTexU, currentTexV;
    protected float currentNormalX, currentNormalY, currentNormalZ;
    public boolean depth = false, glow = false, depthMask = false;

    public String texture;

    public VBOStaticBatchRenderer(LWJGLWindow window, ShaderGroup shader, boolean color, String texture, boolean normal, int vertices)
    {
        super(false);

        this.window = window;
        this.vertexCount = vertices;

        this.vertVBO = window.createVBO();
        this.vertices = BufferUtils.createFloatBuffer(vertices * 3);

        this.shader = shader;

        for (ShaderGroup.Attribute a : shader.attributes)
        {
            int attributeId = this.attributeProperties.size();
            this.attributeToId.put(a, attributeId);
            int vboId = window.createVBO();
            FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices * a.count);
            float[] floatArray = new float[a.count];
            this.attributeProperties.add(new AttributeProperty(vboId, buffer, floatArray, a));
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

    @Override
    public void delete(IBatchRenderableObject o)
    {

    }

    @Override
    public void beginAdd(IBatchRenderableObject o)
    {

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

        for (int i = 0; i < attributeProperties.size(); i++)
        {
            AttributeProperty prop = attributeProperties.get(i);
            for (float f : prop.floatArray)
            {
                prop.buffer.put(f);
            }
        }
    }

    public void setNormal(float[] n)
    {
        this.setNormal(n[0], n[1], n[2]);
    }

    public void setAttribute(ShaderGroup.Attribute a, float... floats)
    {
        int attributeId = this.attributeToId.getInt(a);
        AttributeProperty prop = this.attributeProperties.get(attributeId);
        int index = 0;
        for (float f: floats)
        {
            prop.floatArray[index] = f;
            index++;
        }
    }

    public void setGlow(float g)
    {
        this.colorGlow = g;
    }

    @Override
    public void addAttribute(ShaderGroup.Attribute attribute)
    {

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

        for (int i = 0; i < attributeProperties.size(); i++)
        {
            AttributeProperty prop = attributeProperties.get(i);
            Buffer b = prop.buffer;
            b.flip();

            this.window.vertexBufferData(prop.vboId, b);
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
        {
            int attributeId = this.attributeToId.getInt(a);
            AttributeProperty prop = this.attributeProperties.get(attributeId);
            this.shader.setCustomBuffer(a, prop.vboId, a.count);
        }

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
    public void endModification()
    {

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

        for (int i = 0; i < attributeProperties.size(); i++)
        {
            AttributeProperty prop = attributeProperties.get(i);
            this.window.freeVBO(prop.vboId);
        }

        this.attributeProperties.clear();
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
