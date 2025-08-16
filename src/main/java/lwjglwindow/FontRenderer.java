package lwjglwindow;

import basewindow.BaseFontRenderer;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FontRenderer extends BaseFontRenderer
{
    public static class FontInfo
    {
        public String chars;
        public int[] charSizes;
        public String image;
        public float size = 16; // how many characters fit per horizontal line
        public int hSpace = 2; // spacing between rows, increase this to 2 for antialiasing to prevent weird artifacts
        public Map<Character, Integer> charIndexMap = new HashMap<>();

        public FontInfo(String image, String chars, int[] charSizes)
        {
            this.image = image;
            this.chars = chars;
            this.charSizes = charSizes;

            for (int i = 0; i < chars.length(); i++)
            {
                charIndexMap.put(chars.charAt(i), i);
            }
        }
    }

    private final List<FontInfo> fontInfos = new ArrayList<>();
    private final FontInfo defaultFont;

    public FontRenderer(LWJGLWindow h, String defaultFontFile)
    {
        super(h);

        defaultFont = new FontInfo(defaultFontFile,
            " !\"#$%&'()*+,-./" +
                "0123456789:;<=>?" +
                "@ABCDEFGHIJKLMNO" +
                "PQRSTUVWXYZ[\\]^_" +
                "'abcdefghijklmno" +
                "pqrstuvwxyz{|}~`" +
                "âăîşţàçæèéêëïôœù" +
                "úûüÿáíóñ¡¿äöå",
            new int[]{
                3, 2, 4, 5, 5, 6, 5, 2, 3, 3, 4, 5, 1, 5, 1, 5,
                5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 1, 1, 5, 5, 5, 5,
                7, 5, 5, 5, 5, 5, 5, 5, 5, 3, 5, 5, 5, 5, 5, 5,
                5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 3, 5, 3, 5, 5,
                2, 5, 5, 5, 5, 5, 4, 5, 5, 1, 5, 4, 2, 5, 5, 5,
                5, 5, 5, 5, 3, 5, 5, 5, 5, 5, 5, 4, 1, 4, 6, 2,
                5, 5, 5, 5, 3, 5, 5, 7, 5, 5, 5, 5, 3, 5, 7, 5,
                5, 5, 5, 5, 5, 3, 5, 5, 3, 5, 5, 5, 5
            });

        fontInfos.add(defaultFont);
    }

    /**
     * Add a new font to the renderer.
     *
     * @param imageFile The image file path.
     * @param chars     The characters to include in the font.
     * @param charSizes The width of each character in (pixels / 4).
     */
    public void addFont(String imageFile, String chars, int[] charSizes)
    {
        fontInfos.add(new FontInfo(imageFile, chars, charSizes));
    }

    public boolean supportsChar(char c)
    {
        for (FontInfo font : fontInfos)
        {
            if (font.charIndexMap.containsKey(c))
            {
                return true;
            }
        }
        return false;
    }

    protected FontInfo findFontForChar(char c)
    {
        for (FontInfo font : fontInfos)
        {
            if (font.charIndexMap.containsKey(c))
            {
                return font;
            }
        }
        return defaultFont;
    }

    protected int drawChar(double x, double y, double z, double sX, double sY, char c, boolean depthtest)
    {
        FontInfo font = findFontForChar(c);
        Integer i = font.charIndexMap.get(c);

        if (i == null)
        {
            i = font.charIndexMap.getOrDefault('?', 31);
        }

        int col = (int) (i % font.size);
        int row = (int) (i / font.size);
        int width = font.charSizes[i];

        if (this.drawBox)
        {
            this.window.shapeRenderer.drawRect(x, y, sX * width * 2, sY * 32);
            this.window.shapeRenderer.drawRect(x, y + sY * 16, sX * width * 2, sY * 16);
            this.window.shapeRenderer.drawRect(x + sX * width * 2, y, sX * width * 2, sY * 32);
            this.window.shapeRenderer.drawRect(x + sX * width * 2, y + sY * 16, sX * width * 2, sY * 16);
        }

        this.window.shapeRenderer.drawImage(x, y - sY * 16, z, sX * 32 * font.size, sY * 32 * font.size,
            col / font.size, (row * font.hSpace) / font.size,
            (col + width / 8f) / font.size, (row * font.hSpace + 2) / font.size,
            font.image, false, depthtest);
        return width;
    }

    public void drawString(double x, double y, double z, double sX, double sY, String s)
    {
        drawString(x, y, z, sX, sY, s, true);
    }

    public void drawString(double x, double y, double z, double sX, double sY, String s, boolean depth)
    {
        if (depth)
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        else
            GL11.glDisable(GL11.GL_DEPTH_TEST);

        double opacity = this.window.colorA;
        double curX = x;
        char[] c = s.toCharArray();

        double r0 = this.window.colorR;
        double g0 = this.window.colorG;
        double b0 = this.window.colorB;
        double a0 = this.window.colorA;

        for (int i = 0; i < c.length; i++)
        {
            if (c[i] == '\u00C2')
                continue;
            else if (c[i] == '\u00A7')
            {
                if (s.length() <= i + 1)
                    continue;

                if (c[i + 1] == 'r')
                {
                    i++;
                    this.window.setColor(r0 * 255, g0 * 255, b0 * 255, a0 * 255);
                    continue;
                }

                if (s.length() <= i + 12)
                    continue;

                try
                {
                    int r = Integer.parseInt(c[i + 1] + "" + c[i + 2] + "" + c[i + 3]);
                    int g = Integer.parseInt(c[i + 4] + "" + c[i + 5] + "" + c[i + 6]);
                    int b = Integer.parseInt(c[i + 7] + "" + c[i + 8] + "" + c[i + 9]);
                    int a = Integer.parseInt(c[i + 10] + "" + c[i + 11] + "" + c[i + 12]);
                    this.window.setColor(r, g, b, a * opacity);
                } catch (Exception e)
                {
                    continue;
                }

                i += 12;
            } else
                curX += (drawChar(curX, y, z, sX, sY, c[i], depth) + 1) * sX * 4;
        }

        GL11.glDisable(GL11.GL_DEPTH_TEST);
    }

    @Override
    public void drawString(double x, double y, double sX, double sY, String s)
    {
        double curX = x;
        char[] c = s.toCharArray();
        double opacity = this.window.colorA;

        double r0 = this.window.colorR;
        double g0 = this.window.colorG;
        double b0 = this.window.colorB;
        double a0 = this.window.colorA;

        for (int i = 0; i < c.length; i++)
        {
            if (c[i] == '\u00C2')
                continue;
            else if (c[i] == '\u00A7')
            {
                if (s.length() <= i + 1)
                    continue;

                if (c[i + 1] == 'r')
                {
                    i++;
                    this.window.setColor(r0 * 255, g0 * 255, b0 * 255, a0 * 255);
                    continue;
                }

                if (s.length() <= i + 12)
                    continue;

                try
                {
                    int r = Integer.parseInt(c[i + 1] + "" + c[i + 2] + "" + c[i + 3]);
                    int g = Integer.parseInt(c[i + 4] + "" + c[i + 5] + "" + c[i + 6]);
                    int b = Integer.parseInt(c[i + 7] + "" + c[i + 8] + "" + c[i + 9]);
                    int a = Integer.parseInt(c[i + 10] + "" + c[i + 11] + "" + c[i + 12]);
                    this.window.setColor(r, g, b, a * opacity);
                } catch (Exception e)
                {
                    continue;
                }

                i += 12;
            } else
                curX += (drawChar(curX, y, 0, sX, sY, c[i], false) + 1) * sX * 4;
        }
    }

    public double getStringSizeX(double sX, String s)
    {
        double w = 0;
        char[] c = s.toCharArray();

        for (int i = 0; i < c.length; i++)
        {
            if (c[i] == '\u00C2')
                continue;
            else if (c[i] == '\u00A7')
            {
                if (s.length() <= i + 12)
                    continue;

                i += 12;
            } else
            {
                FontInfo font = findFontForChar(c[i]);
                Integer index = font.charIndexMap.get(c[i]);
                if (index == null) index = font.charIndexMap.getOrDefault('?', 31);
                w += (font.charSizes[index] + 1) * sX * 4;
            }
        }

        return Math.max(w - sX * 4, 0);
    }

    public double getStringSizeY(double sY, String s)
    {
        return (sY * 32);
    }
}
