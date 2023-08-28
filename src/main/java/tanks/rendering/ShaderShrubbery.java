package tanks.rendering;

import basewindow.BaseWindow;

public class ShaderShrubbery extends RendererShader implements IObstacleSizeShader, IObstacleVertexCoordShader, IShrubHeightShader
{
    public Uniform1f shrubHeight;
    public Uniform1f obstacleSizeFrac;
    public Attribute1f vertexCoord;

    public ShaderShrubbery(BaseWindow w)
    {
        super(w, "shrubbery");
    }

    @Override
    public void initialize() throws Exception
    {
        this.shaderBase.setUp("/shaders/main.vert", new String[]{"/shaders/main_shrubbery.vert"}, "/shaders/main.frag", null);
        this.shaderShadowMap.setUp("/shaders/shadow_map.vert", new String[]{"/shaders/main_shrubbery.vert"}, "/shaders/shadow_map.frag", null);
    }

    @Override
    public void setSize(float size)
    {
        this.obstacleSizeFrac.set(size);
    }

    @Override
    public Attribute1f getVertexCoord()
    {
        return this.vertexCoord;
    }

    @Override
    public void setShrubHeight(float size)
    {
        this.shrubHeight.set(size);
    }
}
