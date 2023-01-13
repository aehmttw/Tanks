package lwjglwindow;

import basewindow.BaseShapeBatchRenderer;
import basewindow.IBatchRenderableObject;
import basewindow.transformation.Rotation;
import basewindow.transformation.Scale;
import basewindow.transformation.Translation;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Objects;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glDepthMask;

public class VBOShapeBatchRenderer extends BaseShapeBatchRenderer
{
    public int vertVBO = -1;
    public int colVBO = -1;

    public boolean initialized = false;

    public HashMap<IBatchRenderableObject, PointQueue> lastPoints = new HashMap<>();
    public IntHashTable lastPointsPosHash = new IntHashTable();
    public IntHashTable lastPointsColHash = new IntHashTable();
    public IntHashTable lastPointsSize = new IntHashTable();
    public int lastPointsCount = 0;

    public HashMap<IBatchRenderableObject, PointQueue> points = new HashMap<>();
    public IntHashTable pointsPosHash = new IntHashTable();
    public IntHashTable pointsColHash = new IntHashTable();
    public IntHashTable pointsSize = new IntHashTable();
    public int pointsCount = 0;

    public IntHashTable objBufferPos = new IntHashTable();

    public static Point[] recyclePoints = new Point[10000];
    public static int recyclePointsFirst = 0;
    public static int recyclePointsLast = 0;
    public static int recyclePointsSize = 0;

    public LWJGLWindow window;

    public double colorR;
    public double colorG;
    public double colorB;
    public double colorA;
    public double colorGlow;

    public float currentR;
    public float currentG;
    public float currentB;
    public float currentA;

    public boolean forceRedraw = false;

    public boolean batching = false;
    public boolean depth = false;
    public boolean glow = false;
    public boolean depthMask = false;

    public VBOShapeBatchRenderer(LWJGLWindow window)
    {
        this.window = window;
        this.initializeVBO();
    }

    public void begin(boolean depth)
    {
        this.begin(depth, false);
    }

    public void begin(boolean depth, boolean glow)
    {
        this.begin(depth, glow, !(glow));
    }

    public void begin(boolean depth, boolean glow, boolean depthMask)
    {
        this.batching = true;
        this.depth = depth;
        this.glow = glow;
        this.depthMask = depthMask;

        this.reset();
    }

