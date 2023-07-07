package lwjglwindow;

import basewindow.BaseShapeBatchRenderer;
import basewindow.BaseShapeBatchRenderer2;
import basewindow.IBaseShader;
import basewindow.IBatchRenderableObject;
import basewindow.transformation.Rotation;
import basewindow.transformation.Scale;
import basewindow.transformation.Translation;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

import java.nio.FloatBuffer;
import java.util.LinkedHashMap;

import static org.lwjgl.opengl.GL11.*;

public class VBOShapeBatchRenderer2 extends BaseShapeBatchRenderer2
{
    public int vertVBO = -1;
    public int colVBO = -1;

    public boolean initialized = false;

    public LinkedHashMap<IBatchRenderableObject, Integer> bufferStartPoints = new LinkedHashMap<>();
    public LinkedHashMap<IBatchRenderableObject, Integer> bufferSizes = new LinkedHashMap<>();

    public int size = 0;
    public int capacity = 6000;
    public int initSize = 0;

    public FloatBuffer vertBuffer = BufferUtils.createFloatBuffer(capacity * 3);
    public FloatBuffer colBuffer = BufferUtils.createFloatBuffer(capacity * 4);

    public LWJGLWindow window;

    public float colorR;
    public float colorG;
    public float colorB;
    public float colorA;
    public float colorGlow;

    public float currentR;
    public float currentG;
    public float currentB;
    public float currentA;

    public boolean batching = false;
    public boolean depth = false;
    public boolean glow = false;
    public boolean depthMask = false;

    public IBatchRenderableObject modifying = null;
    public int modifyingSize = -1;
    public int modifyingWritten = 0;

    public VBOShapeBatchRenderer2(LWJGLWindow window)
    {
        super(true);
        this.window = window;
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
        this.batching = true;
        this.depth = depth;
        this.glow = glow;
        this.depthMask = depthMask;
    }

    public void draw()
    {
        glDepthMask(this.depthMask);

        if (this.depth)
        {
            window.enableDepthtest();
            glDepthFunc(GL_LEQUAL);
        }
        else
        {
            window.disableDepthtest();
            glDepthFunc(GL_ALWAYS);
        }

        if (this.glow)
            window.setGlowBlendFunc();
        else
            window.setTransparentBlendFunc();

        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        Translation.transform(window, posX / window.absoluteWidth, posY / window.absoluteHeight, posZ / window.absoluteDepth);
        Rotation.transform(window, -pitch, -roll, -yaw);
        Scale.transform(window, sX, sY, sZ);

        this.batchDraw();

        glPopMatrix();

        this.batching = false;

        window.disableDepthtest();
        glDepthMask(true);
        window.setTransparentBlendFunc();
    }

    public void setColor(double r, double g, double b, double a, double glow)
    {
        this.colorR = (float) (r / 255);
        this.colorG = (float) (g / 255);
        this.colorB = (float) (b / 255);
        this.colorA = (float) (a / 255);
        this.colorGlow = (float) glow;
    }

    @Override
    public void free()
    {
        System.out.println(" x " + this.colVBO + " " + this.vertVBO);
        this.window.freeVBO(this.colVBO);
        this.window.freeVBO(this.vertVBO);
    }

    public void setWorkingColor(float r, float g, float b, float a, float glow)
    {
        this.currentR = r;
        this.currentG = g;
        this.currentB = b;
        this.currentA = a + ((int) (glow * 256) * 2);
    }

