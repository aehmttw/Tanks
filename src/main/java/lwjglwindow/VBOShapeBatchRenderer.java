package lwjglwindow;

import basewindow.BaseShapeBatchRenderer;
import basewindow.IBatchRenderableObject;
import basewindow.ShaderGroup;
import basewindow.transformation.Rotation;
import basewindow.transformation.Scale;
import basewindow.transformation.Translation;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;

@SuppressWarnings("ForLoopReplaceableByForEach")
public class VBOShapeBatchRenderer extends BaseShapeBatchRenderer
{
    public int vertVBO = -1, colVBO = -1;
    public int size = 0, capacity = 6000, initSize = 0;

    // Parallel arrays with ID maps
    public Object2IntOpenHashMap<ShaderGroup.Attribute> attributeToId = new Object2IntOpenHashMap<>();
    public ArrayList<AttributeProperty> attributeProperties = new ArrayList<>();
    public boolean initialized = false;
    public Object2IntLinkedOpenHashMap<IBatchRenderableObject> bufferToId = new Object2IntLinkedOpenHashMap<>();
    public ArrayList<BufferProperty> bufferProperties = new ArrayList<>();
    public float currentR, currentG, currentB, currentA;

    public void expand()
    {
        this.vertBuffer.rewind();
        this.colBuffer.rewind();

        for (AttributeProperty attributeProperty : attributeProperties)
            attributeProperty.buffer.rewind();

        Object2IntLinkedOpenHashMap<IBatchRenderableObject> newBufferToId = new Object2IntLinkedOpenHashMap<>();

        int totalSize = 0;
        for (BufferProperty bufferProp : this.bufferProperties)
            if (bufferProp.object != null)
                totalSize += bufferProp.size;

        int newCapacity = this.capacity * 2;

        if (totalSize <= this.capacity / 2)
            newCapacity = this.capacity;

        FloatBuffer newVertBuffer = BufferUtils.createFloatBuffer(newCapacity * 3);
        FloatBuffer newColBuffer = BufferUtils.createFloatBuffer(newCapacity * 4);
        ArrayList<FloatBuffer> newAttributeBuffers = new ArrayList<>();
        for (AttributeProperty prop : attributeProperties)
            newAttributeBuffers.add(BufferUtils.createFloatBuffer(newCapacity * prop.attribute.count));

        int pos = 0;
        int newPos = 0;
        for (int i = 0; i < this.bufferProperties.size(); i++)
        {
            BufferProperty bufferProp = this.bufferProperties.get(i);
            if (bufferProp.object == null) continue;

            IBatchRenderableObject o = bufferProp.object;
            int start = bufferProp.startPoint;
            int size = bufferProp.size;

            while (pos < start)
            {
                this.vertBuffer.get();
                this.vertBuffer.get();
                this.vertBuffer.get();
                this.colBuffer.get();
                this.colBuffer.get();
                this.colBuffer.get();
                this.colBuffer.get();

                for (AttributeProperty prop : attributeProperties)
                    for (int k = 0; k < prop.attribute.count; k++)
                        prop.buffer.get();
                pos++;
            }

            newBufferToId.put(o, i);
            bufferProp.startPoint = newPos;

            while (pos < start + size)
            {
                newVertBuffer.put(this.vertBuffer.get());
                newVertBuffer.put(this.vertBuffer.get());
                newVertBuffer.put(this.vertBuffer.get());
                newColBuffer.put(this.colBuffer.get());
                newColBuffer.put(this.colBuffer.get());
                newColBuffer.put(this.colBuffer.get());
                newColBuffer.put(this.colBuffer.get());

                for (int j = 0; j < attributeProperties.size(); j++)
                {
                    AttributeProperty prop = attributeProperties.get(j);
                    for (int k = 0; k < prop.attribute.count; k++)
                        newAttributeBuffers.get(j).put(prop.buffer.get());
                }

                pos++;
                newPos++;
            }
        }

        this.vertBuffer = newVertBuffer;
        this.colBuffer = newColBuffer;

        for (int i = 0; i < attributeProperties.size(); i++)
            attributeProperties.get(i).buffer = newAttributeBuffers.get(i);

        this.bufferToId = newBufferToId;
        this.size = newPos;
        this.capacity = newCapacity;

        this.justExpanded = true;
    }

