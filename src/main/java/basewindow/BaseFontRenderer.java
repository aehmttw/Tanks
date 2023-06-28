package basewindow;

public abstract class BaseFontRenderer
{
    public boolean drawBox = false;

    public BaseWindow window;

    public BaseFontRenderer(BaseWindow h)
    {
        this.window = h;
    }

    public abstract boolean supportsChar(char c);

    public abstract void drawString(double x, double y, double z, double sX, double sY, String s);

    public abstract void drawString(double x, double y, double sX, double sY, String s);

    public abstract double getStringSizeX(double sX, String s);

    public abstract double getStringSizeY(double sY, String s);
}
