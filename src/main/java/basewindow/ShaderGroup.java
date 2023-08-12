package basewindow;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_COLOR_ARRAY;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;

/**
 * A shader group allows for shaders to share the same uniforms and attributes
 * across both normal and shadow map render mode shaders.
 */
public class ShaderGroup
{
    public ShaderBase shaderBase;
    public ShaderShadowMap shaderShadowMap;
    public ArrayList<Attribute> attributes = new ArrayList<>();
    public BaseWindow window;

    public ShaderGroup(BaseWindow w)
    {
        this.window = w;
        this.shaderBase = new ShaderBase(w);
        this.shaderBase.group = this;
        this.shaderShadowMap = new ShaderShadowMap(w);
        this.shaderShadowMap.group = this;
    }

    public void initialize() throws Exception
    {
        this.shaderShadowMap.setUp
                ("/shaders/shadow_map.vert", new String[]{"/shaders/main_default.vert"},
                "/shaders/shadow_map.frag", null);
        this.shaderBase.setUp
                ("/shaders/main.vert", new String[]{"/shaders/main_default.vert"},
                "/shaders/main.frag", new String[]{"/shaders/main_default.frag"});
    }

    public void setVertexBuffer(int id)
    {
        if (this.window.drawingShadow)
            this.shaderShadowMap.util.setVertexBuffer(id);
        else
            this.shaderBase.util.setVertexBuffer(id);
    }

    public void setColorBuffer(int id)
    {
        if (this.window.drawingShadow)
            this.shaderShadowMap.util.setColorBuffer(id);
        else
            this.shaderBase.util.setColorBuffer(id);
    }

    public void setTexCoordBuffer(int id)
    {
        if (this.window.drawingShadow)
            this.shaderShadowMap.util.setTexCoordBuffer(id);
        else
            this.shaderBase.util.setTexCoordBuffer(id);
    }

    public void setNormalBuffer(int id)
    {
        if (this.window.drawingShadow)
            this.shaderShadowMap.util.setNormalBuffer(id);
        else
            this.shaderBase.util.setNormalBuffer(id);
    }

    public void setCustomBuffer(Attribute attribute, int bufferID, int size)
    {
        if (this.window.drawingShadow)
            this.shaderShadowMap.util.setCustomBuffer(attribute.shadowMapAttribute, bufferID, size);
        else
            this.shaderBase.util.setCustomBuffer(attribute.normalAttribute, bufferID, size);
    }

    public void drawVBO(int numberIndices)
    {
        if (this.window.drawingShadow)
            this.shaderShadowMap.util.drawVBO(numberIndices);
        else
            this.shaderBase.util.drawVBO(numberIndices);
    }

    public void set()
    {
        if (this.window.drawingShadow)
            this.window.setShader(this.shaderShadowMap);
        else
            this.window.setShader(this.shaderBase);
    }

    public abstract static class Attribute
    {
        public String name;
        public int id;

        public int count;

        public ShaderProgram.Attribute normalAttribute;
        public ShaderProgram.Attribute shadowMapAttribute;

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
        void setMode(boolean shadow);

        void bind(boolean shadow);
    }

    public static abstract class GroupPrimitiveUniform<T, U extends ShaderProgram.IPrimitiveUniform<T>> implements IGroupUniform
    {
        protected U baseUniform;
        protected U shadowMapUniform;

        protected U current;

        public void setMode(boolean shadow)
        {
            U old = current;
            if (shadow)
                current = shadowMapUniform;
            else
                current = baseUniform;

            current.set(old.get());
        }

        public void set(T t)
        {
            current.set(t);
        }

        public void bind(boolean shadow)
        {
            if (shadow)
                shadowMapUniform.bind();
            else
                baseUniform.bind();
        }
    }

    public static abstract class GroupMatrixUniform<T extends ShaderProgram.IMatrixUniform>
    {
        protected T baseUniform;
        protected T shadowMapUniform;

        protected T current;

        public void setMode(boolean shadow)
        {
            T old = current;
            if (shadow)
                current = shadowMapUniform;
            else
                current = baseUniform;

            current.set(old.getMatrix(), old.getTranspose());
        }

        public void set(float[] f, boolean transpose)
        {
            current.set(f, transpose);
        }

        public void bind(boolean shadow)
        {
            if (shadow)
                shadowMapUniform.bind();
            else
                baseUniform.bind();
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

}
