package tanks.rendering;

import basewindow.RenderPass;
import basewindow.ShaderGroupSingleStage;
import tanks.Game;

public class ShaderPostLightingMix extends ShaderGroupSingleStage
{
    public UniformSampler2D colorTex;
    public UniformSampler2D lightTex;
    public UniformSampler2D glowTex;
    public UniformSampler2D shadowTex;

    public Uniform3f baseLight;

    public ShaderPostLightingMix(RenderPass p)
    {
        super(Game.game.window, "post_lighting_mix", p);
    }

    @Override
    public void initialize() throws Exception
    {
        super.initialize();
        this.shader.setUp("/shaders/post_lighting_mix.vert", "/shaders/post_lighting_mix.frag");
    }
}
