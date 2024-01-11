package tanks.rendering;

import basewindow.BaseWindow;
import basewindow.OnlyBaseUniform;

public class ShaderBoostPanel extends RendererShader implements IObstacleSizeShader, IObstacleVertexCoordShader, IObstacleTimeShader, IGlowShader
{
    @OnlyBaseUniform
    public Uniform1i time;
    public Uniform1f obstacleSizeFrac;
    public Attribute1f vertexCoord;

    public ShaderBoostPanel(BaseWindow w)
    {
        super(w, "boost_panel");
    }

    @Override
    public void initialize() throws Exception
    {
        this.shaderBase.setUp("/shaders/main.vert", new String[]{"/shaders/main_boost_panels.vert"}, "/shaders/main.frag", null);
        this.shaderShadowMap.setUp("/shaders/shadow_map.vert", new String[]{"/shaders/main_obstacles.vert"}, "/shaders/shadow_map.frag", null);
    }

    @Override
    public void setSize(float size)
    {
        this.obstacleSizeFrac.set(size);
    }

    @Override
    public void setTime(int time)
    {
        this.time.set(time);
    }

    @Override
    public Attribute1f getVertexCoord()
    {
        return this.vertexCoord;
    }

    @Override
    public float getGlow()
    {
        return 1.0f;
    }
}
