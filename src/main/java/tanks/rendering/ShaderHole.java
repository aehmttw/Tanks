package tanks.rendering;

import basewindow.BaseWindow;

public class ShaderHole extends RendererShader implements IObstacleSizeShader, IGroundHeightShader
{
    public Uniform1f obstacleSizeFrac;
    public Attribute1f groundHeight;

    public ShaderHole(BaseWindow w)
    {
        super(w, "holes");
    }

    @Override
    public void initialize() throws Exception
    {
        this.shaderBase.setUp("/shaders/main.vert", new String[]{"/shaders/main_holes.vert"}, "/shaders/main.frag", null);
        this.shaderShadowMap.setUp("/shaders/shadow_map.vert", new String[]{"/shaders/main_holes.vert"}, "/shaders/shadow_map.frag", null);
    }

    @Override
    public Attribute1f getGroundHeight()
    {
        return this.groundHeight;
    }

    @Override
    public void setSize(float size)
    {
        this.obstacleSizeFrac.set(size);
    }
}
