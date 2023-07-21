package lwjglwindow;

import basewindow.BaseShaderUtil;
import basewindow.BaseShapeBatchRenderer2;
import basewindow.IBatchRenderableObject;
import basewindow.ShaderProgram;
import basewindow.transformation.Rotation;
import basewindow.transformation.Scale;
import basewindow.transformation.Translation;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static org.lwjgl.opengl.GL11.*;

public class VBOShapeBatchRenderer2 extends BaseShapeBatchRenderer2
{
    public int vertVBO = -1;
    public int colVBO = -1;
    public HashMap<ShaderProgram.Attribute, Integer> attributeVBOs = new HashMap<>();

    public boolean initialized = false;

    public LinkedHashMap<IBatchRenderableObject, Integer> bufferStartPoints = new LinkedHashMap<>();
    public LinkedHashMap<IBatchRenderableObject, Integer> bufferSizes = new LinkedHashMap<>();

    public int size = 0;
    public int capacity = 6000;
    public int initSize = 0;

    public FloatBuffer vertBuffer = BufferUtils.createFloatBuffer(capacity * 3);
    public FloatBuffer colBuffer = BufferUtils.createFloatBuffer(capacity * 4);
    protected HashMap<ShaderProgram.Attribute, Buffer> attributeBuffers = new HashMap<>();
    protected HashMap<ShaderProgram.Attribute, int[]> intAttributes = new HashMap<>();
    protected HashMap<ShaderProgram.Attribute, float[]> floatAttributes = new HashMap<>();

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

    public boolean depth = false;
    public boolean glow = false;
    public boolean depthMask = false;

    public IBatchRenderableObject modifying = null;
    public int modifyingSize = -1;
    public int modifyingWritten = 0;

    public ShaderProgram shader;

    public VBOShapeBatchRenderer2(LWJGLWindow window)
    {
        super(true);
        this.window = window;
        this.shader = window.currentShader;
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

        window.disableDepthtest();
        glDepthMask(true);
        window.setTransparentBlendFunc();
    }

    @Override
    public void free()
    {
        this.window.freeVBO(this.colVBO);
        this.window.freeVBO(this.vertVBO);
    }

    public void setColor(float r, float g, float b, float a, float glow)
    {
        this.currentR = r / 255;
        this.currentG = g / 255;
        this.currentB = b / 255;
        this.currentA = a / 255 + ((int) (glow * 256) * 2);
    }

