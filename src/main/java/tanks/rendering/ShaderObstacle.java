package tanks.rendering;

import basewindow.BaseWindow;

public class ShaderObstacle extends RendererShader implements IObstacleSizeShader, IObstacleVertexCoordShader
{
    public Uniform1f obstacleSizeFrac;
    public Attribute1f vertexCoord;

    public ShaderObstacle(BaseWindow w)
    {
        super(w, "obstacles");
    }

    @Override
    public void initialize() throws Exception
    {
        this.shaderBase.setUp("/shaders/main.vert", new String[]{"/shaders/main_obstacles.vert"}, "/shaders/main.frag", null);
        this.shaderShadowMap.setUp("/shaders/shadow_map.vert", new String[]{"/shaders/main_obstacles.vert"}, "/shaders/shadow_map.frag", null);
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
}
