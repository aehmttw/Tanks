package tanks.rendering;

import basewindow.BaseWindow;

public class ShaderExplosive extends ShaderObstacle implements IGlowShader
{
    public ShaderExplosive(BaseWindow w)
    {
        super(w);
        this.name = "explosive";
    }

    @Override
    public float getGlow()
    {
        return 0.5f;
    }
}
