package basewindow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * A shader group allows for shaders to share the same uniforms and attributes
 * across both normal and shadow map render mode shaders.
 */
public abstract class ShaderGroup
{
    public HashSet<Attribute> attributes = new HashSet<>();
    public HashMap<RenderPass, ShaderStage> stages = new HashMap<>();

    public BaseWindow window;

    public String name;
    protected int random = (int) (Math.random() * 100);

    public ShaderGroup(BaseWindow w, String name)
    {
        this.window = w;
        this.name = name;
    }

    public void initialize() throws Exception
    {
        int i = 0;
        for (ShaderStage st: stages.values())
        {
            st.shader.stageIndex = i;
            st.index = i;
            i++;
        }
    }

    public void addStage(ShaderStage st)
    {
        this.stages.put(st.renderPass, st);
    }

    public abstract static class Attribute
    {
        public int count;

        public ShaderProgram.Attribute[] passAttributes;

        public Attribute(int count)
        {
            this.count = count;
        }
    }

    public static class Attribute1f extends Attribute
    {
        public Attribute1f()
        {
            super(1);
        }
    }

    public static class Attribute2f extends Attribute
    {
        public Attribute2f()
        {
            super(2);
        }
    }

    public static class Attribute3f extends Attribute
    {
        public Attribute3f()
        {
            super(3);
        }
    }

    public static class Attribute4f extends Attribute
    {
        public Attribute4f()
        {
            super(4);
        }
    }

    public interface IGroupUniform
    {
        void setWindow(BaseWindow window);

        void bind(int stage);
    }

    public static abstract class GroupPrimitiveUniform<T, U extends ShaderProgram.IPrimitiveUniform<T>> implements IGroupUniform
    {
        protected U[] uniforms;

        protected BaseWindow window;

        public void set(T t)
        {
            this.uniforms[window.currentShaderStage.index].set(t);
        }

        public void setWindow(BaseWindow w)
        {
            this.window = w;
        }

        public void bind(int stage)
        {
            this.uniforms[stage].bind();
        }
    }

    public static abstract class GroupMatrixUniform<T extends ShaderProgram.IMatrixUniform>
    {
        protected T[] uniforms;

        protected BaseWindow window;

        public void set(float[] f, boolean transpose)
        {
            this.uniforms[window.currentShaderStage.index].set(f, transpose);
        }

        public void setWindow(BaseWindow w)
        {
            this.window = w;
        }

        public void bind(int stage)
        {
            this.uniforms[stage].bind();
        }
    }

    public static class Uniform1b extends GroupPrimitiveUniform<Boolean, ShaderProgram.Uniform1b> { }

    public static class Uniform1i extends GroupPrimitiveUniform<Integer, ShaderProgram.Uniform1i> { }

    public static class Uniform2i extends GroupPrimitiveUniform<int[], ShaderProgram.Uniform2i> { }

    public static class Uniform3i extends GroupPrimitiveUniform<int[], ShaderProgram.Uniform3i> { }

    public static class Uniform4i extends GroupPrimitiveUniform<int[], ShaderProgram.Uniform4i> { }

    public static class Uniform1f extends GroupPrimitiveUniform<Float, ShaderProgram.Uniform1f> { }

    public static class Uniform2f extends GroupPrimitiveUniform<float[], ShaderProgram.Uniform2f> { }

    public static class Uniform3f extends GroupPrimitiveUniform<float[], ShaderProgram.Uniform3f> { }

    public static class Uniform4f extends GroupPrimitiveUniform<float[], ShaderProgram.Uniform4f> { }

    public static class UniformMatrix2 extends GroupMatrixUniform<ShaderProgram.UniformMatrix2> { }

    public static class UniformMatrix3 extends GroupMatrixUniform<ShaderProgram.UniformMatrix3> { }

    public static class UniformMatrix4 extends GroupMatrixUniform<ShaderProgram.UniformMatrix4> { }

    public static class ShaderStage
    {
        public ShaderGroup group;
        public int index;
        public ShaderProgram shader;
        public RenderPass renderPass;

        public ShaderStage(ShaderGroup g, RenderPass pass, ShaderProgram shader)
        {
            this.group = g;
            this.renderPass = pass;
            this.shader = shader;
            this.shader.group = g;
        }
    }

    @Override
    public String toString()
    {
        return this.name + this.random;
    }
}
