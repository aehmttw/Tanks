package lwjglwindow;

import basewindow.BaseFontRenderer;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.system.MemoryStack;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBTruetype.*;

public class TruetypeFontRenderer extends BaseFontRenderer
{
    public static class TtfFontInfo
    {
        public final STBTTFontinfo stbInfo;
        public final ByteBuffer ttfBuffer;
        public final float fontScale;
        public final int ascent;
        public final int bakeHeight;
        public final boolean pixelPerfect;
        public final double sizeScale;
        public final double yOffset;

        private final Map<Integer, Integer> glyphTextures = new HashMap<>();
        private final Map<Integer, int[]> glyphMetrics = new HashMap<>();

        public TtfFontInfo(ByteBuffer buffer, int bakeHeight, boolean pixelPerfect, double sizeScale, double yOffset)
        {
            this.ttfBuffer = buffer;
            this.stbInfo = STBTTFontinfo.create();
            this.bakeHeight = bakeHeight;
            this.pixelPerfect = pixelPerfect;
            this.sizeScale = sizeScale;
            this.yOffset = yOffset;

            if (!stbtt_InitFont(stbInfo, buffer))
                throw new RuntimeException("Failed to initialize STB truetype font");

            this.fontScale = stbtt_ScaleForPixelHeight(stbInfo, bakeHeight);

            try (MemoryStack stack = MemoryStack.stackPush())
            {
                IntBuffer a = stack.mallocInt(1);
                IntBuffer d = stack.mallocInt(1);
                IntBuffer lg = stack.mallocInt(1);
                stbtt_GetFontVMetrics(stbInfo, a, d, lg);
                this.ascent = a.get(0);
            }
        }

        public boolean supportsCodepoint(int codepoint)
        {
            return stbtt_FindGlyphIndex(stbInfo, codepoint) != 0;
        }

        public int[] getMetrics(int codepoint)
        {
            if (glyphMetrics.containsKey(codepoint))
                return glyphMetrics.get(codepoint);

            try (MemoryStack stack = MemoryStack.stackPush())
            {
                IntBuffer adv = stack.mallocInt(1);
                IntBuffer lsb = stack.mallocInt(1);
                stbtt_GetCodepointHMetrics(stbInfo, codepoint, adv, lsb);

                IntBuffer x0 = stack.mallocInt(1);
                IntBuffer y0 = stack.mallocInt(1);
                IntBuffer x1 = stack.mallocInt(1);
                IntBuffer y1 = stack.mallocInt(1);
                stbtt_GetCodepointBitmapBox(stbInfo, codepoint, fontScale, fontScale, x0, y0, x1, y1);

                int[] m = new int[]{adv.get(0), x1.get(0) - x0.get(0), y1.get(0) - y0.get(0), x0.get(0), y0.get(0)};
                glyphMetrics.put(codepoint, m);
                return m;
            }
        }

