package tanks.rendering;

import basewindow.BaseWindow;
import basewindow.ShaderGroupShadowDraw;
import basewindow.StageExclusiveUniform;

public class ShaderFireworkExplosionTrail extends ShaderGroupUI
{
    public Attribute1f timeOffset;
    public Attribute1f posOffset;
    public Attribute1f maxAge;

    public Uniform1f time;
    public Uniform3f gravity;
    public Uniform4f color;

    public ShaderFireworkExplosionTrail()
    {
        super("firework_trail");
    }

    @Override
    public void initialize() throws Exception
    {
        super.initialize();
        this.shader.setUp("/shaders/ui.vert", new String[]{"/shaders/ui_firework_trail.vert"}, "/shaders/ui.frag", null);
    }
}
