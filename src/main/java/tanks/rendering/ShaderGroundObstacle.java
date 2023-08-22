package tanks.rendering;

import basewindow.BaseWindow;

/**
 * Default shader for the ground tiles under obstacles
 */
public class ShaderGroundObstacle extends RendererShader implements IObstacleSizeShader
{
    public Uniform1f obstacleSizeFrac;

    public ShaderGroundObstacle(BaseWindow w)
    {
        super(w, "ground_obstacles");
    }

    @Override
    public void initialize() throws Exception
    {
        this.shaderBase.setUp("/shaders/main.vert", new String[]{"/shaders/main_ground_obstacles.vert"}, "/shaders/main.frag", null);
        this.shaderShadowMap.setUp("/shaders/shadow_map.vert", new String[]{"/shaders/main_ground_obstacles.vert"}, "/shaders/shadow_map.frag", null);
    }

    @Override
    public void setSize(float size)
    {
        this.obstacleSizeFrac.set(size);
    }
}