    public void expand()
    {
        this.vertBuffer.rewind();
        this.colBuffer.rewind();

        LinkedHashMap<IBatchRenderableObject, Integer> newBufferStartPoints = new LinkedHashMap<>();

        int newCapacity = this.capacity * 2;

        FloatBuffer newVertBuffer = BufferUtils.createFloatBuffer(newCapacity * 3);
        FloatBuffer newColBuffer = BufferUtils.createFloatBuffer(newCapacity * 4);

        int pos = 0;
        int newPos = 0;
        for (IBatchRenderableObject o : this.bufferStartPoints.keySet())
        {
            int start = this.bufferStartPoints.get(o);
            int size = this.bufferSizes.get(o);

            while (pos < start)
            {
                this.vertBuffer.get();
                this.vertBuffer.get();
                this.vertBuffer.get();
                this.colBuffer.get();
                this.colBuffer.get();
                this.colBuffer.get();
                this.colBuffer.get();
                pos++;
            }

            newBufferStartPoints.put(o, newPos);

            while (pos < start + size)
            {
                newVertBuffer.put(this.vertBuffer.get());
                newVertBuffer.put(this.vertBuffer.get());
                newVertBuffer.put(this.vertBuffer.get());
                newColBuffer.put(this.colBuffer.get());
                newColBuffer.put(this.colBuffer.get());
                newColBuffer.put(this.colBuffer.get());
                newColBuffer.put(this.colBuffer.get());
                pos++;
                newPos++;
            }
        }

        this.vertBuffer = newVertBuffer;
        this.colBuffer = newColBuffer;
        this.bufferStartPoints = newBufferStartPoints;
        this.size = newPos;
        this.capacity = newCapacity;

        if (this.initialized)
        {
            newVertBuffer.flip();
            newColBuffer.flip();

            this.vertBuffer.limit(this.vertBuffer.capacity());
            this.colBuffer.limit(this.colBuffer.capacity());
            this.window.vertexBufferDataDynamic(vertVBO, vertBuffer);
            this.window.vertexBufferDataDynamic(colVBO, colBuffer);

            this.initSize = this.size;
        }
    }

    public void addPoint(IBatchRenderableObject o, float x, float y, float z)
    {
        if (this.modifyingSize < 0)
        {
            if (this.size >= this.capacity)
                this.expand();
        }

        if (this.modifyingSize >= 0 && this.modifyingWritten >= this.modifyingSize)
        {
            this.migrate(o);
        }

        if (this.bufferStartPoints.get(o) == null)
        {
            this.bufferStartPoints.put(o, this.size);
            this.bufferSizes.put(o, 1);
        }
        else if (this.modifyingSize < 0)
            this.bufferSizes.put(o, this.bufferSizes.get(o) + 1);

        this.vertBuffer.put(x);
        this.vertBuffer.put(y);
        this.vertBuffer.put(z);
        this.colBuffer.put(this.currentR);
        this.colBuffer.put(this.currentG);
        this.colBuffer.put(this.currentB);
        this.colBuffer.put(this.currentA);

        if (this.modifyingSize < 0)
            this.size++;
        else
            this.modifyingWritten++;
    }

    @Override
    public void fillRect(IBatchRenderableObject o, double x, double y, double sX, double sY)
    {
        this.beginAdd(o);

        if (this.window.shadowsEnabled && !this.window.drawingShadow)
            return;

        float x0 = (float) x;
        float y0 = (float) y;

        float x1 = (float) (x + sX);
        float y1 = (float) (y + sY);

        float r1 = this.colorR;
        float g1 = this.colorG;
        float b1 = this.colorB;
        float a = this.colorA;
        float g = this.colorGlow;

        this.setWorkingColor(r1, g1, b1, a, g);
        this.addPoint(o, x1, y0, 0);
        this.addPoint(o, x0, y0, 0);
        this.addPoint(o, x0, y1, 0);

        this.addPoint(o, x1, y0, 0);
        this.addPoint(o, x1, y1, 0);
        this.addPoint(o, x0, y1, 0);

        this.endAdd();
    }

