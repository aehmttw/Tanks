package tanks.rendering;

import basewindow.BaseWindow;
import basewindow.OnlyBaseUniform;

@RendererDrawLayer(7)
public class ShaderIce extends RendererShader implements IObstacleSizeShader, IGroundHeightShader
{
    @OnlyBaseUniform
    public Uniform1f obstacleSizeFrac;
    public Attribute1f groundHeight;

    public ShaderIce(BaseWindow w)
    {
        super(w, "ice");
        this.depthMask = false;
    }

    @Override
    public void initialize() throws Exception
    {
        this.shaderBase.setUp("/shaders/main.vert", new String[]{"/shaders/main_ice.vert"}, "/shaders/main.frag", null);
        this.shaderShadowMap.setUp("/shaders/shadow_map.vert", new String[]{"/shaders/main_ice.vert"}, "/shaders/shadow_map.frag", null);
    }

    @Override
    public Attribute1f getGroundHeight()
    {
        return groundHeight;
    }

    @Override
    public void setSize(float size)
    {
        this.obstacleSizeFrac.set(size);
    }
}
