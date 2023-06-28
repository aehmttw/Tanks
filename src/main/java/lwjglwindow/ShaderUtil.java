package lwjglwindow;

import basewindow.BaseShaderUtil;
import basewindow.ShaderProgram;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.lwjgl.opengl.EXTGeometryShader4.*;
import static org.lwjgl.opengl.GL20.*;

public class ShaderUtil extends BaseShaderUtil
{
    public LWJGLWindow window;
    public ShaderProgram program;
    public int programID;

    public ArrayList<Integer> enabledAttributes = new ArrayList<>();

    public ShaderUtil(LWJGLWindow w, ShaderProgram s)
    {
        this.window = w;
        this.program = s;
    }

    @Override
    public void setUp(String vert, String[] vertHeaders, String frag, String[] fragHeaders) throws Exception
    {
        this.createProgram(vert, vertHeaders, null, null, frag, fragHeaders);
    }

    @Override
    public void setUp(String vert, String[] vertHeaders, String geom, String[] geomHeaders, String frag, String[] fragHeaders) throws Exception
    {
        this.createProgram(vert, vertHeaders, geom, geomHeaders, frag, fragHeaders);
    }

    public void createProgram(String vert, String[] vertHeaders, String geom, String[] geomHeaders, String frag, String[] fragHeaders) throws Exception
    {
        this.programID = glCreateProgram();

        int vshader = this.createShader(vert, vertHeaders, GL_VERTEX_SHADER);
        int fshader = this.createShader(frag, fragHeaders, GL_FRAGMENT_SHADER);

        glAttachShader(this.programID, vshader);
        glAttachShader(this.programID, fshader);

        if (geom != null)
        {
            int gshader = this.createShader(geom, geomHeaders, GL_GEOMETRY_SHADER_EXT);
            glAttachShader(this.programID, gshader);

            //int[] max = new int[1];
            //glGetIntegerv(GL_MAX_GEOMETRY_OUTPUT_VERTICES_EXT, max);
            glProgramParameteriEXT(this.programID, GL_GEOMETRY_VERTICES_OUT_EXT, 3);
        }

        glLinkProgram(this.programID);
        this.program.bindAttributes();
        this.program.initializeAttributeParameters();

        int linked = glGetProgrami(this.programID, GL_LINK_STATUS);
        String programLog = glGetProgramInfoLog(this.programID);

        if (programLog.trim().length() > 0)
            System.err.println(programLog);

        if (linked == 0)
            throw new AssertionError("Could not link program");

        this.setUpUniforms();
    }

    protected int createShader(String filename, int shaderType) throws Exception
    {
        return createShader(filename, null, shaderType);
    }

    protected int createShader(String filename, String[] headers, int shaderType) throws Exception
    {
        int shader = 0;
        try
        {
            shader = ARBShaderObjects.glCreateShaderObjectARB(shaderType);

            if (shader == 0)
                return 0;

            StringBuilder header = new StringBuilder();
            if (headers != null)
            {
                for (String h: headers)
                {
                    header.append(this.window.readFileAsString(h));
                }
            }

            ARBShaderObjects.glShaderSourceARB(shader, header + this.window.readFileAsString(filename));
            ARBShaderObjects.glCompileShaderARB(shader);

            if (ARBShaderObjects.glGetObjectParameteriARB(shader, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) == GL11.GL_FALSE)
                throw new RuntimeException("Error creating shader " + filename + ": " + getLogInfo(shader));

            return shader;
        }
        catch (Exception exc)
        {
            ARBShaderObjects.glDeleteObjectARB(shader);
            throw exc;
        }
    }