    public FloatBuffer vertBuffer = BufferUtils.createFloatBuffer(capacity * 3);
    public FloatBuffer colBuffer = BufferUtils.createFloatBuffer(capacity * 4);

    public LWJGLWindow window;

    @SuppressWarnings("ForLoopReplaceableByForEach")
    public void addPoint(float x, float y, float z)
    {
        IBatchRenderableObject o = this.adding;
        if (this.modifyingSize < 0)
            if (this.size >= this.capacity)
                this.expand();

        if (this.modifyingSize >= 0 && this.modifyingWritten >= this.modifyingSize)
            this.migrate(o);

        if (!this.bufferToId.containsKey(o))
        {
            int bufferId = this.bufferProperties.size();
            this.bufferToId.put(o, bufferId);
            this.bufferProperties.add(BufferProperty.newInstance(this.size, 1, o));
        }
        else if (this.modifyingSize < 0)
        {
            int bufferId = this.bufferToId.getInt(o);
            BufferProperty bufferProp = this.bufferProperties.get(bufferId);
            bufferProp.size++;
        }

        this.vertBuffer.put(x);
        this.vertBuffer.put(y);
        this.vertBuffer.put(z);
        this.colBuffer.put(this.currentR);
        this.colBuffer.put(this.currentG);
        this.colBuffer.put(this.currentB);
        this.colBuffer.put(this.currentA);

        for (int i = 0, attributePropertiesSize = attributeProperties.size(); i < attributePropertiesSize; i++)
        {
            AttributeProperty prop = attributeProperties.get(i);
            float[] floatArray = prop.floatArray;
            for (int j = 0, floatArrayLength = floatArray.length; j < floatArrayLength; j++)
                prop.buffer.put(floatArray[j]);
        }

        if (this.modifyingSize < 0)
            this.size++;
        else
            this.modifyingWritten++;
    }
    public float colorGlow;

    public boolean depth = false;
    public boolean glow = false;
    public boolean depthMask = false;

    public IBatchRenderableObject modifying = null;
    public int modifyingSize = -1;
    public int modifyingWritten = 0;

    public ShaderGroup shader;

    protected IBatchRenderableObject adding = null;
    protected boolean justExpanded = false;

    public VBOShapeBatchRenderer(LWJGLWindow window)
    {
        super(true);
        this.window = window;
        this.shader = window.currentShader.group;
    }

