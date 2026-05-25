package tanks.rendering;

import basewindow.BaseWindow;
import basewindow.ShaderGroupShadowDraw;
import basewindow.StageExclusiveUniform;

public class ShaderFireworkExplosion extends ShaderGroupUI
{
    public Attribute2f offset;
    public Attribute1f maxAge;

    public Uniform1f time;
    public Uniform1f fireworkGlow;
    public Uniform3f gravity;
    public Uniform4f color;

    public ShaderFireworkExplosion()
    {
        super("firework");
    }

    @Override
    public void initialize() throws Exception
    {
        super.initialize();
        this.shader.setUp("/shaders/ui.vert", new String[]{"/shaders/ui_firework.vert"}, "/shaders/ui.frag", null);
    }
}
