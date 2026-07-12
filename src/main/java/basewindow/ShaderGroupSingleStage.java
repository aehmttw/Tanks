package basewindow;

public abstract class ShaderGroupSingleStage extends ShaderGroup
{
    public ShaderProgram shader;

    public ShaderGroupSingleStage(BaseWindow w, ShaderProgram s, String name, RenderPass p)
    {
        super(w, name);
        this.shader = s;
        this.shader.group = this;
        this.addStage(new ShaderStage(this, p, this.shader));
    }

    public ShaderGroupSingleStage(BaseWindow w, String name, RenderPass p)
    {
        this(w, new ShaderSingleStage(w), name, p);
    }

    public static class ShaderSingleStage extends ShaderProgram implements IBaseShader
    {
        public ShaderSingleStage(BaseWindow window)
        {
            super(window);
        }

        @Override
        public String toString()
        {
            return this.group.name;
        }

        @Override
        public void renderVBO(int vertexBufferID, int colorBufferID, int texBufferID, int normalBufferID, int numberIndices)
        {
            this.window.vboRenderer.setVertexBuffer(vertexBufferID);
            this.window.vboRenderer.setColorBuffer(colorBufferID);
            this.window.vboRenderer.setTexCoordBuffer(texBufferID);
            this.window.vboRenderer.setNormalBuffer(normalBufferID);
            this.window.vboRenderer.drawVBO(numberIndices);
        }
    }
}