    public void end()
    {
        this.stage();

        this.draw();
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

    public void forceRedraw()
    {
        this.forceRedraw = true;
    }

    public static void recyclePoint(Point p)
    {
        recyclePoints[recyclePointsLast] = p;
        recyclePointsLast++;
        recyclePointsSize++;

        if (recyclePointsLast >= recyclePoints.length)
            recyclePointsLast = 0;

        if (recyclePointsSize == recyclePoints.length)
        {
            Point[] rp = new Point[recyclePoints.length * 2];
            for (int i = 0; i < recyclePoints.length; i++)
            {
                rp[i] = recyclePoints[i];
                recyclePoints[i] = null;
            }

            recyclePoints = rp;
        }
    }

    public static Point getRecycledPoint()
    {
        Point p = recyclePoints[recyclePointsFirst];
        recyclePoints[recyclePointsFirst] = null;

        recyclePointsFirst++;
        recyclePointsSize--;

        if (recyclePointsFirst >= recyclePoints.length)
            recyclePointsFirst = 0;

        return p;
    }

    public static class Point
    {
        public float posX;
        public float posY;
        public float posZ;

        public float colR;
        public float colG;
        public float colB;
        public float colA;

        public int posHash;
        public int colHash;

        public boolean initialized = false;

        protected void initialize(float x, float y, float z, float r, float g, float b, float a)
        {
            this.posX = x;
            this.posY = y;
            this.posZ = z;
            this.colR = r;
            this.colG = g;
            this.colB = b;
            this.colA = a;

            this.posHash = f(Float.floatToIntBits(posX) + f(Float.floatToIntBits(posY) + f(Float.floatToIntBits(posZ))));
            this.colHash = f(Float.floatToIntBits(colR) + f(Float.floatToIntBits(colG) + f(Float.floatToIntBits(colB) + f(Float.floatToIntBits(colA)))));

            this.initialized = true;
        }

        public void free()
        {
            this.initialized = false;
            recyclePoint(this);
        }
    }

    public static class PointQueue
    {
        public PointQueueNode start;
        public PointQueueNode end;

        public static PointQueue recycleNodes = new PointQueue();

        public static class PointQueueNode
        {
            public Point point;
            public PointQueueNode next;
            public boolean pointInitialized;

            public static PointQueueNode newNode(Point p)
            {
                PointQueueNode n;

                if (recycleNodes.isEmpty())
                    n = new PointQueueNode();
                else
                {
                    n = recycleNodes.popNode();

                    if (n.pointInitialized)
                        n.point.free();
                }

                n.point = p;
                n.pointInitialized = true;

                return n;
            }
        }

        public boolean isEmpty()
        {
            return start == null;
        }

        public void push(Point p)
        {
            PointQueueNode n = PointQueueNode.newNode(p);
            this.pushNode(n);
        }

        public void pushNode(PointQueueNode n)
        {
            if (start == null)
            {
                start = n;
                end = n;
            }
            else
            {
                end.next = n;
                end = n;
            }
        }

        public PointQueueNode popNode()
        {
            PointQueueNode n = start;

            if (start == end)
            {
                start = null;
                end = null;
            }
            else
                start = start.next;

            return n;
        }

        public Point pop()
        {
            PointQueueNode n = popNode();

            recycleNodes.pushNode(n);
            Point p = n.point;
            n.point = null;
            n.pointInitialized = false;
            return p;
        }
    }

    public static int f(int i)
    {
        return 1664525 * i + 1013904223;
    }

    public Point newPoint(float x, float y, float z, float r, float g, float b, float a)
    {
        Point p;

        if (recyclePointsSize <= 0)
            p = new Point();
        else
            p = getRecycledPoint();

        p.initialize(x + offX, y + offY, z + offZ, r, g, b, a);
        return p;
    }

    public Point newPoint(float x, float y, float z)
    {
        return newPoint(x, y, z, currentR, currentG, currentB, currentA);
    }

    public void setColor(double r, double g, double b, double a, double glow)
    {
        this.colorR = r / 255;
        this.colorG = g / 255;
        this.colorB = b / 255;
        this.colorA = a / 255;
        this.colorGlow = glow;
    }

    @Override
    public void free()
    {
        this.window.freeVBO(this.colVBO);
        this.window.freeVBO(this.vertVBO);
    }

    public void setWorkingColor(float r, float g, float b, float a, float glow)
    {
        this.currentR = r;
        this.currentG = g;
        this.currentB = b;
        this.currentA = a + ((int)(glow * 256) * 2);
    }

    public void addPoint(IBatchRenderableObject o, float x, float y, float z)
    {
        pointsCount++;

        PointQueue p = this.points.get(o);

        if (p == null)
        {
            p = new PointQueue();
            this.points.put(o, p);
            this.pointsPosHash.put(o, 0);
            this.pointsColHash.put(o, 0);
            this.pointsSize.put(o, 0);
        }

        Point pt = newPoint(x, y, z);
        p.push(pt);

        this.pointsPosHash.put(o, f(this.pointsPosHash.get(o) + pt.posHash));
        this.pointsColHash.put(o, f(this.pointsColHash.get(o) + pt.colHash));

        this.pointsSize.put(o, this.pointsSize.get(o) + 1);
    }

    @Override
    public void fillOval(IBatchRenderableObject o, double x, double y, double sX, double sY)
    {
        if (o.wasRedrawn())
            return;

        if (this.window.shadowsEnabled && !this.window.drawingShadow)
            return;

        x += sX / 2;
        y += sY / 2;

        int sides = Math.max(4, (int) (sX + sY) / 4 + 5);

        this.setWorkingColor((float) this.colorR, (float) this.colorG, (float) this.colorB, (float) this.colorA, (float) this.colorGlow);
        for (float i = 0; i < Math.PI * 2; i += Math.PI * 2 / sides)
            this.addPoint(o, (float) (x + Math.cos(i) * sX / 2), (float) (y + Math.sin(i) * sY / 2), 0);
        this.addPoint(o, (float) (x + sX / 2), (float) y, 0);
    }

    @Override
    public void fillRect(IBatchRenderableObject o, double x, double y, double sX, double sY)
    {
        if (o.wasRedrawn())
            return;

        if (this.window.shadowsEnabled && !this.window.drawingShadow)
            return;

        float x0 = (float) x;
        float y0 = (float) y;

        float x1 = (float) (x + sX);
        float y1 = (float) (y + sY);

        float r1 = (float) this.colorR;
        float g1 = (float) this.colorG;
        float b1 = (float) this.colorB;
        float a = (float) this.colorA;
        float g = (float) this.colorGlow;

        this.setWorkingColor(r1, g1, b1, a, g);
        this.addPoint(o, x1, y0, 0);
        this.addPoint(o, x0, y0, 0);
        this.addPoint(o, x0, y1, 0);

        this.addPoint(o, x1, y0, 0);
        this.addPoint(o, x1, y1, 0);
        this.addPoint(o, x0, y1, 0);
    }

    public void fillBox(IBatchRenderableObject o, double x, double y, double z, double sX, double sY, double sZ, byte options)
    {
        if (o.wasRedrawn())
            return;

        if (this.window.shadowsEnabled && !this.window.drawingShadow)
            return;

        float x0 = (float) x;
        float y0 = (float) y;
        float z0 = (float) z;

        float x1 = (float) (x + sX);
        float y1 = (float) (y + sY);
        float z1 = (float) (z + sZ);

        float r1 = (float) this.colorR;
        float g1 = (float) this.colorG;
        float b1 = (float) this.colorB;
        float a = (float) this.colorA;
        float g = (float) this.colorGlow;

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
    }

    public void initializeVBO()
    {
        this.vertVBO = this.window.createVBO();
        this.colVBO = this.window.createVBO();
    }

    public void initializeBuffers()
    {
        this.initialized = true;

        FloatBuffer vert = BufferUtils.createFloatBuffer(pointsCount * 3);
        FloatBuffer col = BufferUtils.createFloatBuffer(pointsCount * 4);

        int i = 0;
        for (IBatchRenderableObject o: points.keySet())
        {
            PointQueue p = points.get(o);
            objBufferPos.put(o, i);

            PointQueue.PointQueueNode node = p.start;
            while (node != null)
            {
                i++;

                vert.put(node.point.posX);
                vert.put(node.point.posY);
                vert.put(node.point.posZ);

                col.put(node.point.colR);
                col.put(node.point.colG);
                col.put(node.point.colB);
                col.put(node.point.colA);

                if (node == p.end)
                    break;

                node = node.next;
            }
        }

        vert.flip();
        col.flip();

        this.window.vertexBufferDataDynamic(vertVBO, vert);
        this.window.vertexBufferDataDynamic(colVBO, col);
    }

    public void batchDraw()
    {
        this.window.setColor(255, 255, 255, 255);
        this.window.renderVBO(vertVBO, colVBO, 0, this.pointsCount);
    }

    public void reset()
    {
        if (this.window.shadowsEnabled && !this.window.drawingShadow)
            return;

        for (IBatchRenderableObject o: this.lastPoints.keySet())
        {
            if (o.wasRedrawn())
            {
                o.setRedrawn(false);
                continue;
            }

            PointQueue l = this.lastPoints.get(o);

            if (PointQueue.recycleNodes.end != null)
                PointQueue.recycleNodes.end.next = l.start;
            else
                PointQueue.recycleNodes.start = l.start;

            PointQueue.recycleNodes.end = l.end;

            l.start = null;
            l.end = null;
        }

        this.lastPoints.clear();
        this.lastPointsPosHash.clear();
        this.lastPointsColHash.clear();

        this.lastPointsCount = this.pointsCount;

        HashMap<IBatchRenderableObject, PointQueue> lastPointsC = this.lastPoints;
        IntHashTable lastPointsPosHashC = this.lastPointsPosHash;
        IntHashTable lastPointsColHashC = this.lastPointsColHash;
        IntHashTable lastPointsSizeC = this.lastPointsSize;

        this.lastPoints = this.points;
        this.lastPointsPosHash = this.pointsPosHash;
        this.lastPointsColHash = this.pointsColHash;
        this.lastPointsSize = this.pointsSize;

        this.points = lastPointsC;
        this.pointsPosHash = lastPointsPosHashC;
        this.pointsColHash = lastPointsColHashC;
        this.pointsSize = lastPointsSizeC;

        this.pointsCount = 0;

        for (IBatchRenderableObject o: this.lastPoints.keySet())
        {
            if (!o.colorChanged() && !o.positionChanged() && !this.forceRedraw)
            {
                this.points.put(o, this.lastPoints.get(o));
                this.pointsPosHash.put(o, this.lastPointsPosHash.get(o));
                this.pointsColHash.put(o, this.lastPointsColHash.get(o));

                int p = this.lastPointsSize.get(o);
                this.pointsSize.put(o, p);
                pointsCount += p;

                o.setRedrawn(true);
            }
        }

        this.forceRedraw = false;
    }

    public void stage()
    {
        if (this.window.shadowsEnabled && !this.window.drawingShadow)
            return;

        if (!initialized || !points.keySet().equals(lastPoints.keySet()))
        {
            this.initializeBuffers();
            return;
        }

        for (IBatchRenderableObject o: points.keySet())
        {
            if (!Objects.equals(this.lastPointsSize.get(o), this.pointsSize.get(o)))
            {
                this.initializeBuffers();
                return;
            }
        }

        for (IBatchRenderableObject o: points.keySet())
        {
            if (this.lastPointsPosHash.get(o) != this.pointsPosHash.get(o))
            {
                int index = objBufferPos.get(o) * 3;

                PointQueue p = points.get(o);
                FloatBuffer b = BufferUtils.createFloatBuffer(this.lastPointsSize.get(o) * 3);

                PointQueue.PointQueueNode node = p.start;
                while (true)
                {
                    Point pt = node.point;
                    b.put(pt.posX);
                    b.put(pt.posY);
                    b.put(pt.posZ);

                    if (node == p.end)
                        break;

                    node = node.next;
                }

                b.flip();

                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertVBO);
                GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, Float.BYTES * (long) index, b);
            }

            if (this.lastPointsColHash.get(o) != this.pointsColHash.get(o))
            {
                int index = objBufferPos.get(o) * 4;

                PointQueue p = points.get(o);
                FloatBuffer b = BufferUtils.createFloatBuffer(this.lastPointsSize.get(o) * 4);

                PointQueue.PointQueueNode node = p.start;
                while (true)
                {
                    Point pt = node.point;
                    b.put(pt.colR);
                    b.put(pt.colG);
                    b.put(pt.colB);
                    b.put(pt.colA);

                    if (node == p.end)
                        break;

                    node = node.next;
                }

                b.flip();

                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colVBO);
                GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, Float.BYTES * (long) index, b);
            }
        }
    }
}