    @Override
    public void fillBox(IBatchRenderableObject o, double x, double y, double z, double sX, double sY, double sZ, byte options)
    {
        if (this.window.shadowsEnabled && !this.window.drawingShadow)
            return;

        this.beginAdd(o);

        float x0 = (float) x;
        float y0 = (float) y;
        float z0 = (float) z;

        float x1 = (float) (x + sX);
        float y1 = (float) (y + sY);
        float z1 = (float) (z + sZ);

        float r1 = this.colorR;
        float g1 = this.colorG;
        float b1 = this.colorB;
        float a = this.colorA;
        float g = this.colorGlow;

        float r2 = r1 * 0.8f;
        float g2 = g1 * 0.8f;
        float b2 = b1 * 0.8f;

        float r3 = r1 * 0.6f;
        float g3 = g1 * 0.6f;
        float b3 = b1 * 0.6f;

        if (options % 2 == 0)
        {
            this.setWorkingColor(r1, g1, b1, a, g);
            this.addPoint(o, x1, y0, z0);
            this.addPoint(o, x0, y0, z0);
            this.addPoint(o, x0, y1, z0);

            this.addPoint(o, x1, y0, z0);
            this.addPoint(o, x1, y1, z0);
            this.addPoint(o, x0, y1, z0);
        }

        if ((options >> 2) % 2 == 0)
        {
            this.setWorkingColor(r2, g2, b2, a, g);
            this.addPoint(o, x1, y1, z1);
            this.addPoint(o, x0, y1, z1);
            this.addPoint(o, x0, y1, z0);

            this.addPoint(o, x1, y1, z1);
            this.addPoint(o, x1, y1, z0);
            this.addPoint(o, x0, y1, z0);
        }

        if ((options >> 3) % 2 == 0)
        {
            this.setWorkingColor(r2, g2, b2, a, g);
            this.addPoint(o, x1, y0, z1);
            this.addPoint(o, x0, y0, z1);
            this.addPoint(o, x0, y0, z0);

            this.addPoint(o, x1, y0, z1);
            this.addPoint(o, x1, y0, z0);
            this.addPoint(o, x0, y0, z0);
        }

        if ((options >> 4) % 2 == 0)
        {
            this.setWorkingColor(r3, g3, b3, a, g);
            this.addPoint(o, x0, y1, z1);
            this.addPoint(o, x0, y1, z0);
            this.addPoint(o, x0, y0, z0);

            this.addPoint(o, x0, y1, z1);
            this.addPoint(o, x0, y0, z1);
            this.addPoint(o, x0, y0, z0);
        }

        if ((options >> 5) % 2 == 0)
        {
            this.setWorkingColor(r3, g3, b3, a, g);
            this.addPoint(o, x1, y1, z0);
            this.addPoint(o, x1, y1, z1);
            this.addPoint(o, x1, y0, z1);

            this.addPoint(o, x1, y1, z0);
            this.addPoint(o, x1, y0, z0);
            this.addPoint(o, x1, y0, z1);
        }

        if ((options >> 1) % 2 == 0)
        {
            this.setWorkingColor(r1, g1, b1, a, g);
            this.addPoint(o, x1, y1, z1);
            this.addPoint(o, x0, y1, z1);
            this.addPoint(o, x0, y0, z1);

            this.addPoint(o, x1, y1, z1);
            this.addPoint(o, x1, y0, z1);
            this.addPoint(o, x0, y0, z1);
        }

        this.endAdd();
    }

