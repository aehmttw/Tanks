package tanks.rendering;

import basewindow.RenderPass;

public class ShaderUIDefault extends ShaderGroupUI
{
    public ShaderUIDefault(RenderPass p)
    {
        super(p);
    }

    @Override
    public void initialize() throws Exception
    {
        super.initialize();
        this.shader.setUp("/shaders/ui.vert", new String[]{"/shaders/ui_default.vert"}, "/shaders/ui.frag", null);
    }
}
