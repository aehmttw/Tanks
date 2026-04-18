package basewindow;

/**
 * Corresponds to an OpenGL render pass, generally used for situations where the whole
 * game scene is to be rendered.
 *
 * For example, one render pass may render the depth of the scene from a light source
 * so that another render pass which draws the scene, can determine if objects are in
 * line of sight of that light source.
 */
public abstract class RenderPass
{
    public String name;
    public BaseWindow window;

    public RenderPass(BaseWindow w, String name)
    {
        this.name = name;
        this.window = w;
    }

    public void draw()
    {
        this.window.currentRenderPass = this;
    }
}