    public void endModification()
    {
        if (this.modifying == null || this.modifyingSize < 0)
            return;

        int start = this.bufferStartPoints.get(this.modifying);
        for (int i = this.modifyingWritten + start; i < start + this.modifyingSize; i++)
        {
            this.vertBuffer.put(i * 3, 0f);
            this.vertBuffer.put(i * 3 + 1, 0f);
            this.vertBuffer.put(i * 3 + 2, 0f);

            this.colBuffer.put(i * 4, 0f);
            this.colBuffer.put(i * 4 + 1, 0f);
            this.colBuffer.put(i * 4 + 2, 0f);
            this.colBuffer.put(i * 4 + 3, 0f);
        }

        this.vertBuffer.position(start * 3);
        this.colBuffer.position(start * 4);
        this.vertBuffer.limit((start + this.modifyingSize) * 3);
        this.colBuffer.limit((start + this.modifyingSize) * 4);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertVBO);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, (long) Float.BYTES * start * 3, this.vertBuffer);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colVBO);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, (long) Float.BYTES * start * 4, this.colBuffer);

        this.vertBuffer.position(this.size * 3);
        this.colBuffer.position(this.size * 4);
        this.vertBuffer.limit(this.vertBuffer.capacity());
        this.colBuffer.limit(this.colBuffer.capacity());
    }

    public void beginAdd(IBatchRenderableObject o)
    {
        if (this.initialized)
        {
            this.vertBuffer.limit(this.capacity * 3);
            this.colBuffer.limit(this.capacity * 4);

            if (this.modifying != o)
            {
                this.endModification();
                this.modifyingWritten = 0;

                if (this.bufferStartPoints.get(o) != null)
                {
                    this.modifyingSize = this.bufferSizes.get(o);
                    this.vertBuffer.position(this.bufferStartPoints.get(o) * 3);
                    this.colBuffer.position(this.bufferStartPoints.get(o) * 4);
                }
                else
                {
                    this.modifyingSize = -1;
                    this.vertBuffer.position(this.size * 3);
                    this.colBuffer.position(this.size * 4);
                }
            }

            this.modifying = o;
        }
    }

    public void endAdd()
    {
        if (this.initSize < this.size && this.initialized)
        {
            this.vertBuffer.position(this.initSize * 3);
            this.colBuffer.position(this.initSize * 4);
            this.vertBuffer.limit(this.size * 3);
            this.colBuffer.limit(this.size * 4);

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertVBO);
            GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, (long) Float.BYTES * this.initSize * 3, this.vertBuffer);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colVBO);
            GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, (long) Float.BYTES * this.initSize * 4, this.colBuffer);

            this.initSize = this.size;
        }
    }

    public void delete(IBatchRenderableObject o)
    {
        if (!this.bufferStartPoints.containsKey(o))
            return;

        int pos = this.bufferStartPoints.remove(o);
        int size = this.bufferSizes.remove(o);

        this.vertBuffer.position(pos * 3);
        this.colBuffer.position(pos * 4);

        for (int i = pos; i < pos + size; i++)
        {
            this.vertBuffer.put(i * 3, 0f);
            this.vertBuffer.put(i * 3 + 1, 0f);
            this.vertBuffer.put(i * 3 + 2, 0f);

            this.colBuffer.put(i * 4, 0f);
            this.colBuffer.put(i * 4 + 1, 0f);
            this.colBuffer.put(i * 4 + 2, 0f);
            this.colBuffer.put(i * 4 + 3, 0f);
        }

        this.vertBuffer.rewind();
        this.colBuffer.rewind();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertVBO);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, (long) Float.BYTES * pos * 3, new float[3 * size]);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colVBO);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, (long) Float.BYTES * pos * 4, new float[4 * size]);
    }

    public void moveFloat(FloatBuffer b, int mul, int off, int rem)
    {
        b.put(this.size * mul + off,  b.get(rem * mul + off));
        b.put(rem * mul + off, 0f);
    }

    public void migrate(IBatchRenderableObject o)
    {
        if (this.capacity <= this.size + this.bufferSizes.get(o))
            this.expand();

        int pos = this.bufferStartPoints.get(o);
        int size = this.bufferSizes.get(o);

        this.vertBuffer.position(pos * 3);
        this.colBuffer.position(pos * 4);

        this.initSize = this.size;

        for (int i = pos; i < pos + size; i++)
        {
            this.moveFloat(this.vertBuffer, 3, 0, i);
            this.moveFloat(this.vertBuffer, 3, 1, i);
            this.moveFloat(this.vertBuffer, 3, 2, i);

            this.moveFloat(this.colBuffer, 4, 0, i);
            this.moveFloat(this.colBuffer, 4, 1, i);
            this.moveFloat(this.colBuffer, 4, 2, i);
            this.moveFloat(this.colBuffer, 4, 3, i);

            this.size++;
        }

        this.bufferStartPoints.put(o, initSize);
        this.modifyingSize = -1;

        this.vertBuffer.rewind();
        this.colBuffer.rewind();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertVBO);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, (long) Float.BYTES * pos * 3, new float[3 * size]);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colVBO);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, (long) Float.BYTES * pos * 4, new float[4 * size]);
    }

    public void stage()
    {
        this.initialized = true;

        this.vertVBO = this.window.createVBO();
        this.colVBO = this.window.createVBO();

        System.out.println(" + " + this.colVBO + " " + this.vertVBO);

        this.initSize = this.size;
        this.vertBuffer.flip();
        this.colBuffer.flip();

        if (this.dynamic)
        {
            this.vertBuffer.limit(this.vertBuffer.capacity());
            this.colBuffer.limit(this.colBuffer.capacity());
            this.window.vertexBufferDataDynamic(vertVBO, vertBuffer);
            this.window.vertexBufferDataDynamic(colVBO, colBuffer);
        }
        else
        {
            this.window.vertexBufferData(vertVBO, vertBuffer);
            this.window.vertexBufferData(colVBO, colBuffer);
        }
    }

    public void batchDraw()
    {
        this.endModification();

        if (!this.initialized)
            this.stage();

        IBaseShader shader = (IBaseShader) this.window.currentShader;
        this.window.setColor(255, 255, 255, 255);
        shader.renderVBO(vertVBO, colVBO, 0, 0, this.size);
    }
}
