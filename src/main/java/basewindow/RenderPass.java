package basewindow;

public abstract class RenderPass
{
    public String name;
    public BaseWindow window;

    public RenderPass(BaseWindow w, String name)
    {
        this.name = name;
        this.window = w;
    }

    public abstract void draw();
}
