package basewindow;

import java.lang.reflect.Field;
import java.util.ArrayList;

public abstract class ShaderProgram
{
    public BaseShaderUtil util;
    public ArrayList<Attribute> attributes = new ArrayList<>();

    public ShaderProgram(BaseWindow w)
    {
        this.util = w.getShaderUtil(this);
    }

    public abstract void initialize() throws Exception;

    public void initializeUniforms()
    {

    }

    public void initializeAttributeParameters()
    {

    }

    public void bindAttributes() throws IllegalAccessException
    {
        for (Field f: this.getClass().getFields())
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

        public int dataType;
        public int count;

        public void setDataType(int type, int count)
        {
            this.dataType = type;
            this.count = count;
        }

        public abstract void bind();
    }

    public static abstract class Uniform
    {
        protected int flag;
        protected String name;
        protected int programID;

        public abstract void bind();
    }

    public interface IUniform {}

    public interface Uniform1b extends IUniform
    {
        void set(boolean b);

        boolean get();
    }

    public interface Uniform1i extends IUniform
    {
        void set(int i);

        int get();
    }

    public interface Uniform2i extends IUniform
    {
        void set(int i1, int i2);

        int[] get();
    }

    public interface Uniform3i extends IUniform
    {
        void set(int i1, int i2, int i3);

        int[] get();
    }

    public interface Uniform4i extends IUniform
    {
        void set(int i1, int i2, int i3, int i4);

        int[] get();
    }

    public interface Uniform1f extends IUniform
    {
        void set(float i);

        float get();
    }

    public interface Uniform2f extends IUniform
    {
        void set(float i1, float i2);

        float[] get();
    }

    public interface Uniform3f extends IUniform
    {
        void set(float i1, float i2, float i3);

        float[] get();
    }

    public interface Uniform4f extends IUniform
    {
        void set(float i1, float i2, float i3, float i4);

        float[] get();
    }

    public interface UniformMatrix2 extends IUniform
    {
        void set(float[] floats, boolean transpose);

        float[] getMatrix();

        boolean getTranspose();
    }

    public interface UniformMatrix3 extends IUniform
    {
        void set(float[] floats, boolean transpose);

        float[] getMatrix();

        boolean getTranspose();
    }

    public interface UniformMatrix4 extends IUniform
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
            Field[] fields = c.getFields();
            for (Field f : fields)
            {
                if (IUniform.class.isAssignableFrom(f.getType()))
                {
                    Uniform oldU = (Uniform) f.get(s);
                    Uniform newU = (Uniform) f.get(this);

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
