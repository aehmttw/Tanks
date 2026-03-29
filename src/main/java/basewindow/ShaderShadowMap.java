package basewindow;

public class ShaderShadowMap extends ShaderProgram implements IBaseShader
{
    public ShaderShadowMap(BaseWindow window)
    {
        super(window);
    }

//    @Override
//    public void initialize() throws Exception
//    {
//        this.setUp("/shaders/shadow_map.vert", new String[]{"/shaders/main_default.vert"}, "/shaders/shadow_map.frag", null);
//    }

    public void renderVBO(int vertexBufferID, int colorBufferID, int texBufferID, int normalBufferID, int numberIndices)
    {
        this.window.vboRenderer.setVertexBuffer(vertexBufferID);
        this.window.vboRenderer.setTexCoordBuffer(texBufferID);
        this.window.vboRenderer.drawVBO(numberIndices);
    }

    @Override
    public String toString()
    {
        return this.group.name + "/shadowmap";
    }
}
