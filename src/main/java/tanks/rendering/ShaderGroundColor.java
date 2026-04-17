package tanks.rendering;

import basewindow.BaseWindow;
import basewindow.ShaderGroupShadowDraw;
import basewindow.StageExclusiveUniform;

public class ShaderGroundColor extends RendererShader implements IObstacleSizeShader, IGroundColorShader
{
    @StageExclusiveUniform({ShaderGroupShadowDraw.draw_pass})
    public Uniform1f obstacleSizeFrac;
    public Attribute3f groundColor;

    public ShaderGroundColor(BaseWindow w)
    {
        super(w, "ground_color");
    }

    @Override
    public void initialize() throws Exception
    {
        super.initialize();
        this.shaderBase.setUp("/shaders/main.vert", new String[]{"/shaders/main_ground_color.vert"}, "/shaders/main.frag", null);
        this.shaderShadowMap.setUp("/shaders/shadow_map.vert", new String[]{"/shaders/main_ground_color.vert"}, "/shaders/shadow_map.frag", null);
    }

    @Override
    public void setSize(float size)
    {
        this.obstacleSizeFrac.set(size);
    }

    @Override
    public Attribute3f getGroundColor()
    {
        return groundColor;
    }
}
