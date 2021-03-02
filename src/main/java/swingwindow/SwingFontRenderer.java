package swingwindow;

import basewindow.BaseFontRenderer;

import java.awt.*;
import java.awt.font.FontRenderContext;

public class SwingFontRenderer extends BaseFontRenderer
{
    public FontRenderContext frc = new FontRenderContext(null, true, true);

    public SwingFontRenderer(SwingWindow h)
    {
        super(h);
    }

    @Override
    public void drawString(double x, double y, double z, double sX, double sY, String s)
    {
        throw new UnsupportedOperationException("The Swing renderer does not support 3D!");
    }

    @Override
    public void drawString(double x, double y, double sX, double sY, String s)
    {
        SwingWindow home = (SwingWindow) this.home;
        home.graphics.setFont(home.graphics.getFont().deriveFont(Font.BOLD, (float) sX * 32));
        home.graphics.drawString(process(s), (int)(x - 1 * sX), (int)(y + 17 * sY));
    }

    @Override
    public double getStringSizeX(double sX, String s)
    {
        SwingWindow home = (SwingWindow) this.home;
        home.graphics.setFont(home.graphics.getFont().deriveFont(Font.BOLD, (float) sX * 32));
        return ((SwingWindow) this.home).graphics.getFont().getStringBounds(process(s), frc).getWidth();
    }

    @Override
    public double getStringSizeY(double sY, String s)
    {
        SwingWindow home = (SwingWindow) this.home;
        home.graphics.setFont(home.graphics.getFont().deriveFont(Font.BOLD, (float) sY * 32));
        return ((SwingWindow) this.home).graphics.getFont().getSize() / 3.0;
    }

    public String process(String s)
    {
        while (s.contains("\u00A7"))
        {
            int in = s.indexOf("\u00A7");
            s = s.substring(0, in) + s.substring(in + 13);
        }

        return s;
    }
}