    public VBOShapeBatchRenderer(LWJGLWindow window, ShaderGroup s)
    {
        this(window);
        this.shader = s;

        for (ShaderGroup.Attribute a: s.attributes)
            this.addAttribute(a);
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

    public void setColor(float r, float g, float b, float a)
    {
        this.currentR = r / 255;
        this.currentG = g / 255;
        this.currentB = b / 255;
        this.currentA = a / 255;
    }

    public void setGlow(float g)
    {
        this.colorGlow = g;
    }

    public void endModification()
    {
        if (this.modifying == null || this.modifyingSize < 0 || !this.bufferToId.containsKey(this.modifying))
            return;

        int bufferId = this.bufferToId.getInt(this.modifying);
        BufferProperty bufferProp = this.bufferProperties.get(bufferId);
        int start = bufferProp.startPoint;
        for (int i = this.modifyingWritten + start; i < start + this.modifyingSize; i++)
        {
            this.vertBuffer.put(i * 3, 0f);
            this.vertBuffer.put(i * 3 + 1, 0f);
            this.vertBuffer.put(i * 3 + 2, 0f);

            this.colBuffer.put(i * 4, 0f);
            this.colBuffer.put(i * 4 + 1, 0f);
            this.colBuffer.put(i * 4 + 2, 0f);
            this.colBuffer.put(i * 4 + 3, 0f);

            for (AttributeProperty prop : attributeProperties)
                for (int o = 0; o < prop.attribute.count; o++)
                    prop.buffer.put(i * prop.attribute.count + o, 0);
        }

        this.vertBuffer.position(start * 3);
        this.colBuffer.position(start * 4);
        this.vertBuffer.limit((start + this.modifyingSize) * 3);
        this.colBuffer.limit((start + this.modifyingSize) * 4);
        for (int i = 0, attributePropertiesSize = attributeProperties.size(); i < attributePropertiesSize; i++)
        {
            AttributeProperty prop = attributeProperties.get(i);
            prop.buffer.position(start * prop.attribute.count);
            prop.buffer.limit((start + this.modifyingSize) * prop.attribute.count);
        }

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertVBO);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, (long) Float.BYTES * start * 3, this.vertBuffer);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colVBO);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, (long) Float.BYTES * start * 4, this.colBuffer);
        for (int i = 0, attributePropertiesSize = attributeProperties.size(); i < attributePropertiesSize; i++)
        {
            AttributeProperty prop = attributeProperties.get(i);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, prop.vboId);
            GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, (long) Float.BYTES * start * prop.attribute.count, prop.buffer);
        }

        this.vertBuffer.limit(this.vertBuffer.capacity());
        this.colBuffer.limit(this.colBuffer.capacity());
        this.vertBuffer.position(this.size * 3);
        this.colBuffer.position(this.size * 4);
        for (int i = 0, attributePropertiesSize = attributeProperties.size(); i < attributePropertiesSize; i++)
        {
            AttributeProperty prop = attributeProperties.get(i);
            prop.buffer.limit(prop.buffer.capacity());
            prop.buffer.position(this.size * prop.attribute.count);
        }

        this.modifying = null;
    }

    public void beginAdd(IBatchRenderableObject o)
    {
        if (this.adding != o)
            this.endAdd();

        this.adding = o;

        if (this.initialized)
        {
            this.vertBuffer.limit(this.capacity * 3);
            this.colBuffer.limit(this.capacity * 4);
            for (int i = 0, attributePropertiesSize = attributeProperties.size(); i < attributePropertiesSize; i++)
            {
                AttributeProperty prop = attributeProperties.get(i);
                prop.buffer.limit(this.capacity * prop.attribute.count);
            }

            if (this.modifying != o)
            {
                this.endModification();
                this.modifyingWritten = 0;

                if (this.bufferToId.containsKey(o))
                {
                    int bufferId = this.bufferToId.getInt(o);
                    BufferProperty bufferProp = this.bufferProperties.get(bufferId);
                    this.modifyingSize = bufferProp.size;
                    this.vertBuffer.position(bufferProp.startPoint * 3);
                    this.colBuffer.position(bufferProp.startPoint * 4);
                    for (int i = 0, attributePropertiesSize = attributeProperties.size(); i < attributePropertiesSize; i++)
                    {
                        AttributeProperty prop = attributeProperties.get(i);
                        prop.buffer.position(bufferProp.startPoint * prop.attribute.count);
                    }
                }
                else
                {
                    this.modifyingSize = -1;
                    this.vertBuffer.position(this.size * 3);
                    this.colBuffer.position(this.size * 4);
                    for (AttributeProperty prop : attributeProperties)
                        prop.buffer.position(this.size * prop.attribute.count);
                }
            }

            this.modifying = o;
        }
    }

    public void endAdd()
    {
        if (this.adding == null)
            return;

        this.adding = null;

        if (this.justExpanded && this.initialized)
        {
            this.vertBuffer.flip();
            this.colBuffer.flip();

            this.vertBuffer.limit(this.vertBuffer.capacity());
            this.colBuffer.limit(this.colBuffer.capacity());
            this.window.vertexBufferDataDynamic(vertVBO, vertBuffer);
            this.window.vertexBufferDataDynamic(colVBO, colBuffer);

            for (AttributeProperty prop : attributeProperties)
            {
                Buffer b = prop.buffer;
                b.flip();
                b.limit(b.capacity());

                this.window.vertexBufferDataDynamic(prop.vboId, b);
            }

            this.justExpanded = false;
            this.initSize = this.size;
        }
        else if (this.initSize < this.size && this.initialized)
        {
            this.vertBuffer.position(this.initSize * 3);
            this.colBuffer.position(this.initSize * 4);
            for (AttributeProperty prop : attributeProperties)
                prop.buffer.position(this.initSize * prop.attribute.count);

            this.vertBuffer.limit(this.size * 3);
            this.colBuffer.limit(this.size * 4);
            for (AttributeProperty prop : attributeProperties)
                prop.buffer.limit(this.size * prop.attribute.count);

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertVBO);
            GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, (long) Float.BYTES * this.initSize * 3, this.vertBuffer);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colVBO);
            GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, (long) Float.BYTES * this.initSize * 4, this.colBuffer);
            for (AttributeProperty prop : attributeProperties)
            {
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, prop.vboId);
                GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, (long) Float.BYTES * this.initSize * prop.attribute.count, prop.buffer);
            }

            this.initSize = this.size;
        }
    }

    public void delete(IBatchRenderableObject o)
    {
        if (!this.bufferToId.containsKey(o))
            return;

        if (this.modifying == o)
            this.modifying = null;

        if (this.adding != null)
            this.endAdd();

        int bufferId = this.bufferToId.removeInt(o);
        BufferProperty bufferProp = this.bufferProperties.get(bufferId);
        int pos = bufferProp.startPoint;
        int size = bufferProp.size;
        bufferProp.object = null; // Mark as deleted

        this.vertBuffer.position(pos * 3);
        this.colBuffer.position(pos * 4);
        for (AttributeProperty prop : attributeProperties)
            prop.buffer.position(pos * prop.attribute.count);

        for (int i = pos; i < pos + size; i++)
        {
            this.vertBuffer.put(i * 3, 0f);
            this.vertBuffer.put(i * 3 + 1, 0f);
            this.vertBuffer.put(i * 3 + 2, 0f);

            this.colBuffer.put(i * 4, 0f);
            this.colBuffer.put(i * 4 + 1, 0f);
            this.colBuffer.put(i * 4 + 2, 0f);
            this.colBuffer.put(i * 4 + 3, 0f);

            for (AttributeProperty prop : attributeProperties)
                for (int k = 0; k < prop.attribute.count; k++)
                    prop.buffer.put(i * prop.attribute.count + k, 0);
        }


        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertVBO);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, (long) Float.BYTES * pos * 3, new float[3 * size]);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colVBO);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, (long) Float.BYTES * pos * 4, new float[4 * size]);
        for (AttributeProperty prop : attributeProperties)
        {
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, prop.vboId);
            GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, (long) Float.BYTES * pos * prop.attribute.count, new float[prop.attribute.count * size]);
        }

        this.vertBuffer.rewind();
        this.colBuffer.rewind();
        for (AttributeProperty attributeProperty : attributeProperties)
            attributeProperty.buffer.rewind();
    }

    public void migrate(IBatchRenderableObject o)
    {
        int bufferId = this.bufferToId.getInt(o);
        BufferProperty bufferProp = this.bufferProperties.get(bufferId);

        if (this.capacity <= this.size + bufferProp.size)
            this.expand();

        int pos = bufferProp.startPoint;
        int size = bufferProp.size;

        this.vertBuffer.position(pos * 3);
        this.colBuffer.position(pos * 4);
        for (AttributeProperty prop : attributeProperties)
            prop.buffer.position(pos * prop.attribute.count);

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

            for (AttributeProperty prop : attributeProperties)
                for (int f = 0; f < prop.attribute.count; f++)
                    this.moveFloat(prop.buffer, prop.attribute.count, f, i);

            this.size++;
        }

        bufferProp.startPoint = initSize;
        this.modifyingSize = -1;

        this.vertBuffer.rewind();
        this.colBuffer.rewind();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertVBO);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, (long) Float.BYTES * pos * 3, new float[3 * size]);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colVBO);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, (long) Float.BYTES * pos * 4, new float[4 * size]);
        for (AttributeProperty prop : attributeProperties)
        {
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, prop.vboId);
            GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, (long) Float.BYTES * pos * prop.attribute.count, new float[prop.attribute.count * size]);
        }
    }

    public void addAttribute(ShaderGroup.Attribute attribute)
    {
        int attributeId = this.attributeProperties.size();
        this.attributeToId.put(attribute, attributeId);
        FloatBuffer buffer = BufferUtils.createFloatBuffer(capacity * attribute.count);
        float[] floatArray = new float[attribute.count];
        this.attributeProperties.add(new AttributeProperty(-1, buffer, floatArray, attribute));
    }

    public void moveFloat(FloatBuffer b, int mul, int off, int rem)
    {
        b.put(this.size * mul + off,  b.get(rem * mul + off));
        b.put(rem * mul + off, 0f);
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

    public void stage()
    {
        this.initialized = true;

        this.vertVBO = this.window.createVBO();
        this.colVBO = this.window.createVBO();

        for (AttributeProperty prop : attributeProperties)
        {
            prop.vboId = this.window.createVBO();
            prop.buffer.flip();
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

            for (AttributeProperty prop : attributeProperties)
            {
                prop.buffer.limit(prop.buffer.capacity());
                this.window.vertexBufferDataDynamic(prop.vboId, prop.buffer);
            }
        }
        else
        {
            this.window.vertexBufferData(vertVBO, vertBuffer);
            this.window.vertexBufferData(colVBO, colBuffer);

            for (AttributeProperty prop : attributeProperties)
                this.window.vertexBufferData(prop.vboId, prop.buffer);
        }
    }

    public void batchDraw()
    {
        this.endModification();
        this.endAdd();

        if (!this.initialized)
            this.stage();

        this.modifying = null;
        if (!this.hidden)
        {
            this.window.setColor(255, 255, 255, 255, this.colorGlow);

            this.shader.setVertexBuffer(vertVBO);
            this.shader.setColorBuffer(colVBO);

            for (ShaderGroup.Attribute a : this.shader.attributes)
            {
                int attributeId = this.attributeToId.getInt(a);
                AttributeProperty prop = this.attributeProperties.get(attributeId);
                this.shader.setCustomBuffer(a, prop.vboId, a.count);
            }

            this.shader.drawVBO(this.size);
        }
    }

    // Property classes for parallel arrays
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

    public static class BufferProperty
    {
        private static final ObjectArrayFIFOQueue<BufferProperty> buffers = new ObjectArrayFIFOQueue<>();
        private static final ObjectArrayFIFOQueue<BufferProperty> recycledBuffers = new ObjectArrayFIFOQueue<>();
        private static final int MAX_BUFFERS = 10000;

        public int startPoint;
        public int size;
        public IBatchRenderableObject object;

        private BufferProperty()
        {
        }

        public void set(int startPoint, int size, IBatchRenderableObject object)
        {
            this.startPoint = startPoint;
            this.size = size;
            this.object = object;
        }

        public static BufferProperty newInstance(int startPoint, int size, IBatchRenderableObject o)
        {
            BufferProperty prop = recycledBuffers.isEmpty() ? new BufferProperty() : recycledBuffers.dequeue();
            prop.set(startPoint, size, o);
            if (buffers.size() < MAX_BUFFERS)
                buffers.enqueue(prop);
            return prop;
        }

        public static void recycle()
        {
            while (!buffers.isEmpty())
                recycledBuffers.enqueue(buffers.dequeue());
        }
    }
}
