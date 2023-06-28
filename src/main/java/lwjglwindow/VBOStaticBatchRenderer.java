package lwjglwindow;

import basewindow.BaseStaticBatchRenderer;
import basewindow.ShaderProgram;
import basewindow.transformation.Rotation;
import basewindow.transformation.Scale;
import basewindow.transformation.Translation;
import org.lwjgl.BufferUtils;

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

    public ShaderProgram shader;
    public int vertVBO = -1;
    public int colVBO = -1;
    public int texVBO = -1;
    public int normVBO = -1;
    public HashMap<ShaderProgram.Attribute, Integer> attributeVBOs = new HashMap<>();

    protected int vertexCount;

    protected FloatBuffer vertices;
    protected FloatBuffer colors;
    protected FloatBuffer texCoords;
    protected FloatBuffer normals;
    protected HashMap<ShaderProgram.Attribute, Buffer> attributeBuffers = new HashMap<>();

    public boolean staged = false;

    public VBOStaticBatchRenderer(LWJGLWindow window, ShaderProgram shader, boolean color, String texture, boolean normal, int vertices)
    {
        this.window = window;
        this.vertexCount = vertices;

        this.vertVBO = window.createVBO();
        this.vertices = BufferUtils.createFloatBuffer(vertices * 3);

        this.shader = shader;

        for (ShaderProgram.Attribute a : shader.attributes)
        {
            attributeVBOs.put(a, window.createVBO());

            if (a.dataType == FLOAT)
                attributeBuffers.put(a, BufferUtils.createFloatBuffer(vertices * a.count));
            else if (a.dataType == INT)
                attributeBuffers.put(a, BufferUtils.createIntBuffer(vertices * a.count));
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

    public void addVertex(float x, float y, float z)
    {
        this.vertices.put(x);
        this.vertices.put(y);
        this.vertices.put(z);
    }

    public void addColor(float r, float g, float b, float a)
    {
        this.colors.put(r);
        this.colors.put(g);
        this.colors.put(b);
        this.colors.put(a);
    }

    public void addTexCoord(float u, float v)
    {
        this.texCoords.put(u);
        this.texCoords.put(v);
    }

    public void addNormal(float x, float y, float z)
    {
        this.normals.put(x);
        this.normals.put(y);
        this.normals.put(z);
    }

    @Override
    public void addNormal(float[] n)
    {
        this.addNormal(n[0], n[1], n[2]);
    }

    public void addAttributeF(ShaderProgram.Attribute a, float... floats)
    {
        FloatBuffer b = (FloatBuffer) this.attributeBuffers.get(a);

        for (float f: floats)
        {
            b.put(f);
        }
    }

    public void addAttributeI(ShaderProgram.Attribute a, int... ints)
    {
        IntBuffer b = (IntBuffer) this.attributeBuffers.get(a);

        for (int i: ints)
        {
            b.put(i);
        }
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

        for (ShaderProgram.Attribute a: this.shader.attributes)
        {
            Buffer b = this.attributeBuffers.get(a);
            b.flip();

            this.window.vertexBufferData(this.attributeVBOs.get(a), b);
        }

        this.staged = true;
    }

    public void draw(double posX, double posY, double posZ, double sX, double sY, double sZ, double yaw, double pitch, double roll, boolean depth, boolean depthWrite)
    {
        if (!staged)
            throw new RuntimeException("Drawing an unstaged batch");

        this.shader.util.setVertexBuffer(vertVBO);
        this.shader.util.setColorBuffer(colVBO);
        this.shader.util.setTexCoordBuffer(texVBO);
        this.shader.util.setNormalBuffer(normVBO);

        for (ShaderProgram.Attribute a: this.shader.attributes)
            this.shader.util.setCustomBuffer(a, this.attributeVBOs.get(a), a.count, a.dataType);

        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        Translation.transform(window, posX / window.absoluteWidth, posY / window.absoluteHeight, posZ / window.absoluteDepth);
        Rotation.transform(window, -pitch, -roll, -yaw);
        Scale.transform(window, sX, sY, sZ);

        if (depth)
            window.enableDepthtest();

        if (depthWrite)
            window.enableDepthmask();
        else
            window.disableDepthmask();

        if (this.texture != null)
            window.setTexture(this.texture, false);

        this.shader.util.drawVBO(this.vertexCount);

        window.disableDepthtest();
        window.disableTexture();

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

        for (ShaderProgram.Attribute a: this.shader.attributes)
            this.window.freeVBO(this.attributeVBOs.get(a));
    }
}
