package tanks.rendering;

import basewindow.RenderPass;
import basewindow.ShaderGroupSingleStage;
import tanks.Game;

public class ShaderPostLights extends ShaderGroupSingleStage
{
    public Uniform1f width;
    public Uniform1f height;

    public Uniform4f lightPos;
    public Uniform3f lightColor;

    public UniformMatrix4 invProjection;

    public UniformSampler2D depthTex;

    public ShaderPostLights(RenderPass p)
    {
        super(Game.game.window, "post_lights", p);
    }

    @Override
    public void initialize() throws Exception
    {
        super.initialize();
        this.shader.setUp("/shaders/post_lights.vert", "/shaders/post_lights.frag");
    }
}