    @Override
    public void setUpUniforms() throws InstantiationException, IllegalAccessException
    {
        glUseProgram(programID);

        for (Field f: program.getClass().getFields())
        {
            if (ShaderProgram.IUniform.class.isAssignableFrom(f.getType()))
            {
                Class[] classes = this.getClass().getClasses();

                for (Class c: classes)
                {
                    if (f.getType().isAssignableFrom(c))
                    {
                        LWJGLUniform u = (LWJGLUniform) c.newInstance();
                        u.name = f.getName();
                        u.programID = this.programID;
                        u.bind();
                        f.set(program, u);
                    }
                }
            }
        }

        this.program.initializeUniforms();
        glUseProgram(0);
    }

    @Override
    public void set()
    {
        GL20.glUseProgram(this.programID);
    }

    protected static String getLogInfo(int obj)
    {
        return ARBShaderObjects.glGetInfoLogARB(obj, ARBShaderObjects.glGetObjectParameteriARB(obj, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB));
    }

    @Override
    public ShaderProgram.Attribute getAttribute()
    {
        return new LWJGLAttribute();
    }

    public class LWJGLAttribute extends ShaderProgram.Attribute
    {
        public void bind()
        {
            this.id = GL20.glGetAttribLocation(programID, name);
            GL20.glBindAttribLocation(programID, id, name);
        }
    }

