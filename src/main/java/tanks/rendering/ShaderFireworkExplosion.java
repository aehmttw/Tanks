package tanks.rendering;

import basewindow.BaseWindow;
import basewindow.OnlyBaseUniform;

public class ShaderFireworkExplosion extends RendererShader
{
    public Attribute2f offset;
    public Attribute1f maxAge;

    @OnlyBaseUniform
    public Uniform1f time;

    @OnlyBaseUniform
    public Uniform1f fireworkGlow;

    @OnlyBaseUniform
    public Uniform3f gravity;

    @OnlyBaseUniform
    public Uniform4f color;

    public ShaderFireworkExplosion(BaseWindow w)
    {
        super(w, "firework");
    }

    @Override
    public void initialize() throws Exception
    {
        this.shaderBase.setUp("/shaders/main.vert", new String[]{"/shaders/main_firework.vert"}, "/shaders/main.frag", null);
        this.shaderShadowMap.setUp("/shaders/shadow_map.vert", new String[]{"/shaders/main_firework.vert"}, "/shaders/shadow_map.frag", null);
    }
}
