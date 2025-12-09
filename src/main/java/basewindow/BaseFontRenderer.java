package basewindow;

import lwjglwindow.FontRenderer;

public abstract class BaseFontRenderer
{
    public boolean drawBox = false;

    public BaseWindow window;

    public BaseFontRenderer(BaseWindow h)
    {
        this.window = h;
    }

    public abstract boolean supportsChar(char c);

    public abstract void drawString(double x, double y, double z, double sX, double sY, String s, boolean depth);

    public abstract void drawString(double x, double y, double z, double sX, double sY, String s);

    public abstract void drawString(double x, double y, double sX, double sY, String s);

    public abstract double getStringSizeX(double sX, String s);

    public abstract double getStringSizeY(double sY, String s);

    public abstract void addFont(String id, String imageFile, String chars, int[] charSizes);

    public abstract void addFont(String id, String imageFile, String chars, int[] charSizes, int hSpace);

    public abstract void setDefaultFont(String id, String imageFile, String chars, int[] charSizes, int hSpace);

    public abstract FontRenderer.FontInfo getFontById(String id);

    public abstract boolean hasFontId(String id);
}
