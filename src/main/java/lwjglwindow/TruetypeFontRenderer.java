package lwjglwindow;

import basewindow.BaseFontRenderer;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGUniverse;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.system.MemoryStack;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBTruetype.*;

public class TruetypeFontRenderer extends BaseFontRenderer
{
    public static final boolean print_debug = false;

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
        public final java.awt.Font awtFont;
        public final double awtToStbScaleRatio;

        private final Map<Integer, Integer> glyphTextures = new HashMap<>();
        private final Map<Integer, int[]> glyphMetrics = new HashMap<>();

        /**
         * Non-null when this font carries an OpenType {@code SVG } table (a color emoji font). Its
         * glyphs are rasterized from SVG instead of through STB — see {@link #getOrCreateColorGlyphTexture}.
         */
        public SvgFontTable svg;
        private final Map<Integer, Integer> colorGlyphTextures = new HashMap<>();
        private final Map<Integer, int[]> colorGlyphMetrics = new HashMap<>();
        private final SVGUniverse svgUniverse = new SVGUniverse();

        public TtfFontInfo(ByteBuffer buffer, int bakeHeight, boolean pixelPerfect, double sizeScale, double yOffset)
        {
            this(buffer, 0, bakeHeight, pixelPerfect, sizeScale, yOffset);
        }

        /**
         * @param fontOffset Byte offset of this font's table directory within {@code buffer}: 0 for a
         *                   standalone .ttf, or the {@code stbtt_GetFontOffsetForIndex} value for a
         *                   member of a .ttc collection (where many fonts share one buffer).
         */
        public TtfFontInfo(ByteBuffer buffer, int fontOffset, int bakeHeight, boolean pixelPerfect, double sizeScale, double yOffset)
        {
            this.ttfBuffer = buffer;
            this.stbInfo = STBTTFontinfo.create();
            this.bakeHeight = bakeHeight;
            this.pixelPerfect = pixelPerfect;
            this.sizeScale = sizeScale;
            this.yOffset = yOffset;

            if (!stbtt_InitFont(stbInfo, buffer, fontOffset))
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

            java.awt.Font rawFont = null;
            try
            {
                byte[] bytes = new byte[buffer.remaining()];
                int originalPos = buffer.position();
                buffer.get(bytes);
                buffer.position(originalPos);
                
                rawFont = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, new java.io.ByteArrayInputStream(bytes));
            }
            catch (Exception e)
            {
                System.err.println("TruetypeFontRenderer: failed to load AWT Font: " + e.getMessage());
            }
            this.awtFont = (rawFont != null) ? rawFont.deriveFont((float) bakeHeight) : null;

            double ratio = 1.0;
            if (this.awtFont != null)
            {
                char calChar = 'a';
                if (!supportsCodepoint(calChar))
                {
                    for (int c = 32; c < 65536; c++)
                    {
                        if (supportsCodepoint(c))
                        {
                            calChar = (char) c;
                            break;
                        }
                    }
                }
                
                try
                {
                    java.awt.font.FontRenderContext frc = new java.awt.font.FontRenderContext(null, true, true);
                    java.awt.font.GlyphVector gv = this.awtFont.layoutGlyphVector(frc, new char[]{calChar}, 0, 1, java.awt.Font.LAYOUT_LEFT_TO_RIGHT);
                    double awtAdvance = gv.getGlyphPosition(1).getX();
                    int[] stbMetrics = getGlyphMetrics(stbtt_FindGlyphIndex(stbInfo, calChar));
                    double stbAdvance = stbMetrics[0] * this.fontScale;
                    if (awtAdvance > 0 && stbAdvance > 0)
                    {
                        ratio = stbAdvance / awtAdvance;
                    }
                }
                catch (Exception e)
                {
                    // Keep ratio = 1.0
                }
            }
            this.awtToStbScaleRatio = ratio;

