package tanks.gui;

import basewindow.BaseShaderUtil;
import basewindow.BaseWindow;
import basewindow.ShaderBase;
import basewindow.ShaderGroup;

public class ShaderObstacle extends ShaderGroup
{
    public Uniform1f obstacleSizeFrac;
    public Attribute1f vertexCoord;

    public ShaderObstacle(BaseWindow w)
    {
        super(w);
    }

    @Override
    public void initialize() throws Exception
    {
        this.shaderBase.setUp("/shaders/main.vert", new String[]{"/shaders/main_obstacles.vert"}, "/shaders/main.frag", null);
        this.shaderShadowMap.setUp("/shaders/shadow_map.vert", new String[]{"/shaders/main_obstacles.vert"}, "/shaders/shadow_map.frag", null);
    }
}
