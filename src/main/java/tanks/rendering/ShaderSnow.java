package tanks.rendering;

import basewindow.BaseWindow;

public class ShaderSnow extends RendererShader implements IObstacleSizeShader, IGroundHeightShader, IGroundColorShader, IShrubHeightShader
{
    public Uniform1f shrubHeight;
    public Uniform1f obstacleSizeFrac;
    public Attribute1f groundHeight;
    public Attribute3f groundColor;

    public ShaderSnow(BaseWindow w)
    {
        super(w, "snow");
    }

    @Override
    public void initialize() throws Exception
    {
        this.shaderBase.setUp("/shaders/main.vert", new String[]{"/shaders/main_snow.vert"}, "/shaders/main.frag", null);
        this.shaderShadowMap.setUp("/shaders/shadow_map.vert", new String[]{"/shaders/main_snow.vert"}, "/shaders/shadow_map.frag", null);
    }

    @Override
    public void setSize(float size)
    {
        this.obstacleSizeFrac.set(size);
    }

    @Override
    public Attribute1f getGroundHeight()
    {
        return this.groundHeight;
    }

    @Override
    public Attribute3f getGroundColor()
    {
        return this.groundColor;
    }

    @Override
    public void setShrubHeight(float size)
    {
        this.shrubHeight.set(size);
    }
}
