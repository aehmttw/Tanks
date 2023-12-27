package tanks.rendering;

import basewindow.BaseWindow;

public class ShaderGroundIntro extends RendererShader implements IGroundHeightShader, IObstacleSizeShader, IObstacleVertexCoordShader
{
    public Attribute1f groundHeight;
    public Uniform1f obstacleSizeFrac;
    public Attribute1f vertexCoord;
    public Uniform1b d3;

    public ShaderGroundIntro(BaseWindow w)
    {
        super(w, "intro");
    }

    @Override
    public void initialize() throws Exception
    {
        this.shaderBase.setUp("/shaders/main.vert", new String[]{"/shaders/main_ground_intro.vert"}, "/shaders/main.frag", null);
        this.shaderShadowMap.setUp("/shaders/shadow_map.vert", new String[]{"/shaders/main_ground_intro.vert"}, "/shaders/shadow_map.frag", null);
    }

    @Override
    public void setSize(float size)
    {
        this.obstacleSizeFrac.set(size);
    }

    @Override
    public Attribute1f getVertexCoord()
    {
        return vertexCoord;
    }

    @Override
    public Attribute1f getGroundHeight()
    {
        return groundHeight;
    }
}
