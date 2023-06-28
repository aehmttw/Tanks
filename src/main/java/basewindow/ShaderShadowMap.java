package basewindow;

import lwjglwindow.LWJGLWindow;

public class ShaderShadowMap extends ShaderProgram implements IBaseShader
{
    public Uniform1b texture;

    public ShaderShadowMap(LWJGLWindow window)
    {
        super(window);
    }

    @Override
    public void initialize() throws Exception
    {
        this.setUp("/shaders/shadow_map.vert", new String[]{"/shaders/main_default.vert"}, "/shaders/shadow_map.frag", null);
    }

    public void renderVBO(int vertexBufferID, int colorBufferID, int texBufferID, int normalBufferID, int numberIndices)
    {
        this.util.setVertexBuffer(vertexBufferID);
        this.util.setTexCoordBuffer(texBufferID);
        this.util.drawVBO(numberIndices);
    }
}
