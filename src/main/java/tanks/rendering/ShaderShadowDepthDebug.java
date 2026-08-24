package tanks.rendering;

public class ShaderShadowDepthDebug extends ShaderGroupUI
{
    public ShaderShadowDepthDebug()
    {
        super("shadow_depth_debug");
    }

    @Override
    public void initialize() throws Exception
    {
        super.initialize();
        this.shader.setUp("/shaders/ui.vert", new String[]{"/shaders/ui_default.vert"}, "/shaders/shadow_depth_debug.frag", null);
    }
}