    public void setVertexBuffer(int id)
    {
        if (id <= 0)
            return;

        GL11.glEnableClientState(GL_VERTEX_ARRAY);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, id);
        GL11.glVertexPointer(3, GL_FLOAT, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public void setColorBuffer(int id)
    {
        if (id <= 0)
            return;

        GL11.glEnableClientState(GL_COLOR_ARRAY);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, id);
        GL11.glColorPointer(4, GL_FLOAT, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public void setTexCoordBuffer(int id)
    {
        if (id <= 0)
            return;

        GL11.glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, id);
        GL11.glTexCoordPointer(2, GL_FLOAT, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public void setNormalBuffer(int id)
    {
        if (id <= 0)
            return;

        GL11.glEnableClientState(GL_NORMAL_ARRAY);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, id);
        GL11.glNormalPointer(GL_FLOAT, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public void setCustomBuffer(ShaderProgram.Attribute attribute, int bufferID, int size, int type)
    {
        GL15.glBindBuffer(GL_ARRAY_BUFFER, bufferID);
        GL20.glEnableVertexAttribArray(attribute.id);
        GL20.glVertexAttribPointer(attribute.id, size, type, false, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public void drawVBO(int numberIndices)
    {
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, numberIndices);

        GL11.glDisableClientState(GL_NORMAL_ARRAY);
        GL11.glDisableClientState(GL_TEXTURE_COORD_ARRAY);
        GL11.glDisableClientState(GL_VERTEX_ARRAY);
        GL11.glDisableClientState(GL_COLOR_ARRAY);

        for (int i: this.enabledAttributes)
        {
            GL20.glDisableVertexAttribArray(i);
        }

        this.enabledAttributes.clear();
    }

    public static abstract class LWJGLUniform extends ShaderProgram.Uniform
    {
        protected int flag;
        protected String name;
        protected int programID;

        public void bind()
        {
            this.flag = GL20.glGetUniformLocation(programID, name);
        }
    }

    public static class LWJGLUniform1b extends LWJGLUniform implements ShaderProgram.Uniform1b
    {
        private boolean value = false;

        public void set(boolean b)
        {
            value = b;
            GL20.glUniform1i(flag, b ? 1 : 0);
        }

        public boolean get()
        {
            return value;
        }
    }

    public static class LWJGLUniform1i extends LWJGLUniform implements ShaderProgram.Uniform1i
    {
        private int value = 0;

        public void set(int i)
        {
            value = i;
            GL20.glUniform1i(flag, i);
        }

        public int get()
        {
            return value;
        }
    }

    public static class LWJGLUniform2i extends LWJGLUniform implements ShaderProgram.Uniform2i
    {
        private final int[] value = new int[2];

        public void set(int i1, int i2)
        {
            value[0] = i1;
            value[1] = i2;
            GL20.glUniform2i(flag, i1, i2);
        }

        public int[] get()
        {
            return value;
        }
    }

    public static class LWJGLUniform3i extends LWJGLUniform implements ShaderProgram.Uniform3i
    {
        private final int[] value = new int[3];

        public void set(int i1, int i2, int i3)
        {
            value[0] = i1;
            value[1] = i2;
            value[2] = i3;
            GL20.glUniform3i(flag, i1, i2, i3);
        }

        public int[] get()
        {
            return value;
        }
    }

    public static class LWJGLUniform4i extends LWJGLUniform implements ShaderProgram.Uniform4i
    {
        private final int[] value = new int[4];

        public void set(int i1, int i2, int i3, int i4)
        {
            value[0] = i1;
            value[1] = i2;
            value[2] = i3;
            value[3] = i4;
            GL20.glUniform4i(flag, i1, i2, i3, i4);
        }

        public int[] get()
        {
            return value;
        }
    }

    public static class LWJGLUniform1f extends LWJGLUniform implements ShaderProgram.Uniform1f
    {
        private float value;

        public void set(float i)
        {
            this.value = i;
            GL20.glUniform1f(flag, i);
        }

        public float get()
        {
            return value;
        }
    }

    public static class LWJGLUniform2f extends LWJGLUniform implements ShaderProgram.Uniform2f
    {
        private final float[] value = new float[2];

        public void set(float i1, float i2)
        {
            value[0] = i1;
            value[1] = i2;
            GL20.glUniform2f(flag, i1, i2);
        }

        public float[] get()
        {
            return value;
        }
    }

    public static class LWJGLUniform3f extends LWJGLUniform implements ShaderProgram.Uniform3f
    {
        private final float[] value = new float[3];

        public void set(float i1, float i2, float i3)
        {
            value[0] = i1;
            value[1] = i2;
            value[2] = i3;
            GL20.glUniform3f(flag, i1, i2, i3);
        }

        public float[] get()
        {
            return value;
        }
    }

    public static class LWJGLUniform4f extends LWJGLUniform implements ShaderProgram.Uniform4f
    {
        private final float[] value = new float[4];

        public void set(float i1, float i2, float i3, float i4)
        {
            value[0] = i1;
            value[1] = i2;
            value[2] = i3;
            value[3] = i4;
            GL20.glUniform4f(flag, i1, i2, i3, i4);
        }

        public float[] get()
        {
            return value;
        }
    }

    public static class LWJGLUniformMatrix2 extends LWJGLUniform implements ShaderProgram.UniformMatrix2
    {
        private float[] matrix = new float[0];
        private boolean transpose;

        public void set(float[] floats, boolean transpose)
        {
            this.matrix = floats;
            this.transpose = transpose;
            GL20.glUniformMatrix2fv(flag, transpose, floats);
        }

        public float[] getMatrix()
        {
            return matrix;
        }

        public boolean getTranspose()
        {
            return this.transpose;
        }
    }

    public static class LWJGLUniformMatrix3 extends LWJGLUniform implements ShaderProgram.UniformMatrix3
    {
        private float[] matrix = new float[0];
        private boolean transpose;

        public void set(float[] floats, boolean transpose)
        {
            this.matrix = floats;
            this.transpose = transpose;
            GL20.glUniformMatrix3fv(flag, transpose, floats);
        }

        public float[] getMatrix()
        {
            return matrix;
        }

        public boolean getTranspose()
        {
            return this.transpose;
        }
    }

    public static class LWJGLUniformMatrix4 extends LWJGLUniform implements ShaderProgram.UniformMatrix4
    {
        private float[] matrix = new float[0];
        private boolean transpose;

        public void set(float[] floats, boolean transpose)
        {
            this.matrix = floats;
            this.transpose = transpose;
            GL20.glUniformMatrix4fv(flag, transpose, floats);
        }

        public float[] getMatrix()
        {
            return matrix;
        }

        public boolean getTranspose()
        {
            return this.transpose;
        }
    }
}
