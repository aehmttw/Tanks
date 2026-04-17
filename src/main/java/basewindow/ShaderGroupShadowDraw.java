package basewindow;

public abstract class ShaderGroupShadowDraw extends ShaderGroup
{
    public static final int shadow_pass = 0;
    public static final int draw_pass = 1;

    public ShaderBase shaderBase;
    public ShaderShadowMap shaderShadowMap;

    public ShaderGroupShadowDraw(BaseWindow w, String name)
    {
        super(w, name);
        this.shaderBase = new ShaderBase(w);
        this.shaderShadowMap = new ShaderShadowMap(w);
        this.addStage(new ShaderStage(this, w.defaultShadowPass, this.shaderShadowMap));
        this.addStage(new ShaderStage(this, w.defaultDrawPass, this.shaderBase));
    }

    @Override
    public void initialize() throws Exception
    {
        super.initialize();
    }
}
