package tanks.rendering;

import basewindow.BaseWindow;
import basewindow.ShaderGroupShadowDraw;
import basewindow.StageExclusiveUniform;

public class ShaderFireworkExplosion extends RendererShader
{
    public Attribute2f offset;
    public Attribute1f maxAge;

    @StageExclusiveUniform({ShaderGroupShadowDraw.draw_pass})
    public Uniform1f time;

    @StageExclusiveUniform({ShaderGroupShadowDraw.draw_pass})
    public Uniform1f fireworkGlow;

    @StageExclusiveUniform({ShaderGroupShadowDraw.draw_pass})
    public Uniform3f gravity;

    @StageExclusiveUniform({ShaderGroupShadowDraw.draw_pass})
    public Uniform4f color;

    public ShaderFireworkExplosion(BaseWindow w)
    {
        super(w, "firework");
    }

    @Override
    public void initialize() throws Exception
    {
        super.initialize();
        this.shaderBase.setUp("/shaders/main.vert", new String[]{"/shaders/main_firework.vert"}, "/shaders/main.frag", null);
        this.shaderShadowMap.setUp("/shaders/shadow_map.vert", new String[]{"/shaders/main_firework.vert"}, "/shaders/shadow_map.frag", null);
    }
}