            this.svg = SvgFontTable.tryParse(buffer, fontOffset);
        }

        public boolean isColorEmoji()
        {
            return svg != null;
        }

        public boolean supportsCodepoint(int codepoint)
        {
            return stbtt_FindGlyphIndex(stbInfo, codepoint) != 0;
        }

        public int[] getMetrics(int codepoint)
        {
            return getGlyphMetrics(stbtt_FindGlyphIndex(stbInfo, codepoint));
        }

        public int[] getGlyphMetrics(int glyphId)
        {
            if (glyphMetrics.containsKey(glyphId))
                return glyphMetrics.get(glyphId);

            try (MemoryStack stack = MemoryStack.stackPush())
            {
                IntBuffer adv = stack.mallocInt(1);
                IntBuffer lsb = stack.mallocInt(1);
                stbtt_GetGlyphHMetrics(stbInfo, glyphId, adv, lsb);

                IntBuffer x0 = stack.mallocInt(1);
                IntBuffer y0 = stack.mallocInt(1);
                IntBuffer x1 = stack.mallocInt(1);
                IntBuffer y1 = stack.mallocInt(1);
                stbtt_GetGlyphBitmapBox(stbInfo, glyphId, fontScale, fontScale, x0, y0, x1, y1);

                int[] m = new int[]{adv.get(0), x1.get(0) - x0.get(0), y1.get(0) - y0.get(0), x0.get(0), y0.get(0)};
                glyphMetrics.put(glyphId, m);
                return m;
            }
        }

        public int getOrCreateTexture(int codepoint)
        {
            return getOrCreateGlyphTexture(stbtt_FindGlyphIndex(stbInfo, codepoint));
        }

        public int getOrCreateGlyphTexture(int glyphId)
        {
            if (glyphTextures.containsKey(glyphId))
                return glyphTextures.get(glyphId);

            try (MemoryStack stack = MemoryStack.stackPush())
            {
                IntBuffer w = stack.mallocInt(1);
                IntBuffer h = stack.mallocInt(1);
                IntBuffer xoff = stack.mallocInt(1);
                IntBuffer yoff = stack.mallocInt(1);

                ByteBuffer bitmap = stbtt_GetGlyphBitmap(stbInfo, fontScale, fontScale, glyphId, w, h, xoff, yoff);

                if (bitmap == null || w.get(0) == 0 || h.get(0) == 0)
                {
                    glyphTextures.put(glyphId, 0);
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

                int[] m = new int[]{getGlyphMetrics(glyphId)[0], w.get(0), h.get(0), xoff.get(0), yoff.get(0)};
                glyphMetrics.put(glyphId, m);
                glyphTextures.put(glyphId, texId);
                return texId;
            }
        }

        /**
         * Rasterizes a color-emoji glyph's SVG artwork (from the {@code SVG } table) into a GL
         * texture, and records its placement metrics {@code {advance, w, h, xoff, yoff}} in the same
         * bakeHeight-pixel, baseline-relative, y-down units the STB path uses (so {@link #drawShapedString}
         * positions it identically). Returns 0 (and caches 0) when the glyph has no SVG artwork or
         * rasterization fails; callers fall back to nothing (tofu) for that glyph.
         */
        public int getOrCreateColorGlyphTexture(int glyphId)
        {
            Integer existing = colorGlyphTextures.get(glyphId);
            if (existing != null)
                return existing;

            int texId = 0;
            try
            {
                String content = svg.buildGlyphContent(glyphId);
                if (content != null)
                {
                    int upem = svg.getUnitsPerEm();
                    double pxScale = (double) bakeHeight / upem;

                    // The glyph markup has no viewport of its own (font design units, y-down, origin
                    // on the baseline). We must supply an explicit width/height/viewBox: without one
                    // svgSalamander computes the content's bounding box to build a viewport, and that
                    // path NPEs on <use>-referenced elements. viewBox also does the design->device
                    // mapping. The region is a generous em-relative box that comfortably contains the
                    // artwork (which sits above the baseline, hence the negative y origin); we then
                    // crop to the actual painted pixels so the texture and offsets are tight.
                    double x0 = -0.15 * upem;
                    double y0 = -1.2 * upem;
                    double w = 1.45 * upem;
                    double h = 1.6 * upem;
                    int cw = (int) Math.ceil(w * pxScale);
                    int ch = (int) Math.ceil(h * pxScale);

                    String svgDoc = "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" version=\"1.1\"" +
                        " width=\"" + cw + "\" height=\"" + ch + "\" viewBox=\"" + x0 + " " + y0 + " " + w + " " + h + "\">" +
                        content + "</svg>";

                    BufferedImage canvas = new BufferedImage(cw, ch, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = canvas.createGraphics();
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

                    URI uri = svgUniverse.loadSVG(new StringReader(svgDoc), "glyph" + glyphId);
                    SVGDiagram diagram = svgUniverse.getDiagram(uri);
                    diagram.setIgnoringClipHeuristic(true);
                    diagram.render(g);
                    g.dispose();

                    int[] argb = canvas.getRGB(0, 0, cw, ch, null, 0, cw);
                    int minX = cw;
                    int minY = ch;
                    int maxX = -1;
                    int maxY = -1;
                    for (int p = 0; p < argb.length; p++)
                    {
                        if ((argb[p] >>> 24) > 10)
                        {
                            int px = p % cw;
                            int py = p / cw;
                            if (px < minX) minX = px;
                            if (px > maxX) maxX = px;
                            if (py < minY) minY = py;
                            if (py > maxY) maxY = py;
                        }
                    }

                    if (maxX >= 0)
                    {
                        int bw = maxX - minX + 1;
                        int bh = maxY - minY + 1;
                        texId = uploadArgbCrop(argb, cw, minX, minY, bw, bh);
                        int xoff = (int) Math.round(x0 * pxScale) + minX;
                        int yoff = (int) Math.round(y0 * pxScale) + minY;
                        colorGlyphMetrics.put(glyphId, new int[]{getGlyphMetrics(glyphId)[0], bw, bh, xoff, yoff});
                    }
                }
            }
            catch (Exception e)
            {
                System.err.println("TruetypeFontRenderer: failed to rasterize color glyph " + glyphId + ": " + e.getMessage());
            }

            if (!colorGlyphMetrics.containsKey(glyphId))
                colorGlyphMetrics.put(glyphId, new int[]{getGlyphMetrics(glyphId)[0], 0, 0, 0, 0});

            colorGlyphTextures.put(glyphId, texId);
            return texId;
        }

        public int[] getColorGlyphMetrics(int glyphId)
        {
            int[] m = colorGlyphMetrics.get(glyphId);
            if (m == null)
            {
                getOrCreateColorGlyphTexture(glyphId);
                m = colorGlyphMetrics.get(glyphId);
            }
            return m;
        }

        /**
         * Uploads a straight-alpha ARGB region (rows {@code minY..}, cols {@code minX..} of a
         * {@code canvasW}-wide buffer) as an RGBA GL texture and returns its id.
         */
        private int uploadArgbCrop(int[] argb, int canvasW, int minX, int minY, int bw, int bh)
        {
            ByteBuffer rgba = BufferUtils.createByteBuffer(bw * bh * 4);
            for (int y = 0; y < bh; y++)
            {
                int row = (minY + y) * canvasW + minX;
                for (int x = 0; x < bw; x++)
                {
                    int px = argb[row + x];
                    rgba.put((byte) ((px >> 16) & 0xFF)); // R
                    rgba.put((byte) ((px >> 8) & 0xFF));  // G
                    rgba.put((byte) (px & 0xFF));         // B
                    rgba.put((byte) ((px >> 24) & 0xFF)); // A
                }
            }
            rgba.flip();

            int texId = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, texId);
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, bw, bh, 0, GL_RGBA, GL_UNSIGNED_BYTE, rgba);
            return texId;
        }
    }

    private final List<TtfFontInfo> fonts = new CopyOnWriteArrayList<>();
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

        addColorEmojiFont("/fonts/emoji/NotoColorEmoji.ttf");
    }

    /**
     * Loads the bundled color emoji font and inserts it right after the default font, so emoji
     * codepoints resolve to it ahead of any later-added system fallback fonts. The font's glyphs are
     * rasterized from its OpenType {@code SVG } table (see {@link SvgFontTable}). Non-fatal: a missing
     * or SVG-less font is logged and skipped, leaving emoji to fall back to tofu.
     */
    private void addColorEmojiFont(String resourcePath)
    {
        try
        {
            // Emoji are baked independently of the UI font. The smaller sizeScale (0.9 vs the UI
            // font's 1.4) means the emoji baseline would otherwise land higher than the text
            // baseline; yOffset ~0.4 pushes the emoji box down so it centers vertically on the text.
            TtfFontInfo emoji = loadFont(resourcePath, 128, false, 0.9, 0.6);
            if (emoji.isColorEmoji())
                this.fonts.add(emoji);
            else
                System.err.println("TruetypeFontRenderer: '" + resourcePath + "' has no 'SVG ' table; not used for color emoji");
        }
        catch (Exception e)
        {
            System.err.println("TruetypeFontRenderer: could not load bundled color emoji font '" + resourcePath + "': " + e.getMessage());
        }
    }

    private ByteBuffer readResource(String path) throws IOException
    {
        try (InputStream in = lwjglWindow.getResource(path))
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            int n;
            while ((n = in.read(chunk)) > 0)
                out.write(chunk, 0, n);
            byte[] bytes = out.toByteArray();
            ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
            buffer.put(bytes).flip();
            return buffer;
        }
    }

    private TtfFontInfo loadFont(String path, int bakeHeight, boolean pixelPerfect, double sizeScale, double yOffset)
    {
        try
        {
            return new TtfFontInfo(readResource(path), bakeHeight, pixelPerfect, sizeScale, yOffset);
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

    /** Reads an entire file on disk into a native {@link ByteBuffer} suitable for STB. */
    private static ByteBuffer readFile(String filePath) throws IOException
    {
        byte[] bytes = Files.readAllBytes(Paths.get(filePath));
        ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
        buffer.put(bytes).flip();
        return buffer;
    }

    /**
     * Registers every font contained in {@code buffer} as a fallback, in order, all sharing the one
     * buffer. A plain .ttf reports a single font; a .ttc collection reports several. Order matters:
     * {@link #findFontForChar} returns the first font that has the glyph, so earlier-added fonts win.
     *
     * @return the number of fonts registered
     */
    private int addFontsFromBuffer(ByteBuffer buffer, String label, int bakeHeight, boolean pixelPerfect, double sizeScale, double yOffset)
    {
        int count = stbtt_GetNumberOfFonts(buffer);
        if (count <= 0)
            throw new RuntimeException("not a valid font file (stbtt_GetNumberOfFonts returned " + count + ")");

        int loaded = 0;
        int failed = 0;
        for (int i = 0; i < count; i++)
        {
            int offset = stbtt_GetFontOffsetForIndex(buffer, i);
            if (offset < 0)
                continue;

            try
            {
                fonts.add(new TtfFontInfo(buffer, offset, bakeHeight, pixelPerfect, sizeScale, yOffset));
                loaded++;
            }
            catch (Exception e)
            {
                failed++;
            }
        }

        // STB rasterizes TrueType (glyf) and bare CFF outlines, but not CFF2 — the format used by
        // variable OpenType/CFF fonts such as the system Noto Sans CJK packages. Those fail to init;
        // report the likely cause once per file instead of once per sub-font.
        if (failed > 0)
        {
            String reason = isOpenTypeCFF(buffer) ? "OpenType/CFF outlines (CFF2 variable fonts aren't supported by STB)" : "STB could not initialize them";
            System.err.println("TruetypeFontRenderer: skipped " + failed + " of " + count + " font(s) in '" + label + "' — " + reason);
        }

        return loaded;
    }

    /** True if the first font in {@code buffer} is OpenType/CFF-flavoured ('OTTO'), not TrueType. */
    private static boolean isOpenTypeCFF(ByteBuffer buffer)
    {
        int off = stbtt_GetFontOffsetForIndex(buffer, 0);
        if (off < 0 || off + 4 > buffer.capacity())
            return false;
        // 'OTTO' = 0x4F 0x54 0x54 0x4F
        return buffer.get(off) == 0x4F && buffer.get(off + 1) == 0x54 && buffer.get(off + 2) == 0x54 && buffer.get(off + 3) == 0x4F;
    }

    /**
     * Adds every font in a file on disk (a .ttf, .otf, or multi-font .ttc) as a fallback. Intended
     * for fonts outside the classpath — an OS system font, or a user-supplied file under
     * {@code ~/.tanks/fonts}. A missing, unreadable, or invalid file is logged and skipped: a bad
     * fallback must never kill the renderer.
     *
     * @return the number of fonts registered (0 on failure)
     */
    public int addFontFile(String filePath, int bakeHeight, boolean pixelPerfect, double sizeScale, double yOffset)
    {
        try
        {
            int loaded = addFontsFromBuffer(readFile(filePath), filePath, bakeHeight, pixelPerfect, sizeScale, yOffset);
            if (loaded > 0 && print_debug)
                System.out.println("TruetypeFontRenderer: loaded " + loaded + " font(s) from " + filePath);
            return loaded;
        }
        catch (Exception e)
        {
            System.err.println("TruetypeFontRenderer: failed to load font file '" + filePath + "': " + e.getMessage());
            return 0;
        }
    }

    /**
     * Adds every font file sitting directly inside {@code dirPath} (non-recursive) as a fallback:
     * each .ttc, .ttf, and .otf, processed in case-insensitive filename order so load priority is
     * stable across runs. This is where the downloaded Noto Sans collection in {@code ~/.tanks/fonts}
     * is picked up, alongside any other font the user drops there. The directory is created if it
     * doesn't exist (so there's a place to drop fonts and for the downloader to write to); an empty
     * or uncreatable directory is a no-op.
     */
    public void addFontsFromDirectory(String dirPath, int bakeHeight, boolean pixelPerfect, double sizeScale, double yOffset)
    {
        File dir = new File(dirPath);
        if (!dir.isDirectory() && !dir.mkdirs())
        {
            System.err.println("TruetypeFontRenderer: could not create font directory " + dirPath);
            return;
        }

        File[] files = dir.listFiles((d, name) ->
        {
            String n = name.toLowerCase(Locale.ROOT);
            return n.endsWith(".ttc") || n.endsWith(".ttf") || n.endsWith(".otf");
        });

        if (files == null)
            return;

        Arrays.sort(files, Comparator.comparing(f -> f.getName().toLowerCase(Locale.ROOT)));
        for (File f: files)
            addFontFile(f.getAbsolutePath(), bakeHeight, pixelPerfect, sizeScale, yOffset);
    }

    /**
     * Adds the platform's system fonts as fallbacks — the tier between the bundled Bullet font and
     * any downloaded fonts. Bullet already covers Latin, so the value here is breadth: CJK, Indic,
     * Arabic, Hebrew, Thai and other scripts that no single UI font carries. We therefore register
     * several broad-coverage system fonts and let {@link #findFontForChar} choose per glyph.
     *
     * <p>macOS and Windows keep their fonts in stable, well-known directories, so those are
     * hardcoded. On Linux font locations vary by distro, so we ask fontconfig ({@code fc-match})
     * which file the system actually uses for each of a set of representative scripts; if fontconfig
     * is unavailable we fall back to scanning the standard font directories.
     */
    public void addSystemFonts(int bakeHeight, boolean pixelPerfect, double sizeScale, double yOffset)
    {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);

        // LinkedHashSet: keep discovery order but drop duplicates — one font often serves several
        // scripts (e.g. a Noto CJK file covers zh/ja/ko), and it must not be loaded more than once.
        Set<String> paths = new LinkedHashSet<>();

        if (os.contains("win"))
        {
            String windir = System.getenv("WINDIR");
            String root = (windir != null ? windir : "C:\\Windows") + "\\Fonts\\";
            addExisting(paths,
                    root + "segoeui.ttf",   // Latin, Cyrillic, Greek, Arabic, Hebrew, ...
                    root + "msyh.ttc",      // Microsoft YaHei — Simplified Chinese
                    root + "msjh.ttc",      // Microsoft JhengHei — Traditional Chinese
                    root + "yugothm.ttc",   // Yu Gothic — Japanese
                    root + "msgothic.ttc",  // MS Gothic — Japanese (older systems)
                    root + "malgun.ttf",    // Malgun Gothic — Korean
                    root + "Nirmala.ttf",   // Nirmala UI — Devanagari and other Indic scripts
                    root + "tahoma.ttf");   // extra Arabic/Hebrew coverage
        }
        else if (os.contains("mac") || os.contains("darwin"))
        {
            addExisting(paths,
                    "/System/Library/Fonts/SFNS.ttf",                              // San Francisco — Latin et al.
                    "/System/Library/Fonts/SFNSText.ttf",
                    "/Library/Fonts/Arial Unicode.ttf",                            // very broad multi-script
                    "/System/Library/Fonts/Supplemental/Arial Unicode.ttf",
                    "/System/Library/Fonts/PingFang.ttc",                          // CJK
                    "/System/Library/Fonts/Hiragino Sans GB.ttc",                  // CJK
                    "/System/Library/Fonts/AppleSDGothicNeo.ttc",                  // Korean
                    "/System/Library/Fonts/Kohinoor.ttc",                          // Devanagari
                    "/System/Library/Fonts/Supplemental/Devanagari Sangam MN.ttc",
                    "/System/Library/Fonts/Supplemental/Thonburi.ttc");            // Thai
        }
        else // Linux / other unix
        {
            // Linux resolves and loads its own fonts (fontconfig-driven, with STB-loadability checks).
            addLinuxSystemFonts(bakeHeight, pixelPerfect, sizeScale, yOffset);
            return;
        }

        if (paths.isEmpty())
            System.err.println("TruetypeFontRenderer: no system fonts found for OS '" + os + "'");

        for (String path: paths)
            addFontFile(path, bakeHeight, pixelPerfect, sizeScale, yOffset);
    }

    /** Adds each path that exists as a regular file to {@code out}, skipping the rest. */
    private static void addExisting(Set<String> out, String... paths)
    {
        for (String p: paths)
            if (new File(p).isFile())
                out.add(p);
    }

    /**
     * Resolves and loads broad-coverage Linux system fonts via fontconfig. For each representative
     * language it takes fontconfig's best match ({@code fc-match}); if STB can't rasterize that file
     * (e.g. a CFF2 font like the system Noto Sans CJK), it walks the fonts that actually cover the
     * script ({@code fc-list}) and loads the first STB can use — typically a glyf alternative such as
     * Droid Sans Fallback. If fontconfig is unavailable, or nothing usable is found, it falls back to
     * scanning the standard font directories.
     */
    private void addLinuxSystemFonts(int bakeHeight, boolean pixelPerfect, double sizeScale, double yOffset)
    {
        // (fontconfig language, representative BMP codepoint) for the major writing systems. We only
        // pull in a font for a script when nothing loaded so far renders its sample character, and we
        // verify a candidate actually contains that glyph: fontconfig's best match is often a CFF2
        // font STB can't use, and its coverage lists include near-misses. Picking by real coverage is
        // what steps over the system's CFF2 Noto CJK to a glyf fallback (e.g. Droid Sans Fallback).
        String[] langs = { "ru", "el", "zh", "ja", "ko", "hi", "bn", "ta", "te", "kn", "ml", "gu", "pa", "or", "si", "ar", "he", "th", "my", "km", "lo", "ka", "hy", "am" };
        int[] samples =
            {
                0x0410,
                0x0391,
                0x4E00,
                0x3042,
                0xAC00,
                0x0905,
                0x0985,
                0x0B85,
                0x0C05,
                0x0C85,
                0x0D05,
                0x0A85,
                0x0A05,
                0x0B05,
                0x0D85,
                0x0627,
                0x05D0,
                0x0E01,
                0x1000,
                0x1780,
                0x0E81,
                0x10D0,
                0x0531,
                0x1200
            };

        Set<String> added = new HashSet<>();      // files contributing glyphs to the chain
        Set<String> rejected = new HashSet<>();   // files STB can't use at all (e.g. CFF2)

        for (int s = 0; s < langs.length; s++)
        {
            int codepoint = samples[s];
            if (anyFontSupports(codepoint))
                continue;   // already covered by Bullet or a font loaded for an earlier script

            List<String> candidates;
            try
            {
                // fontconfig's best match first (highest quality), then every font claiming to cover
                // the script.
                List<String> rawCandidates = new ArrayList<>(runFontconfig("fc-match", "-f", "%{file}\n", ":lang=" + langs[s]));
                rawCandidates.addAll(runFontconfig("fc-list", ":lang=" + langs[s], "--format", "%{file}\n"));

                // Prioritize static fonts over variable fonts because Java 8's AWT shaping
                // does not work with modern OpenType variable fonts (.vf / [wght]).
                List<String> staticFonts = new ArrayList<>();
                List<String> variableFonts = new ArrayList<>();
                for (String c: rawCandidates)
                {
                    String lower = c.toLowerCase();
                    if (lower.contains("[wght") || lower.contains("-vf") || lower.contains("google-noto-vf") || lower.contains("variable"))
                        variableFonts.add(c);
                    else
                        staticFonts.add(c);
                }
                candidates = new ArrayList<>();
                candidates.addAll(staticFonts);
                candidates.addAll(variableFonts);
            }
            catch (IOException notInstalled)
            {
                addFontsFromStandardDirs(bakeHeight, pixelPerfect, sizeScale, yOffset);
                return;
            }

            for (String file: candidates)
            {
                if (added.contains(file) || rejected.contains(file) || !new File(file).isFile())
                    continue;
                if (addFontFileCovering(file, codepoint, rejected, bakeHeight, pixelPerfect, sizeScale, yOffset))
                {
                    added.add(file);
                    break;
                }
            }
        }
    }

    /** True if any already-loaded font has a glyph for {@code codepoint}. */
    private boolean anyFontSupports(int codepoint)
    {
        for (TtfFontInfo font: fonts)
            if (font.supportsCodepoint(codepoint))
                return true;
        return false;
    }

    /**
     * Reads {@code filePath} and registers only the sub-fonts that STB can initialize <em>and</em>
     * that contain a glyph for {@code requiredCodepoint}; returns true if at least one was added.
     * Files STB can't use at all (e.g. CFF2) are recorded in {@code rejected} so other scripts skip
     * them. This is what lets the resolver pass over the system's CFF2 Noto CJK to a glyf fallback.
     */
    private boolean addFontFileCovering(String filePath, int requiredCodepoint, Set<String> rejected,
                                        int bakeHeight, boolean pixelPerfect, double sizeScale, double yOffset)
    {
        ByteBuffer buffer;
        try
        {
            buffer = readFile(filePath);
        }
        catch (IOException e)
        {
            rejected.add(filePath);
            return false;
        }

        int count = stbtt_GetNumberOfFonts(buffer);
        int initialized = 0;
        int added = 0;
        for (int i = 0; i < count; i++)
        {
            int offset = stbtt_GetFontOffsetForIndex(buffer, i);
            if (offset < 0)
                continue;

            TtfFontInfo info;
            try
            {
                info = new TtfFontInfo(buffer, offset, bakeHeight, pixelPerfect, sizeScale, yOffset);
            }
            catch (Exception e)
            {
                continue;   // STB couldn't initialize this sub-font (e.g. CFF2)
            }
            initialized++;

            if (info.supportsCodepoint(requiredCodepoint))
            {
                fonts.add(info);
                added++;
            }
        }

        if (initialized == 0)
        {
            rejected.add(filePath);
            System.err.println("TruetypeFontRenderer: cannot use '" + filePath + "' — " +
                (isOpenTypeCFF(buffer) ? "OpenType/CFF2 outlines unsupported by STB" : "no STB-loadable fonts"));
        }
        if (added > 0 && print_debug)
            System.out.println("TruetypeFontRenderer: loaded system font " + filePath);

        return added > 0;
    }

    /** Last-resort fallback when fontconfig is absent: load every font under the standard font dirs. */
    private void addFontsFromStandardDirs(int bakeHeight, boolean pixelPerfect, double sizeScale, double yOffset)
    {
        Set<String> scanned = new LinkedHashSet<>();
        for (String dir: new String[]
            {
                System.getProperty("user.home") + "/.local/share/fonts",
                System.getProperty("user.home") + "/.fonts",
                "/usr/share/fonts",
                "/usr/local/share/fonts"
            }
        )
            collectFontFiles(new File(dir), scanned, 0);

        for (String f: scanned)
            addFontFile(f, bakeHeight, pixelPerfect, sizeScale, yOffset);

        if (scanned.isEmpty())
            System.err.println("TruetypeFontRenderer: no Linux system fonts found");
    }

    /**
     * Runs a fontconfig command and returns its stdout as a list of non-blank, trimmed lines.
     * Throws {@link IOException} if the binary isn't installed.
     */
    private static List<String> runFontconfig(String... command) throws IOException
    {
        Process p = new ProcessBuilder(command).start();

        List<String> lines = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), java.nio.charset.StandardCharsets.UTF_8)))
        {
            String line;
            while ((line = r.readLine()) != null)
            {
                line = line.trim();
                if (!line.isEmpty())
                    lines.add(line);
            }
        }

        try
        {
            p.waitFor(3, java.util.concurrent.TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
        p.destroy();

        return lines;
    }

    /** Recursively collects .ttf/.ttc/.otf files under {@code dir} (depth-bounded) into {@code out}. */
    private static void collectFontFiles(File dir, Set<String> out, int depth)
    {
        if (depth > 6 || !dir.isDirectory())
            return;

        File[] entries = dir.listFiles();
        if (entries == null)
            return;

        for (File f: entries)
        {
            if (f.isDirectory())
                collectFontFiles(f, out, depth + 1);
            else
            {
                String n = f.getName().toLowerCase(Locale.ROOT);
                if (n.endsWith(".ttf") || n.endsWith(".ttc") || n.endsWith(".otf"))
                    out.add(f.getAbsolutePath());
            }
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

    private TtfFontInfo findFontForCodepoint(int cp)
    {
        for (TtfFontInfo font: fonts)
        {
            if (font.supportsCodepoint(cp))
                return font;
        }
        return defaultFont;
    }

    private TtfFontInfo findFontForChar(char c)
    {
        return findFontForCodepoint(c);
    }

    protected double drawChar(double x, double y, double z, double sX, double sY, char c, boolean depthtest)
    {
        if (lwjglWindow.mainRenderPasses.drawingShadow)
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

        double r0 = this.window.colorR;
        double g0 = this.window.colorG;
        double b0 = this.window.colorB;
        double a0 = this.window.colorA;

        List<ShapedRun> runs = parseRuns(s);
        for (ShapedRun run: runs)
        {
            if (run.isReset)
            {
                this.window.setColor(r0 * 255, g0 * 255, b0 * 255, a0 * 255);
            }
            else if (run.color != null)
            {
                this.window.setColor(run.color[0], run.color[1], run.color[2], run.color[3] * opacity);
            }
            else if (!run.text.isEmpty())
            {
                List<FontRun> fontRuns = partitionByFont(run.text);
                for (FontRun fontRun: fontRuns)
                {
                    curX += drawShapedString(curX, y, z, sX, sY, fontRun.text, fontRun.font, depth);
                }
            }
        }

        glDisable(GL_DEPTH_TEST);
    }

    @Override
    public void drawString(double x, double y, double sX, double sY, String s)
    {
        double opacity = this.window.colorA;
        double curX = x;

        double r0 = this.window.colorR;
        double g0 = this.window.colorG;
        double b0 = this.window.colorB;
        double a0 = this.window.colorA;

        List<ShapedRun> runs = parseRuns(s);
        for (ShapedRun run: runs)
        {
            if (run.isReset)
            {
                this.window.setColor(r0 * 255, g0 * 255, b0 * 255, a0 * 255);
            }
            else if (run.color != null)
            {
                this.window.setColor(run.color[0], run.color[1], run.color[2], run.color[3] * opacity);
            }
            else if (!run.text.isEmpty())
            {
                List<FontRun> fontRuns = partitionByFont(run.text);
                for (FontRun fontRun: fontRuns)
                {
                    curX += drawShapedString(curX, y, 0, sX, sY, fontRun.text, fontRun.font, false);
                }
            }
        }
    }

    @Override
    public double getStringSizeX(double sX, String s)
    {
        double w = 0;
        List<ShapedRun> runs = parseRuns(s);
        for (ShapedRun run: runs)
        {
            if (!run.text.isEmpty())
            {
                List<FontRun> fontRuns = partitionByFont(run.text);
                for (FontRun fontRun: fontRuns)
                {
                    w += getShapedStringSizeX(sX, fontRun.text, fontRun.font);
                }
            }
        }
        return w;
    }

    private double getShapedStringSizeX(double sX, String text, TtfFontInfo font)
    {
        if (text.isEmpty())
            return 0;

        if (font.awtFont == null)
        {
            double w = 0;
            for (int i = 0; i < text.length(); i++)
            {
                int[] m = font.getGlyphMetrics(stbtt_FindGlyphIndex(font.stbInfo, text.charAt(i)));
                w += m[0] * font.fontScale * sX * 32.0 * font.sizeScale / font.bakeHeight;
            }
            return w;
        }

        java.awt.font.FontRenderContext frc = new java.awt.font.FontRenderContext(null, true, true);
        java.awt.font.GlyphVector gv = font.awtFont.layoutGlyphVector(frc, text.toCharArray(), 0, text.length(), java.awt.Font.LAYOUT_LEFT_TO_RIGHT);
        double scaleX = sX * 32.0 * font.sizeScale / font.bakeHeight;
        return gv.getGlyphPosition(gv.getNumGlyphs()).getX() * font.awtToStbScaleRatio * scaleX;
    }

    @Override
    public double getStringSizeY(double sY, String s)
    {
        return sY * 32;
    }

    private static class ShapedRun
    {
        public final String text;
        public final double[] color; // Null if no change, size 4 array if specified
        public final boolean isReset;

        public ShapedRun(String text, double[] color, boolean isReset)
        {
            this.text = text;
            this.color = color;
            this.isReset = isReset;
        }
    }

    private static class FontRun
    {
        public final String text;
        public final TtfFontInfo font;

        public FontRun(String text, TtfFontInfo font)
        {
            this.text = text;
            this.font = font;
        }
    }

    private List<ShapedRun> parseRuns(String s)
    {
        List<ShapedRun> runs = new ArrayList<>();
        StringBuilder currentText = new StringBuilder();
        char[] c = s.toCharArray();
        for (int i = 0; i < c.length; i++)
        {
            if (c[i] == 'Â')
            {
                continue;
            }
            else if (c[i] == '§')
            {
                if (s.length() <= i + 1)
                {
                    currentText.append(c[i]);
                    continue;
                }

                if (c[i + 1] == 'r')
                {
                    if (currentText.length() > 0)
                    {
                        runs.add(new ShapedRun(currentText.toString(), null, false));
                        currentText.setLength(0);
                    }
                    runs.add(new ShapedRun("", null, true));
                    i++;
                    continue;
                }

                if (s.length() <= i + 12)
                {
                    currentText.append(c[i]);
                    continue;
                }

                try
                {
                    int r = Integer.parseInt(c[i + 1] + "" + c[i + 2] + "" + c[i + 3]);
                    int g = Integer.parseInt(c[i + 4] + "" + c[i + 5] + "" + c[i + 6]);
                    int b = Integer.parseInt(c[i + 7] + "" + c[i + 8] + "" + c[i + 9]);
                    int a = Integer.parseInt(c[i + 10] + "" + c[i + 11] + "" + c[i + 12]);
                    
                    if (currentText.length() > 0)
                    {
                        runs.add(new ShapedRun(currentText.toString(), null, false));
                        currentText.setLength(0);
                    }
                    runs.add(new ShapedRun("", new double[]{r, g, b, a}, false));
                    i += 12;
                }
                catch (Exception e)
                {
                    currentText.append(c[i]);
                }
            }
            else
            {
                currentText.append(c[i]);
            }
        }
        if (currentText.length() > 0)
        {
            runs.add(new ShapedRun(currentText.toString(), null, false));
        }
        return runs;
    }

    private List<FontRun> partitionByFont(String text)
    {
        List<FontRun> runs = new ArrayList<>();
        if (text.isEmpty())
            return runs;

        StringBuilder currentText = new StringBuilder();
        TtfFontInfo currentFont = null;

        // Iterate by Unicode codepoint (not UTF-16 char) so both halves of a surrogate-pair emoji
        // resolve to — and stay grouped under — the same font. Grouping per-char instead would send
        // each lone surrogate to the default font and produce tofu.
        int i = 0;
        while (i < text.length())
        {
            int cp = text.codePointAt(i);
            int cc = Character.charCount(cp);
            TtfFontInfo font = findFontForCodepoint(cp);

            if (currentFont == null)
                currentFont = font;

            if (font != currentFont)
            {
                runs.add(new FontRun(currentText.toString(), currentFont));
                currentText.setLength(0);
                currentFont = font;
            }

            currentText.append(text, i, i + cc);
            i += cc;
        }
        if (currentText.length() > 0)
        {
            runs.add(new FontRun(currentText.toString(), currentFont));
        }
        return runs;
    }

    private double drawShapedString(double x, double y, double z, double sX, double sY, String text, TtfFontInfo font, boolean depthtest)
    {
        if (text.isEmpty())
            return 0;

        if (font.awtFont == null)
        {
            double curX = x;
            for (int i = 0; i < text.length(); i++)
            {
                curX += drawChar(curX, y, z, sX, sY, text.charAt(i), depthtest);
            }
            return curX - x;
        }

        java.awt.font.FontRenderContext frc = new java.awt.font.FontRenderContext(null, true, true);
        java.awt.font.GlyphVector gv = font.awtFont.layoutGlyphVector(frc, text.toCharArray(), 0, text.length(), java.awt.Font.LAYOUT_LEFT_TO_RIGHT);

        int numGlyphs = gv.getNumGlyphs();
        double scaleX = sX * 32.0 * font.sizeScale / font.bakeHeight;
        double scaleY = sY * 32.0 * font.sizeScale / font.bakeHeight;
        double baselineY = (y - sY * 16) + font.ascent * font.fontScale * scaleY + sY * 32 * font.yOffset;

        if (lwjglWindow.mainRenderPasses.drawingShadow)
        {
            return gv.getGlyphPosition(numGlyphs).getX() * font.awtToStbScaleRatio * scaleX;
        }

        // Color emoji carry their own RGB. Force the vertex color to opaque white so the shader's
        // `color * vertexColor` (ui.frag) leaves the emoji untinted; restore the run color afterward
        // since a run may mix emoji and non-emoji font runs.
        boolean color = font.isColorEmoji();
        double r0 = this.window.colorR;
        double g0 = this.window.colorG;
        double b0 = this.window.colorB;
        double a0 = this.window.colorA;
        if (color)
            this.window.setColor(255, 255, 255, a0 * 255);

        for (int i = 0; i < numGlyphs; i++)
        {
            int glyphId = gv.getGlyphCode(i);
            if (glyphId < 0)
                continue;

            java.awt.geom.Point2D pos = gv.getGlyphPosition(i);

            int texId = color ? font.getOrCreateColorGlyphTexture(glyphId) : font.getOrCreateGlyphTexture(glyphId);
            int[] m = color ? font.getColorGlyphMetrics(glyphId) : font.getGlyphMetrics(glyphId);
            int bitmapW = m[1];
            int bitmapH = m[2];
            int xoff = m[3];
            int yoff = m[4];

            double gx = x + (pos.getX() * font.awtToStbScaleRatio + xoff) * scaleX;
            double gy = baselineY + (pos.getY() * font.awtToStbScaleRatio + yoff) * scaleY;
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
        }

        if (color)
            this.window.setColor(r0 * 255, g0 * 255, b0 * 255, a0 * 255);

        return gv.getGlyphPosition(numGlyphs).getX() * font.awtToStbScaleRatio * scaleX;
    }
}