        public int getOrCreateTexture(int codepoint)
        {
            if (glyphTextures.containsKey(codepoint))
                return glyphTextures.get(codepoint);

            try (MemoryStack stack = MemoryStack.stackPush())
            {
                IntBuffer w = stack.mallocInt(1);
                IntBuffer h = stack.mallocInt(1);
                IntBuffer xoff = stack.mallocInt(1);
                IntBuffer yoff = stack.mallocInt(1);

                ByteBuffer bitmap = stbtt_GetCodepointBitmap(stbInfo, fontScale, fontScale, codepoint, w, h, xoff, yoff);

                if (bitmap == null || w.get(0) == 0 || h.get(0) == 0)
                {
                    glyphTextures.put(codepoint, 0);
                    return 0;
                }

                // The project's fragment shader does `color * vertexColor`, where `color` is the
                // sampled texel. GL_ALPHA textures return RGB=0 in GLSL, which would zero out
                // the glyph color. Upload as RGBA with white RGB and STB's alpha so the shader
                // multiplies the per-vertex color through unchanged.
                int bw = w.get(0);
                int bh = h.get(0);
                ByteBuffer rgba = BufferUtils.createByteBuffer(bw * bh * 4);
                for (int i = 0; i < bw * bh; i++)
                {
                    int a = bitmap.get(i) & 0xFF;
                    if (pixelPerfect)
                        a = a >= 128 ? 0xFF : 0x00;
                    rgba.put((byte) 0xFF);
                    rgba.put((byte) 0xFF);
                    rgba.put((byte) 0xFF);
                    rgba.put((byte) a);
                }
                rgba.flip();

                int filter = pixelPerfect ? GL_NEAREST : GL_LINEAR;
                int texId = glGenTextures();
                glBindTexture(GL_TEXTURE_2D, texId);
                glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, bw, bh, 0, GL_RGBA, GL_UNSIGNED_BYTE, rgba);

                stbtt_FreeBitmap(bitmap);

                int[] m = new int[]{getMetrics(codepoint)[0], w.get(0), h.get(0), xoff.get(0), yoff.get(0)};
                glyphMetrics.put(codepoint, m);
                glyphTextures.put(codepoint, texId);
                return texId;
            }
        }
    }

    private final List<TtfFontInfo> fonts = new ArrayList<>();
    private final TtfFontInfo defaultFont;
    private final LWJGLWindow lwjglWindow;

    public TruetypeFontRenderer(LWJGLWindow h, String ttfResourcePath)
    {
        this(h, ttfResourcePath, 64, false, 1.0, 0.0);
    }

    /**
     * @param bakeHeight   Glyph rasterization resolution in pixels. Smaller = chunkier; larger = smoother.
     * @param pixelPerfect If true, glyph alpha is thresholded to 0/255 and texture filtering uses NEAREST.
     * @param sizeScale    Glyph size multiplier. 1.0 = STB's natural size; >1 enlarges to better fill the cell.
     * @param yOffset      Vertical shift in cell-height units. +0.1 moves text down by sY * 3.2 screen pixels.
     */
    public TruetypeFontRenderer(LWJGLWindow h, String ttfResourcePath, int bakeHeight, boolean pixelPerfect, double sizeScale, double yOffset)
    {
        super(h);
        this.lwjglWindow = h;
        this.defaultFont = loadFont(ttfResourcePath, bakeHeight, pixelPerfect, sizeScale, yOffset);
        this.fonts.add(defaultFont);
    }

    private TtfFontInfo loadFont(String path, int bakeHeight, boolean pixelPerfect, double sizeScale, double yOffset)
    {
        try
        {
            InputStream in = lwjglWindow.getResource(path);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            int n;
            while ((n = in.read(chunk)) > 0)
                out.write(chunk, 0, n);
            in.close();
            byte[] bytes = out.toByteArray();
            ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
            buffer.put(bytes).flip();
            return new TtfFontInfo(buffer, bakeHeight, pixelPerfect, sizeScale, yOffset);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to load truetype font: " + path, e);
        }
    }

    /**
     * Adds a fallback font with custom tuning. Used when the primary font lacks a glyph.
     * If the font fails to load (missing file, unsupported format, etc.) the failure is logged
     * and the call is a no-op — a broken fallback should not kill the renderer.
     */
    public void addFont(String ttfPath, int bakeHeight, boolean pixelPerfect, double sizeScale, double yOffset)
    {
        try
        {
            fonts.add(loadFont(ttfPath, bakeHeight, pixelPerfect, sizeScale, yOffset));
        }
        catch (Exception e)
        {
            System.err.println("TruetypeFontRenderer: failed to add fallback font '" + ttfPath + "': " + e.getMessage());
            if (e.getCause() != null)
                System.err.println("  caused by: " + e.getCause());
        }
    }

    /** {@code BaseFontRenderer} contract — for TrueType only the first arg matters; other args ignored. */
    @Override
    public void addFont(String ttfPath, String chars, int[] charSizes)
    {
        addFont(ttfPath, 64, false, 1.0, 0.0);
    }

    /**
     * Loads every font listed in {@code indexPath} (one resource path per line; blank lines and
     * lines starting with {@code #} are ignored) and registers each as a fallback with the given
     * tuning. The index file is read via {@code LWJGLWindow.getResource}, so it goes through the
     * project's normal override-location lookup before falling back to the classpath.
     */
    public void addFontsFromIndex(String indexPath, int bakeHeight, boolean pixelPerfect, double sizeScale, double yOffset)
    {
        try (InputStream in = lwjglWindow.getResource(indexPath);
             BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)))
        {
            int loaded = 0;
            String line;
            while ((line = r.readLine()) != null)
            {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#"))
                    continue;

                int before = fonts.size();
                addFont(line, bakeHeight, pixelPerfect, sizeScale, yOffset);
                if (fonts.size() > before)
                    loaded++;
            }
            System.out.println("TruetypeFontRenderer: loaded " + loaded + " fonts from " + indexPath);
        }
        catch (Exception e)
        {
            System.err.println("TruetypeFontRenderer: failed to read font index '" + indexPath + "': " + e.getMessage());
        }
    }

    @Override
    public boolean supportsChar(char c)
    {
        for (TtfFontInfo font: fonts)
        {
            if (font.supportsCodepoint(c))
                return true;
        }
        return false;
    }

    private TtfFontInfo findFontForChar(char c)
    {
        for (TtfFontInfo font: fonts)
        {
            if (font.supportsCodepoint(c))
                return font;
        }
        return defaultFont;
    }

    protected double drawChar(double x, double y, double z, double sX, double sY, char c, boolean depthtest)
    {
        if (lwjglWindow.drawingShadow)
            return 0;

        int codepoint = c;
        TtfFontInfo font = findFontForChar(c);

        int texId = font.getOrCreateTexture(codepoint);
        int[] m = font.getMetrics(codepoint);
        int advance = m[0];
        int bitmapW = m[1];
        int bitmapH = m[2];
        int xoff = m[3];
        int yoff = m[4];

        double scaleX = sX * 32.0 * font.sizeScale / font.bakeHeight;
        double scaleY = sY * 32.0 * font.sizeScale / font.bakeHeight;

        double baselineY = (y - sY * 16) + font.ascent * font.fontScale * scaleY + sY * 32 * font.yOffset;
        double gx = x + xoff * scaleX;
        double gy = baselineY + yoff * scaleY;
        double gw = bitmapW * scaleX;
        double gh = bitmapH * scaleY;

        if (texId != 0)
        {
            if (depthtest)
                glEnable(GL_DEPTH_TEST);

            lwjglWindow.enableTexture();
            glEnable(GL_BLEND);
            lwjglWindow.setTransparentBlendFunc();
            glDepthMask(false);

            glBindTexture(GL_TEXTURE_2D, texId);

            glBegin(GL_TRIANGLE_FAN);
            glTexCoord2d(0, 0);
            glVertex3d(gx, gy, z);
            glTexCoord2d(0, 1);
            glVertex3d(gx, gy + gh, z);
            glTexCoord2d(1, 1);
            glVertex3d(gx + gw, gy + gh, z);
            glTexCoord2d(1, 0);
            glVertex3d(gx + gw, gy, z);
            glEnd();

            glDepthMask(true);
            lwjglWindow.disableTexture();

            if (depthtest)
                glDisable(GL_DEPTH_TEST);
        }

        return advance * font.fontScale * scaleX;
    }

    @Override
    public void drawString(double x, double y, double z, double sX, double sY, String s)
    {
        drawString(x, y, z, sX, sY, s, true);
    }

    @Override
    public void drawString(double x, double y, double z, double sX, double sY, String s, boolean depth)
    {
        if (depth)
            glEnable(GL_DEPTH_TEST);
        else
            glDisable(GL_DEPTH_TEST);

        double opacity = this.window.colorA;
        double curX = x;
        char[] c = s.toCharArray();

        double r0 = this.window.colorR;
        double g0 = this.window.colorG;
        double b0 = this.window.colorB;
        double a0 = this.window.colorA;

        for (int i = 0; i < c.length; i++)
        {
            if (c[i] == 'Â')
                continue;
            else if (c[i] == '§')
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
                }
                catch (Exception e)
                {
                    continue;
                }

                i += 12;
            }
            else
                curX += drawChar(curX, y, z, sX, sY, c[i], depth);
        }

        glDisable(GL_DEPTH_TEST);
    }

    @Override
    public void drawString(double x, double y, double sX, double sY, String s)
    {
        double opacity = this.window.colorA;
        double curX = x;
        char[] c = s.toCharArray();

        double r0 = this.window.colorR;
        double g0 = this.window.colorG;
        double b0 = this.window.colorB;
        double a0 = this.window.colorA;

        for (int i = 0; i < c.length; i++)
        {
            if (c[i] == 'Â')
                continue;
            else if (c[i] == '§')
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
                }
                catch (Exception e)
                {
                    continue;
                }

                i += 12;
            }
            else
                curX += drawChar(curX, y, 0, sX, sY, c[i], false);
        }
    }

    @Override
    public double getStringSizeX(double sX, String s)
    {
        double w = 0;
        char[] c = s.toCharArray();

        for (int i = 0; i < c.length; i++)
        {
            if (c[i] == 'Â')
                continue;
            else if (c[i] == '§')
            {
                if (s.length() <= i + 1)
                    continue;

                if (c[i + 1] == 'r')
                {
                    i++;
                    continue;
                }

                if (s.length() <= i + 12)
                    continue;

                i += 12;
            }
            else
            {
                TtfFontInfo font = findFontForChar(c[i]);
                int[] m = font.getMetrics(c[i]);
                w += m[0] * font.fontScale * sX * 32.0 * font.sizeScale / font.bakeHeight;
            }
        }

        return w;
    }

    @Override
    public double getStringSizeY(double sY, String s)
    {
        return sY * 32;
    }
}
