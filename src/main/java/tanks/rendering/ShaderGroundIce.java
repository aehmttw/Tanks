package tanks.rendering;

import basewindow.BaseWindow;

public class ShaderGroundIce extends RendererShader implements IObstacleSizeShader
{
    public Uniform1f obstacleSizeFrac;

    public ShaderGroundIce(BaseWindow w)
    {
        super(w, "ground_ice");
    }

    @Override
    public void initialize() throws Exception
    {
        this.shaderBase.setUp("/shaders/main.vert", new String[]{"/shaders/main_ground_ice.vert"}, "/shaders/main.frag", null);
        this.shaderShadowMap.setUp("/shaders/shadow_map.vert", new String[]{"/shaders/main_ground_ice.vert"}, "/shaders/shadow_map.frag", null);
    }

    @Override
    public void setSize(float size)
    {
        this.obstacleSizeFrac.set(size);
    }
}
