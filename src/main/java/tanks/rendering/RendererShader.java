package tanks.rendering;

import basewindow.BaseWindow;
import basewindow.ShaderGroup;
import basewindow.ShaderGroupShadowDraw;

public abstract class RendererShader extends ShaderGroupShadowDraw
{
    public boolean depthTest = true;
    public boolean glow = false;
    public boolean depthMask = true;

    public RendererShader(BaseWindow w, String name)
    {
        super(w, name);
    }
}
