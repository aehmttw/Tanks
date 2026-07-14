package basewindow;

public abstract class BaseFontRenderer
{
    public boolean drawBox = false;

    /**
     * When true, color-emoji glyphs are multiplied by the current draw color instead of being drawn
     * in their own colors. Normally emoji ignore the draw color (so they show full color even when
     * text is, say, black); enabling this lets a caller draw emoji as a text drop-shadow — a darker,
     * offset copy behind the real text. Callers should set it back to false when done.
     */
    public boolean tintColorEmoji = false;

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

    public abstract void addFont(String imageFile, String chars, int[] charSizes);
}
