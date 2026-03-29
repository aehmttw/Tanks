package tanks.rendering;

import basewindow.BaseWindow;
import basewindow.ShaderGroup;
import basewindow.ShaderGroupShadowDraw;
import basewindow.StageExclusiveUniform;

public class ShaderTracks extends ShaderGroupShadowDraw
{
    @StageExclusiveUniform({ShaderGroupShadowDraw.draw_pass})
    public Uniform1f time;
    @StageExclusiveUniform({ShaderGroupShadowDraw.draw_pass})
    public Uniform1f maxAge;
    public Attribute1f addTime;

    public ShaderTracks(BaseWindow w)
    {
        super(w, "tracks");
    }

    @Override
    public void initialize() throws Exception
    {
        this.shaderBase.setUp("/shaders/main.vert", new String[]{"/shaders/main_tracks.vert"}, "/shaders/main.frag", null);
        this.shaderShadowMap.setUp("/shaders/shadow_map.vert", new String[]{"/shaders/main_default.vert"}, "/shaders/shadow_map.frag", null);
    }
}
