package tanks.rendering;

import basewindow.BaseWindow;

public class ShaderMud extends RendererShader implements IObstacleSizeShader
{
    public Uniform1f obstacleSizeFrac;

    public ShaderMud(BaseWindow w)
    {
        super(w, "mud");
    }

    @Override
    public void initialize() throws Exception
    {
        this.shaderBase.setUp("/shaders/main.vert", new String[]{"/shaders/main_mud.vert"}, "/shaders/main.frag", null);
        this.shaderShadowMap.setUp("/shaders/shadow_map.vert", new String[]{"/shaders/main_mud.vert"}, "/shaders/shadow_map.frag", null);
    }

    @Override
    public void setSize(float size)
    {
        this.obstacleSizeFrac.set(size);
    }
}