    public void expand()
    {
        this.vertBuffer.rewind();
        this.colBuffer.rewind();

        for (ShaderProgram.Attribute a: this.attributeBuffers.keySet())
        {
            this.attributeBuffers.get(a).rewind();
        }

        LinkedHashMap<IBatchRenderableObject, Integer> newBufferStartPoints = new LinkedHashMap<>();

        int totalSize = 0;
        for (IBatchRenderableObject o : this.bufferStartPoints.keySet())
        {
            totalSize += this.bufferSizes.get(o);
        }

        int newCapacity = this.capacity * 2;

        if (totalSize <= this.capacity / 2)
            newCapacity = this.capacity;

        FloatBuffer newVertBuffer = BufferUtils.createFloatBuffer(newCapacity * 3);
        FloatBuffer newColBuffer = BufferUtils.createFloatBuffer(newCapacity * 4);
        HashMap<ShaderProgram.Attribute, Buffer> newAttributeBuffers = new HashMap<>();
        for (ShaderProgram.Attribute a: this.attributeBuffers.keySet())
        {
            if (a.dataType == BaseShaderUtil.FLOAT)
                newAttributeBuffers.put(a, BufferUtils.createFloatBuffer(newCapacity * a.count));
            else if (a.dataType == BaseShaderUtil.INT)
                newAttributeBuffers.put(a, BufferUtils.createIntBuffer(newCapacity * a.count));
        }

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

                for (ShaderProgram.Attribute a: this.attributeBuffers.keySet())
                {
                    for (int i = 0; i < a.count; i++)
                    {
                        Buffer b = this.attributeBuffers.get(a);
                        if (b instanceof FloatBuffer)
                            ((FloatBuffer) b).get();
                        else if (b instanceof IntBuffer)
                            ((IntBuffer) b).get();
                    }
                }
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

                for (ShaderProgram.Attribute a: this.attributeBuffers.keySet())
                {
                    for (int i = 0; i < a.count; i++)
                    {
                        Buffer b = this.attributeBuffers.get(a);
                        if (b instanceof FloatBuffer)
                            ((FloatBuffer)newAttributeBuffers.get(a)).put((FloatBuffer) b).get();
                        else if (b instanceof IntBuffer)
                            ((IntBuffer)newAttributeBuffers.get(a)).put((IntBuffer) b).get();
                    }
                }

                pos++;
                newPos++;
            }
        }

        this.vertBuffer = newVertBuffer;
        this.colBuffer = newColBuffer;

        for (ShaderProgram.Attribute a: newAttributeBuffers.keySet())
        {
            this.attributeBuffers.put(a, newAttributeBuffers.get(a));
        }

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

            for (ShaderProgram.Attribute a: attributeBuffers.keySet())
            {
                Buffer b = this.attributeBuffers.get(a);
                if (b instanceof FloatBuffer)
                    b.limit(b.capacity());
                else if (b instanceof IntBuffer)
                    b.limit(b.capacity());

                this.window.vertexBufferDataDynamic(this.attributeVBOs.get(a), b);
            }

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

        for (ShaderProgram.Attribute a: attributeBuffers.keySet())
        {
            if (a.dataType == BaseShaderUtil.FLOAT)
            {
                FloatBuffer b = (FloatBuffer) this.attributeBuffers.get(a);
                float[] vals = this.floatAttributes.get(a);
                for (float f: vals)
                {
                    b.put(f);
                }
            }
            else if (a.dataType == BaseShaderUtil.INT)
            {
                IntBuffer b = (IntBuffer) this.attributeBuffers.get(a);
                int[] vals = this.intAttributes.get(a);
                for (int f: vals)
                {
                    b.put(f);
                }
            }
        }

        if (this.modifyingSize < 0)
            this.size++;
        else
            this.modifyingWritten++;
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

            for (ShaderProgram.Attribute a: attributeBuffers.keySet())
            {
                for (int o = 0; o < a.count; o++)
                {
                    if (a.dataType == BaseShaderUtil.FLOAT)
                    {
                        FloatBuffer b = (FloatBuffer) this.attributeBuffers.get(a);
                        b.put(i * a.count + o, 0);
                    }
                    else if (a.dataType == BaseShaderUtil.INT)
                    {
                        IntBuffer b = (IntBuffer) this.attributeBuffers.get(a);
                        b.put(i * a.count + o, 0);
                    }
                }
            }
        }

        this.vertBuffer.position(start * 3);
        this.colBuffer.position(start * 4);
        this.vertBuffer.limit((start + this.modifyingSize) * 3);
        this.colBuffer.limit((start + this.modifyingSize) * 4);
        for (ShaderProgram.Attribute a: attributeBuffers.keySet())
        {
            this.attributeBuffers.get(a).position(start * a.count);
            this.attributeBuffers.get(a).limit((start + this.modifyingSize) * a.count);
        }

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertVBO);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, (long) Float.BYTES * start * 3, this.vertBuffer);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colVBO);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, (long) Float.BYTES * start * 4, this.colBuffer);
        for (ShaderProgram.Attribute a: attributeBuffers.keySet())
        {
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.attributeVBOs.get(a));

            if (a.dataType == BaseShaderUtil.FLOAT)
                GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, (long) Float.BYTES * start * a.count, (FloatBuffer) this.attributeBuffers.get(a));
            else if (a.dataType == BaseShaderUtil.INT)
                GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, (long) Integer.BYTES * start * a.count, (IntBuffer) this.attributeBuffers.get(a));

        }

        this.vertBuffer.position(this.size * 3);
        this.colBuffer.position(this.size * 4);
        this.vertBuffer.limit(this.vertBuffer.capacity());
        this.colBuffer.limit(this.colBuffer.capacity());
        for (ShaderProgram.Attribute a: attributeBuffers.keySet())
        {
            this.attributeBuffers.get(a).position(this.size * a.count);
            this.attributeBuffers.get(a).limit(this.attributeBuffers.get(a).capacity());
        }
    }

    public void beginAdd(IBatchRenderableObject o)
    {
        if (this.initialized)
        {
            this.vertBuffer.limit(this.capacity * 3);
            this.colBuffer.limit(this.capacity * 4);
            for (ShaderProgram.Attribute a: attributeBuffers.keySet())
            {
                this.attributeBuffers.get(a).limit(this.capacity * a.count);
            }

            if (this.modifying != o)
            {
                this.endModification();
                this.modifyingWritten = 0;

                if (this.bufferStartPoints.get(o) != null)
                {
                    this.modifyingSize = this.bufferSizes.get(o);
                    this.vertBuffer.position(this.bufferStartPoints.get(o) * 3);
                    this.colBuffer.position(this.bufferStartPoints.get(o) * 4);
                    for (ShaderProgram.Attribute a: attributeBuffers.keySet())
                    {
                        this.attributeBuffers.get(a).position(this.bufferStartPoints.get(o) * a.count);
                    }
                }
                else
                {
                    this.modifyingSize = -1;
                    this.vertBuffer.position(this.size * 3);
                    this.colBuffer.position(this.size * 4);
                    for (ShaderProgram.Attribute a: attributeBuffers.keySet())
                    {
                        this.attributeBuffers.get(a).position(this.size * a.count);
                    }
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
            for (ShaderProgram.Attribute a: attributeBuffers.keySet())
            {
                this.attributeBuffers.get(a).position(this.initSize * a.count);
            }

            this.vertBuffer.limit(this.size * 3);
            this.colBuffer.limit(this.size * 4);
            for (ShaderProgram.Attribute a: attributeBuffers.keySet())
            {
                this.attributeBuffers.get(a).limit(this.size * a.count);
            }

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertVBO);
            GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, (long) Float.BYTES * this.initSize * 3, this.vertBuffer);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colVBO);
            GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, (long) Float.BYTES * this.initSize * 4, this.colBuffer);
            for (ShaderProgram.Attribute a: attributeBuffers.keySet())
            {
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.attributeVBOs.get(a));
                // Ints are the same size as floats, so we should be good for now
                GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, (long) Float.BYTES * this.initSize * a.count, this.vertBuffer);
            }

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
        for (ShaderProgram.Attribute a: attributeBuffers.keySet())
        {
            this.attributeBuffers.get(a).position(pos * a.count);
        }

        for (int i = pos; i < pos + size; i++)
        {
            this.vertBuffer.put(i * 3, 0f);
            this.vertBuffer.put(i * 3 + 1, 0f);
            this.vertBuffer.put(i * 3 + 2, 0f);

            this.colBuffer.put(i * 4, 0f);
            this.colBuffer.put(i * 4 + 1, 0f);
            this.colBuffer.put(i * 4 + 2, 0f);
            this.colBuffer.put(i * 4 + 3, 0f);

            for (ShaderProgram.Attribute a: attributeBuffers.keySet())
            {
                if (a.dataType == BaseShaderUtil.FLOAT)
                    ((FloatBuffer) this.attributeBuffers.get(a)).put(0);
                else if (a.dataType == BaseShaderUtil.INT)
                    ((IntBuffer) this.attributeBuffers.get(a)).put(0);
            }
        }

        this.vertBuffer.rewind();
        this.colBuffer.rewind();
        for (ShaderProgram.Attribute a: attributeBuffers.keySet())
        {
            this.attributeBuffers.get(a).rewind();
        }

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertVBO);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, (long) Float.BYTES * pos * 3, new float[3 * size]);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colVBO);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, (long) Float.BYTES * pos * 4, new float[4 * size]);
        for (ShaderProgram.Attribute a: attributeBuffers.keySet())
        {
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.attributeVBOs.get(a));
            // Ints are the same size as floats, so we should be good for now
            GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, (long) Float.BYTES * pos * a.count, new float[a.count * size]);
        }

    }

    public void moveFloat(FloatBuffer b, int mul, int off, int rem)
    {
        b.put(this.size * mul + off,  b.get(rem * mul + off));
        b.put(rem * mul + off, 0f);
    }

    public void moveInt(IntBuffer b, int mul, int off, int rem)
    {
        b.put(this.size * mul + off,  b.get(rem * mul + off));
        b.put(rem * mul + off, 0);
    }

    public void migrate(IBatchRenderableObject o)
    {
        if (this.capacity <= this.size + this.bufferSizes.get(o))
            this.expand();

        int pos = this.bufferStartPoints.get(o);
        int size = this.bufferSizes.get(o);

        this.vertBuffer.position(pos * 3);
        this.colBuffer.position(pos * 4);
        for (ShaderProgram.Attribute a: attributeBuffers.keySet())
        {
            this.attributeBuffers.get(a).position(pos * a.count);
        }

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

            for (ShaderProgram.Attribute a: attributeBuffers.keySet())
            {
                for (int f = 0; f < a.count; f++)
                {
                    if (a.dataType == BaseShaderUtil.FLOAT)
                        this.moveFloat((FloatBuffer) this.attributeBuffers.get(a), a.count, f, i);
                    else if (a.dataType == BaseShaderUtil.INT)
                        this.moveInt((IntBuffer) this.attributeBuffers.get(a), a.count, f, i);
                }
            }

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
        for (ShaderProgram.Attribute a: attributeBuffers.keySet())
        {
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.attributeVBOs.get(a));
            // Ints are the same size as floats, so we should be good for now
            GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, (long) Float.BYTES * pos * a.count, new float[a.count * size]);
        }
    }

    public void addAttribute(ShaderProgram.Attribute attribute)
    {
        if (attribute.dataType == BaseShaderUtil.FLOAT)
        {
            this.attributeBuffers.put(attribute, BufferUtils.createFloatBuffer(capacity * attribute.count));
            this.floatAttributes.put(attribute, new float[attribute.count]);
        }
        else if (attribute.dataType == BaseShaderUtil.INT)
        {
            this.attributeBuffers.put(attribute, BufferUtils.createIntBuffer(capacity * attribute.count));
            this.intAttributes.put(attribute, new int[attribute.count]);
        }
    }

    public void setAttribute(ShaderProgram.Attribute a, int... ints)
    {
        int[] attribute = this.intAttributes.get(a);
        int index = 0;
        for (int i: ints)
        {
            attribute[index] = i;
            index++;
        }
    }

    public void setAttribute(ShaderProgram.Attribute a, float... floats)
    {
        float[] attribute = this.floatAttributes.get(a);
        int index = 0;
        for (float f: floats)
        {
            attribute[index] = f;
            index++;
        }
    }

    public void stage()
    {
        this.initialized = true;

        this.vertVBO = this.window.createVBO();
        this.colVBO = this.window.createVBO();

        for (ShaderProgram.Attribute a: this.attributeBuffers.keySet())
        {
            this.attributeVBOs.put(a, this.window.createVBO());
            this.attributeBuffers.get(a).flip();
        }

        this.initSize = this.size;
        this.vertBuffer.flip();
        this.colBuffer.flip();

        if (this.dynamic)
        {
            this.vertBuffer.limit(this.vertBuffer.capacity());
            this.colBuffer.limit(this.colBuffer.capacity());
            this.window.vertexBufferDataDynamic(vertVBO, vertBuffer);
            this.window.vertexBufferDataDynamic(colVBO, colBuffer);

            for (ShaderProgram.Attribute a: this.attributeBuffers.keySet())
            {
                this.attributeBuffers.get(a).flip();
                this.window.vertexBufferDataDynamic(this.attributeVBOs.get(a), this.attributeBuffers.get(a));
            }
        }
        else
        {
            this.window.vertexBufferData(vertVBO, vertBuffer);
            this.window.vertexBufferData(colVBO, colBuffer);

            for (ShaderProgram.Attribute a: this.attributeBuffers.keySet())
            {
                this.window.vertexBufferData(this.attributeVBOs.get(a), this.attributeBuffers.get(a));
            }
        }
    }

    public void batchDraw()
    {
        this.endModification();

        if (!this.initialized)
            this.stage();

        this.window.setColor(255, 255, 255, 255);

        this.shader.util.setVertexBuffer(vertVBO);
        this.shader.util.setColorBuffer(colVBO);

        for (ShaderProgram.Attribute a: this.shader.attributes)
            this.shader.util.setCustomBuffer(a, this.attributeVBOs.get(a), a.count, a.dataType);

        this.shader.util.drawVBO(this.size);
    }
}
