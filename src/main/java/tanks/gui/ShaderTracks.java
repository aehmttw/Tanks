package tanks.gui;

import basewindow.BaseWindow;
import basewindow.ShaderGroup;

public class ShaderTracks extends ShaderGroup
{
    public Uniform1f time;
    public Uniform1f maxAge;
    public Attribute1f addTime;

    public ShaderTracks(BaseWindow w)
    {
        super(w, "tracks");
    }

    @Override
    public void initialize() throws Exception
    {
        this.shaderShadowMap.setUp("/shaders/shadow_map.vert", new String[]{"/shaders/main_default.vert"}, "/shaders/shadow_map.frag", null);
        this.shaderBase.setUp("/shaders/main.vert", new String[]{"/shaders/main_tracks.vert"}, "/shaders/main.frag", null);
    }
}
