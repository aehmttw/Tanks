package basewindow;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class ShaderProgram
{
    public ShaderGroup group;
    public BaseShaderUtil util;
    public ArrayList<Attribute> attributes = new ArrayList<>();
    public BaseWindow window;

    protected static HashMap<Class, Field[]> fieldsCache = new HashMap<>();

    protected static Field[] getFields(Class c)
    {
        Field[] cached = fieldsCache.get(c);
        if (cached == null)
        {
            cached = c.getFields();
            fieldsCache.put(c, cached);
        }

        return cached;
    }

    public ShaderProgram(BaseWindow w)
    {
        this.util = w.getShaderUtil(this);
        this.window = w;
    }

    public void initializeUniforms()
    {

    }

    public void initializeAttributeParameters()
    {

    }

    public void bindAttributes() throws IllegalAccessException, InstantiationException
    {
        for (Field f: getFields(this.getClass()))
        {
            if (Attribute.class.isAssignableFrom(f.getType()))
            {
                Attribute a = this.util.getAttribute();
                a.name = f.getName();
                a.bind();
                f.set(this, a);
                this.attributes.add(a);
            }
        }

        for (Field f: getFields(this.group.getClass()))
        {
            if (ShaderGroup.Attribute.class.isAssignableFrom(f.getType()))
            {
                ShaderGroup.Attribute a = (ShaderGroup.Attribute) f.get(this.group);
                if (a == null)
                    a = (ShaderGroup.Attribute) f.getType().newInstance();

                if (this == group.shaderShadowMap)
                {
                    Attribute a2 = this.util.getAttribute();
                    a.shadowMapAttribute = a2;

                    a2.name = f.getName();
                    f.set(this.group, a);

                    this.group.attributes.add(a);
                    a.shadowMapAttribute.bind();
                }
                else
                {
                    Attribute a1 = this.util.getAttribute();
                    a.normalAttribute = a1;

                    a1.name = f.getName();
                    f.set(this.group, a);

                    this.group.attributes.add(a);
                    a.normalAttribute.bind();
                }
            }
        }
    }

    public void setUp(String vert, String frag) throws Exception
    {
        this.setUp(vert, null, frag, null);
    }

    public void setUp(String vert, String[] vertHeaders, String frag, String[] fragHeaders) throws Exception
    {
        this.util.setUp(vert, vertHeaders, frag, fragHeaders);
    }

    public void setUp(String vert, String[] vertHeaders, String geom, String[] geomHeaders, String frag, String[] fragHeaders) throws Exception
    {
        this.util.setUp(vert, vertHeaders, geom, geomHeaders, frag, fragHeaders);
    }

    public void set()
    {
        this.util.set();
    }

    public abstract static class Attribute
    {
        public String name;
        public int id;

        public int count;

        public void setCount(int count)
        {
            this.count = count;
        }

        public abstract void bind();
    }

    public static abstract class Uniform implements IUniform
    {
        protected int flag;
        protected String name;
        protected int programID;
    }

    public interface IUniform
    {
        void bind();
    }

    public interface IPrimitiveUniform<T> extends IUniform
    {
        void set(T t);

        T get();
    }

    public interface IMatrixUniform extends IUniform
    {
        void set(float[] floats, boolean transpose);

        float[] getMatrix();

        boolean getTranspose();
    }

    public interface Uniform1b extends IPrimitiveUniform<Boolean>
    {
        void set(Boolean b);

        Boolean get();
    }

    public interface Uniform1i extends IPrimitiveUniform<Integer>
    {
        void set(Integer i);

        Integer get();
    }

    public interface Uniform2i extends IPrimitiveUniform<int[]>
    {
        void set(int i1, int i2);

        default void set(int[] ints)
        {
            set(ints[0], ints[1]);
        }

        int[] get();
    }

    public interface Uniform3i extends IPrimitiveUniform<int[]>
    {
        void set(int i1, int i2, int i3);

        default void set(int[] ints)
        {
            set(ints[0], ints[1], ints[2]);
        }

        int[] get();
    }

    public interface Uniform4i extends IPrimitiveUniform<int[]>
    {
        void set(int i1, int i2, int i3, int i4);

        default void set(int[] ints)
        {
            set(ints[0], ints[1], ints[2], ints[3]);
        }

        int[] get();
    }

    public interface Uniform1f extends IPrimitiveUniform<Float>
    {
        void set(Float i);

        Float get();
    }

    public interface Uniform2f extends IPrimitiveUniform<float[]>
    {
        void set(float i1, float i2);

        default void set(float[] floats)
        {
            set(floats[0], floats[1]);
        }

        float[] get();
    }

    public interface Uniform3f extends IPrimitiveUniform<float[]>
    {
        void set(float i1, float i2, float i3);

        default void set(float[] floats)
        {
            set(floats[0], floats[1], floats[2]);
        }

        float[] get();
    }

    public interface Uniform4f extends IPrimitiveUniform<float[]>
    {
        void set(float i1, float i2, float i3, float i4);

        default void set(float[] floats)
        {
            set(floats[0], floats[1], floats[2], floats[3]);
        }

        float[] get();
    }

    public interface UniformMatrix2 extends IMatrixUniform
    {
        void set(float[] floats, boolean transpose);

        float[] getMatrix();

        boolean getTranspose();
    }

    public interface UniformMatrix3 extends IMatrixUniform
    {
        void set(float[] floats, boolean transpose);

        float[] getMatrix();

        boolean getTranspose();
    }

    public interface UniformMatrix4 extends IMatrixUniform
    {
        void set(float[] floats, boolean transpose);

        float[] getMatrix();

        boolean getTranspose();
    }

    /**
     * You must be using this shader to do this!
     */
    public void copyUniformsFrom(ShaderProgram s, Class<? extends ShaderProgram> c)
    {
        try
        {
            Field[] fields = getFields(c);
            for (Field f : fields)
            {
                if (IUniform.class.isAssignableFrom(f.getType()))
                {
                    IUniform oldU = (IUniform) f.get(s);
                    IUniform newU = (IUniform) f.get(this);

                    if (oldU instanceof Uniform1b)
                        ((Uniform1b) newU).set(((Uniform1b) oldU).get());
                    else if (oldU instanceof Uniform1i)
                        ((Uniform1i) newU).set(((Uniform1i) oldU).get());
                    else if (oldU instanceof Uniform2i)
                    {
                        int[] i = ((Uniform2i) oldU).get();
                        ((Uniform2i) newU).set(i[0], i[1]);
                    }
                    else if (oldU instanceof Uniform3i)
                    {
                        int[] i = ((Uniform3i) oldU).get();
                        ((Uniform3i) newU).set(i[0], i[1], i[2]);
                    }
                    else if (oldU instanceof Uniform4i)
                    {
                        int[] i = ((Uniform4i) oldU).get();
                        ((Uniform4i) newU).set(i[0], i[1], i[2], i[3]);
                    }
                    else if (oldU instanceof Uniform1f)
                    {
                        ((Uniform1f) newU).set(((Uniform1f) oldU).get());
                    }
                    else if (oldU instanceof Uniform2f)
                    {
                        float[] i = ((Uniform2f) oldU).get();
                        ((Uniform2f) newU).set(i[0], i[1]);
                    }
                    else if (oldU instanceof Uniform3f)
                    {
                        float[] i = ((Uniform3f) oldU).get();
                        ((Uniform3f) newU).set(i[0], i[1], i[2]);
                    }
                    else if (oldU instanceof Uniform4f)
                    {
                        float[] i = ((Uniform4f) oldU).get();
                        ((Uniform4f) newU).set(i[0], i[1], i[2], i[3]);
                    }
                    else if (oldU instanceof UniformMatrix2)
                    {
                        float[] fl = ((UniformMatrix2) oldU).getMatrix();
                        boolean t = ((UniformMatrix2) oldU).getTranspose();
                        ((UniformMatrix2) newU).set(fl, t);
                    }
                    else if (oldU instanceof UniformMatrix3)
                    {
                        float[] fl = ((UniformMatrix3) oldU).getMatrix();
                        boolean t = ((UniformMatrix3) oldU).getTranspose();
                        ((UniformMatrix3) newU).set(fl, t);
                    }
                    else if (oldU instanceof UniformMatrix4)
                    {
                        float[] fl = ((UniformMatrix4) oldU).getMatrix();
                        boolean t = ((UniformMatrix4) oldU).getTranspose();
                        ((UniformMatrix4) newU).set(fl, t);
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
