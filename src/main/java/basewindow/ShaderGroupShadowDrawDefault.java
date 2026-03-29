package basewindow;

public class ShaderGroupShadowDrawDefault extends ShaderGroupShadowDraw
{

    public ShaderGroupShadowDrawDefault(BaseWindow w)
    {
        super(w, "default");
    }

    @Override
    public void initialize() throws Exception
    {
        super.initialize();
        this.shaderShadowMap.setUp
                ("/shaders/shadow_map.vert", new String[]{"/shaders/main_default.vert"},
                        "/shaders/shadow_map.frag", null);
        this.shaderBase.setUp
                ("/shaders/main.vert", new String[]{"/shaders/main_default.vert"},
                        "/shaders/main.frag", new String[]{"/shaders/main_default.frag"});
    }
}
