package tanks.rendering;

import basewindow.BaseWindow;
import basewindow.OnlyBaseUniform;

public class ShaderFireworkExplosionTrail extends RendererShader
{
    public Attribute1f timeOffset;
    public Attribute1f posOffset;
    public Attribute1f maxAge;

    @OnlyBaseUniform
    public Uniform1f time;

    @OnlyBaseUniform
    public Uniform3f gravity;

    @OnlyBaseUniform
    public Uniform4f color;

    public ShaderFireworkExplosionTrail(BaseWindow w)
    {
        super(w, "firework");
    }

    @Override
    public void initialize() throws Exception
    {
        this.shaderBase.setUp("/shaders/main.vert", new String[]{"/shaders/main_firework_trail.vert"}, "/shaders/main.frag", null);
        this.shaderShadowMap.setUp("/shaders/shadow_map.vert", new String[]{"/shaders/main_firework_trail.vert"}, "/shaders/shadow_map.frag", null);
    }
}
