package tanks.rendering;

import basewindow.BaseWindow;

public class ShaderGroundOutOfBounds extends RendererShader implements IObstacleSizeShader
{
    public Uniform1f obstacleSizeFrac;

    public ShaderGroundOutOfBounds(BaseWindow w)
    {
        super(w, "outside");
    }

    @Override
    public void initialize() throws Exception
    {
        this.shaderBase.setUp("/shaders/main.vert", new String[]{"/shaders/main_out_of_bounds.vert"}, "/shaders/main.frag", null);
        this.shaderShadowMap.setUp("/shaders/shadow_map.vert", new String[]{"/shaders/main_out_of_bounds.vert"}, "/shaders/shadow_map.frag", null);
    }

    @Override
    public void setSize(float size)
    {
        this.obstacleSizeFrac.set(size);
    }
}
