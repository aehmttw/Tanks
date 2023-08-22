package tanks.rendering;

import basewindow.BaseWindow;
import basewindow.ShaderGroup;

public abstract class RendererShader extends ShaderGroup
{
    public boolean depthTest = true;
    public boolean glow = false;
    public boolean depthMask = true;

    public RendererShader(BaseWindow w, String name)
    {
        super(w, name);
    }
}
