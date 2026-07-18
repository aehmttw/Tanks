package lwjglwindow;

import basewindow.BaseFontRenderer;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.system.MemoryStack;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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

        // True if this is the first (or only) font in its buffer. Java 8's Font.createFont, given the
        // whole buffer, can only return the buffer's first font, so only a first-in-buffer face gets a
        // correct AWT font and can be used for complex-script shaping; a later .ttc sub-font is limited
        // to STB per-glyph rendering. The font resolver uses this to prefer a shaping-capable face.
        public final boolean firstInBuffer;

        // The AWT Font is loaded lazily (see getAwtFont) rather than in the constructor: creating one
        // spills the font data to a temp file on disk, so eagerly building one for every registered
        // fallback exhausts the disk quota ("Disk quota exceeded"), after which createFont hands back
        // the wrong face entirely (e.g. Noto Emoji for a CJK font) and shaping produces .notdef tofu.
        // These two fields are only valid once awtFontLoaded is true.
        private volatile boolean awtFontLoaded = false;
        private java.awt.Font awtFont = null;
        private double awtToStbScaleRatio = 1.0;

        private final Map<Integer, Integer> glyphTextures = new HashMap<>();
        private final Map<Integer, int[]> glyphMetrics = new HashMap<>();

        // Cache of AWT-shaped layouts, keyed by the exact string drawn. The UI redraws the same strings
        // every frame, so without this each redraw re-runs Font.layoutGlyphVector (heavy shaping, plus a
        // fresh Point2D per glyph and a char[] copy) — the bulk of the new font system's per-frame
        // garbage. Bounded, access-order LRU: static UI text stays resident while transient strings
        // (timers, FPS, scores) age out instead of growing the map without bound. synchronizedMap keeps
        // it safe if measurement and drawing ever run on different threads (as the metrics maps assume).
        private static final int SHAPED_CACHE_MAX = 2048;
        private final Map<String, ShapedText> shapedCache = java.util.Collections.synchronizedMap(
            new LinkedHashMap<String, ShapedText>(256, 0.75f, true)
            {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, ShapedText> eldest)
                {
                    return size() > SHAPED_CACHE_MAX;
                }
            });

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

            this.firstInBuffer = (fontOffset == stbtt_GetFontOffsetForIndex(buffer, 0));

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

        /**
         * Returns this font's AWT {@link java.awt.Font}, used for complex-script shaping, loading it
         * on first call. The load is deferred because {@link java.awt.Font#createFont} writes the font
         * data out to a temp file on disk; with many system fonts registered as fallbacks, doing it up
         * front for all of them exhausts the disk quota ("Disk quota exceeded"), after which createFont
         * silently returns the wrong face (e.g. Noto Emoji for a CJK font) and shaping through it
         * yields .notdef boxes. We only render a handful of scripts, so each font pays this cost only
         * if it is actually used to draw or measure text.
         *
         * @return the derived AWT Font, or null if it could not be created or is the wrong face
         *         (callers then fall back to per-glyph STB rendering)
         */
        public synchronized java.awt.Font getAwtFont()
        {
            if (!awtFontLoaded)
                loadAwtFont();
            return awtFont;
        }

        /** The AWT-to-STB advance ratio, ensuring the AWT font (and hence the ratio) is loaded first. */
        public synchronized double getAwtToStbScaleRatio()
        {
            getAwtFont();
            return awtToStbScaleRatio;
        }

        /**
         * Returns the cached {@link ShapedText} for {@code text}, shaping it via AWT on the first request
         * and reusing it thereafter. Glyph pen positions are stored in STB units (AWT position times the
         * awt-to-STB ratio), so the only per-frame work left for a repeated string is scaling by sX/sY.
         * Must only be called when {@link #getAwtFont()} is non-null (the shaping path); callers guarantee
         * this and fall back to per-glyph STB rendering otherwise.
         */
        ShapedText getShapedText(String text)
        {
            ShapedText st = shapedCache.get(text);
            if (st != null)
                return st;

            double ratio = getAwtToStbScaleRatio();
            java.awt.font.GlyphVector gv = getAwtFont().layoutGlyphVector(
                SHARED_FRC, text.toCharArray(), 0, text.length(), java.awt.Font.LAYOUT_LEFT_TO_RIGHT);

            int n = gv.getNumGlyphs();
            int[] ids = new int[n];
            double[] px = new double[n];
            double[] py = new double[n];
            for (int i = 0; i < n; i++)
            {
                ids[i] = gv.getGlyphCode(i);
                java.awt.geom.Point2D p = gv.getGlyphPosition(i);
                px[i] = p.getX() * ratio;
                py[i] = p.getY() * ratio;
            }
            double advance = gv.getGlyphPosition(n).getX() * ratio;

            st = new ShapedText(ids, px, py, advance);
            shapedCache.put(text, st);
            return st;
        }

        private void loadAwtFont()
        {
            awtFontLoaded = true;

            java.awt.Font rawFont = null;
            try
            {
                byte[] bytes;
                synchronized (ttfBuffer)
                {
                    bytes = new byte[ttfBuffer.remaining()];
                    int originalPos = ttfBuffer.position();
                    ttfBuffer.get(bytes);
                    ttfBuffer.position(originalPos);
                }

                rawFont = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, new java.io.ByteArrayInputStream(bytes));
            }
            catch (Exception e)
            {
                // Non-fatal: without an AWT font this face just renders per-glyph through STB (no
                // complex-script shaping). Only of interest when debugging font issues.
                if (print_debug)
                    System.err.println("TruetypeFontRenderer: failed to load AWT Font: " + e.getMessage());
            }
            this.awtFont = (rawFont != null) ? rawFont.deriveFont((float) bakeHeight) : null;

            // A .ttc collection packs many sub-fonts into the one buffer we pass to createFont, but
            // Java 8 has no way to pick a sub-font — it always returns the collection's first font. And
            // once the disk quota is exhausted createFont can hand back an unrelated fallback face
            // entirely. Either way the AWT font ends up not matching the face STB loaded, and shaping
            // through it yields .notdef boxes. Detect that by checking the AWT font can display the
            // scripts STB says this face covers; if not, drop it so callers fall back to STB per-glyph
            // rendering with the correct face.
            if (this.awtFont != null && !awtFontMatchesStbCoverage())
                this.awtFont = null;

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
                    java.awt.font.GlyphVector gv = this.awtFont.layoutGlyphVector(SHARED_FRC, new char[]{calChar}, 0, 1, java.awt.Font.LAYOUT_LEFT_TO_RIGHT);
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
        }

        // One distinctive codepoint per major writing system, used to check an AWT font is the same
        // face STB loaded (see loadAwtFont). For the same font file STB coverage and AWT canDisplay
        // agree; they only diverge when createFont handed back a different face (a .ttc sub-font
        // stand-in, or a disk-quota fallback font).
        private static final int[] SCRIPT_PROBES =
            {
                0x0041, // Latin
                0x0410, // Cyrillic
                0x0391, // Greek
                0x4E00, // CJK
                0x3042, // Hiragana
                0xAC00, // Hangul
                0x0905, // Devanagari
                0x0985, // Bengali
                0x0B85, // Tamil
                0x0627, // Arabic
                0x05D0, // Hebrew
                0x0E01, // Thai
                0x10D0, // Georgian
                0x0531, // Armenian
                0x1200  // Ethiopic
            };

        /**
         * True if the loaded AWT font can display every probe script that STB reports this face
         * covers. A false result means createFont returned the wrong face (a first-sub-font stand-in
         * for a .ttc member Java 8 can't address, or a disk-quota fallback), so the AWT font must not
         * be used for this face.
         */
        private boolean awtFontMatchesStbCoverage()
        {
            for (int cp: SCRIPT_PROBES)
            {
                if (supportsCodepoint(cp) && !awtFont.canDisplay(cp))
                    return false;
            }
            return true;
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
    }

    private final List<TtfFontInfo> fonts = new CopyOnWriteArrayList<>();
    private final TtfFontInfo defaultFont;
    private final LWJGLWindow lwjglWindow;

    // A FontRenderContext is immutable and identically configured for every shaping call, so share one
    // instead of allocating a new one per draw/measure. (antialias=true, fractionalMetrics=true.)
    private static final java.awt.font.FontRenderContext SHARED_FRC = new java.awt.font.FontRenderContext(null, true, true);

    /**
     * An AWT-shaped layout cached for one (font, string): glyph ids and their pen positions in STB units
     * (AWT position pre-multiplied by the font's awt-to-STB ratio), plus the total advance in the same
     * units. Resolution-independent — the frame's sX/sY scale is applied at draw/measure time — so one
     * cached instance serves every redraw of that string at any size. See {@link TtfFontInfo#getShapedText}.
     */
    private static final class ShapedText
    {
        final int[] glyphIds;
        final double[] posX;
        final double[] posY;
        final double advance;

        ShapedText(int[] glyphIds, double[] posX, double[] posY, double advance)
        {
            this.glyphIds = glyphIds;
            this.posX = posX;
            this.posY = posY;
            this.advance = advance;
        }
    }

    /**
     * A system/user font file that has been discovered but not yet read from disk. Fallback fonts are
     * registered lazily: at startup we record only paths and the writing systems each is expected to
     * cover, and a file is read + STB-initialized the first time a glyph in one of its scripts is
     * actually drawn (see {@link #resolveDeferredForCodepoint}). This keeps startup from reading and
     * parsing dozens of large system fonts — including CJK collections tens of MB each — that a given
     * session may never display. Cross-platform: the same mechanism backs Linux, macOS and Windows.
     */
    private static class DeferredFont
    {
        // A deferred source is either a file on disk (path set, preReadBuffer null) or a single,
        // already-read sub-font of a .ttc collection (preReadBuffer + faceOffset set, path null). The
        // latter backs lazy TTC loading: when a collection file is first read we initialize only its
        // primary face and register each remaining sub-font as one of these, read in (STB-initialized)
        // on demand — see {@link #loadFontFileLazily} and {@link #resolveDeferred}.
        final String path;
        final ByteBuffer preReadBuffer;
        final int faceOffset;

        final int bakeHeight;
        final boolean pixelPerfect;
        final double sizeScale;
        final double yOffset;

        // The Unicode blocks this font is registered to serve. Empty when {@link #broad} is true.
        final Set<Character.UnicodeBlock> blocks;

        // A broad-coverage fallback (e.g. the bundled Noto Sans collection) that is considered for any
        // codepoint, not just a specific block. Broad fonts keep their registration priority, so a
        // user-supplied broad font still wins over a system font for the same glyph.
        final boolean broad;

        // Set once we've attempted to read + load this source, so we never read/init it twice (even if
        // it added nothing, e.g. a CFF2 file STB can't use).
        volatile boolean resolved = false;

        /** A font file on disk, read + loaded (its primary face) on demand. */
        DeferredFont(String path, boolean broad, Set<Character.UnicodeBlock> blocks,
                     int bakeHeight, boolean pixelPerfect, double sizeScale, double yOffset)
        {
            this.path = path;
            this.preReadBuffer = null;
            this.faceOffset = 0;
            this.broad = broad;
            this.blocks = blocks;
            this.bakeHeight = bakeHeight;
            this.pixelPerfect = pixelPerfect;
            this.sizeScale = sizeScale;
            this.yOffset = yOffset;
        }

        /** A single already-read sub-font of a collection, STB-initialized on demand (lazy TTC). */
        DeferredFont(ByteBuffer preReadBuffer, int faceOffset, boolean broad, Set<Character.UnicodeBlock> blocks,
                     int bakeHeight, boolean pixelPerfect, double sizeScale, double yOffset)
        {
            this.path = null;
            this.preReadBuffer = preReadBuffer;
            this.faceOffset = faceOffset;
            this.broad = broad;
            this.blocks = blocks;
            this.bakeHeight = bakeHeight;
            this.pixelPerfect = pixelPerfect;
            this.sizeScale = sizeScale;
            this.yOffset = yOffset;
        }
    }

    // Registered-but-unread fallback fonts, in priority order (earlier wins). Read on demand.
    private final List<DeferredFont> deferredFonts = new CopyOnWriteArrayList<>();
    // Unicode blocks we've already tried to resolve, so a codepoint with no available font doesn't
    // re-scan (and re-read broad fonts) on every frame it's drawn.
    private final Set<Character.UnicodeBlock> resolvedBlocks = ConcurrentHashMap.newKeySet();
    private final Object deferredLock = new Object();

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

        // Every other font loads its AWT font lazily to avoid exhausting the disk quota, but the
        // default (Bullet) font is always in use, so load it up front so it's ready immediately.
        this.defaultFont.getAwtFont();
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
     * Loads a font file's <em>primary</em> face from {@code buffer} and defers the rest — lazy TTC
     * loading. A plain .ttf reports a single font (loaded now); a .ttc collection reports several, of
     * which only the first STB accepts is initialized here. That first face is {@code firstInBuffer},
     * so it gets a correct AWT font and is always preferred by {@link #findLoadedFontForChar}; and for
     * the weight/style-variant collections we fall back to (CJK etc.) every sub-font shares its Unicode
     * coverage, so the siblings would never be selected over it. Initializing them up front is pure
     * waste, so each remaining sub-font is registered as a deferred face — routed by the same script
     * tags ({@code broad}/{@code blocks}) and STB-initialized on demand only if a glyph turns up that
     * the primary face lacks (a genuinely heterogeneous collection). All faces share the one buffer.
     *
     * @return the number of faces initialized now (0 or 1)
     */
    private int loadFontFileLazily(ByteBuffer buffer, String label, boolean broad, Set<Character.UnicodeBlock> blocks,
                                   int bakeHeight, boolean pixelPerfect, double sizeScale, double yOffset)
    {
        int count = stbtt_GetNumberOfFonts(buffer);
        if (count <= 0)
            throw new RuntimeException("not a valid font file (stbtt_GetNumberOfFonts returned " + count + ")");

        // Initialize faces in order until the first STB accepts — the collection's primary face. A
        // healthy .ttc/.ttf yields it on the first try (index 0, hence firstInBuffer); we only step
        // past a face here if STB rejects it (e.g. a CFF2 sub-font).
        int i = 0;
        int loaded = 0;
        for (; i < count; i++)
        {
            int offset = stbtt_GetFontOffsetForIndex(buffer, i);
            if (offset < 0)
                continue;

            try
            {
                fonts.add(new TtfFontInfo(buffer, offset, bakeHeight, pixelPerfect, sizeScale, yOffset));
                loaded++;
                i++;
                break;
            }
            catch (Exception ignored)
            {
                // STB couldn't init this face; try the next one in the collection.
            }
        }

        // Defer every remaining sub-font — read in on demand, never up front (see method contract).
        for (; i < count; i++)
        {
            int offset = stbtt_GetFontOffsetForIndex(buffer, i);
            if (offset < 0)
                continue;
            registerDeferredFace(buffer, offset, broad, blocks, bakeHeight, pixelPerfect, sizeScale, yOffset);
        }

        if (loaded == 0 && print_debug)
        {
            // STB rasterizes TrueType (glyf) and bare CFF outlines, but not CFF2 — the format used by
            // variable OpenType/CFF fonts such as the system Noto Sans CJK packages. Those fail to init;
            // this is expected and routine (the resolver simply steps to a glyf fallback).
            String reason = isOpenTypeCFF(buffer) ? "OpenType/CFF outlines (CFF2 variable fonts aren't supported by STB)" : "STB could not initialize any face";
            System.err.println("TruetypeFontRenderer: loaded no faces from '" + label + "' — " + reason);
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
        // A directly-added file has no script tags of its own, so treat its deferred sub-fonts as broad
        // (considered for any codepoint), matching how a file dropped in the fonts directory is handled.
        return addFontFile(filePath, true, Collections.emptySet(), bakeHeight, pixelPerfect, sizeScale, yOffset);
    }

    /**
     * Reads {@code filePath}, initializes its primary face now, and defers the rest (lazy TTC). The
     * deferred sub-fonts inherit {@code broad}/{@code blocks} for on-demand routing.
     *
     * @return the number of faces initialized now (0 on failure)
     */
    private int addFontFile(String filePath, boolean broad, Set<Character.UnicodeBlock> blocks,
                            int bakeHeight, boolean pixelPerfect, double sizeScale, double yOffset)
    {
        try
        {
            int loaded = loadFontFileLazily(readFile(filePath), filePath, broad, blocks, bakeHeight, pixelPerfect, sizeScale, yOffset);
            if (loaded > 0 && print_debug)
                System.out.println("TruetypeFontRenderer: loaded " + loaded + " font(s) from " + filePath);
            return loaded;
        }
        catch (Exception e)
        {
            // Non-fatal: a fallback file that won't read is just skipped (the resolver tries the next
            // candidate). Read on demand now, so keep it out of normal runtime logs.
            if (print_debug)
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
        // Registered as broad, high-priority deferred fallbacks: a user drops a font here to have it
        // cover whatever a script needs, so it should win over system fonts. It's read on demand the
        // first time a glyph it might cover is drawn, not now — a bundled Noto collection can be tens
        // of MB, and a session that only shows Latin should never pay to read it.
        for (File f: files)
            deferBroadFont(f.getAbsolutePath(), bakeHeight, pixelPerfect, sizeScale, yOffset);
    }

    // Representative codepoints per writing system, used to tag which scripts a deferred font serves.
    private static final int CP_CYRILLIC = 0x0410;
    private static final int CP_GREEK = 0x0391;
    private static final int CP_ARABIC = 0x0627;
    private static final int CP_HEBREW = 0x05D0;
    private static final int CP_CJK = 0x4E00;
    private static final int CP_HIRAGANA = 0x3042;
    private static final int CP_KATAKANA = 0x30A2;
    private static final int CP_HANGUL = 0xAC00;
    private static final int CP_DEVANAGARI = 0x0905;
    private static final int CP_BENGALI = 0x0985;
    private static final int CP_TAMIL = 0x0B85;
    private static final int CP_TELUGU = 0x0C05;
    private static final int CP_GUJARATI = 0x0A85;
    private static final int CP_GURMUKHI = 0x0A05;
    private static final int CP_THAI = 0x0E01;

    /**
     * Registers the platform's system fonts as <em>deferred</em> fallbacks — the tier between the
     * bundled Bullet font and any downloaded fonts. Bullet already covers Latin, so the value here is
     * breadth: CJK, Indic, Arabic, Hebrew, Thai and other scripts no single UI font carries. Nothing is
     * read here; each font is tagged with the scripts it serves and only read the first time one of
     * those scripts is drawn (see {@link #resolveDeferredForCodepoint}).
     *
     * <p>macOS and Windows keep their fonts in stable, well-known directories, so those are hardcoded
     * (each with its scripts). On Linux font locations vary by distro, so we ask fontconfig which files
     * cover each representative script; if fontconfig is unavailable we fall back to the standard font
     * directories.
     */
    public void addSystemFonts(int bakeHeight, boolean pixelPerfect, double sizeScale, double yOffset)
    {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);

        if (os.contains("win"))
        {
            String windir = System.getenv("WINDIR");
            String root = (windir != null ? windir : "C:\\Windows") + "\\Fonts\\";
            // Segoe UI covers Latin/Cyrillic/Greek/Arabic/Hebrew; the CJK and Indic families follow.
            deferSystemFont(root + "segoeui.ttf", new int[]{CP_CYRILLIC, CP_GREEK, CP_ARABIC, CP_HEBREW}, bakeHeight, pixelPerfect, sizeScale, yOffset);
            deferSystemFont(root + "msyh.ttc",    new int[]{CP_CJK}, bakeHeight, pixelPerfect, sizeScale, yOffset);   // YaHei — Simplified Chinese
            deferSystemFont(root + "msjh.ttc",    new int[]{CP_CJK}, bakeHeight, pixelPerfect, sizeScale, yOffset);   // JhengHei — Traditional Chinese
            deferSystemFont(root + "yugothm.ttc", new int[]{CP_HIRAGANA, CP_KATAKANA, CP_CJK}, bakeHeight, pixelPerfect, sizeScale, yOffset);   // Yu Gothic — Japanese
            deferSystemFont(root + "msgothic.ttc", new int[]{CP_HIRAGANA, CP_KATAKANA, CP_CJK}, bakeHeight, pixelPerfect, sizeScale, yOffset);   // MS Gothic — Japanese (older)
            deferSystemFont(root + "malgun.ttf",  new int[]{CP_HANGUL}, bakeHeight, pixelPerfect, sizeScale, yOffset);   // Malgun Gothic — Korean
            deferSystemFont(root + "Nirmala.ttf", new int[]{CP_DEVANAGARI, CP_BENGALI, CP_TAMIL, CP_TELUGU, CP_GUJARATI, CP_GURMUKHI}, bakeHeight, pixelPerfect, sizeScale, yOffset);   // Nirmala UI — Indic
            deferSystemFont(root + "tahoma.ttf",  new int[]{CP_ARABIC, CP_HEBREW}, bakeHeight, pixelPerfect, sizeScale, yOffset);
        }
        else if (os.contains("mac") || os.contains("darwin"))
        {
            deferSystemFont("/System/Library/Fonts/SFNS.ttf",     new int[]{CP_CYRILLIC, CP_GREEK}, bakeHeight, pixelPerfect, sizeScale, yOffset);
            deferSystemFont("/System/Library/Fonts/SFNSText.ttf", new int[]{CP_CYRILLIC, CP_GREEK}, bakeHeight, pixelPerfect, sizeScale, yOffset);
            // Arial Unicode is a very broad multi-script font — treat as a broad fallback.
            deferBroadFont("/Library/Fonts/Arial Unicode.ttf", bakeHeight, pixelPerfect, sizeScale, yOffset);
            deferBroadFont("/System/Library/Fonts/Supplemental/Arial Unicode.ttf", bakeHeight, pixelPerfect, sizeScale, yOffset);
            deferSystemFont("/System/Library/Fonts/PingFang.ttc",          new int[]{CP_CJK}, bakeHeight, pixelPerfect, sizeScale, yOffset);
            deferSystemFont("/System/Library/Fonts/Hiragino Sans GB.ttc",  new int[]{CP_CJK, CP_HIRAGANA, CP_KATAKANA}, bakeHeight, pixelPerfect, sizeScale, yOffset);
            deferSystemFont("/System/Library/Fonts/AppleSDGothicNeo.ttc",  new int[]{CP_HANGUL}, bakeHeight, pixelPerfect, sizeScale, yOffset);
            deferSystemFont("/System/Library/Fonts/Kohinoor.ttc",          new int[]{CP_DEVANAGARI}, bakeHeight, pixelPerfect, sizeScale, yOffset);
            deferSystemFont("/System/Library/Fonts/Supplemental/Devanagari Sangam MN.ttc", new int[]{CP_DEVANAGARI}, bakeHeight, pixelPerfect, sizeScale, yOffset);
            deferSystemFont("/System/Library/Fonts/Supplemental/Thonburi.ttc", new int[]{CP_THAI}, bakeHeight, pixelPerfect, sizeScale, yOffset);
        }
        else // Linux / other unix
        {
            // Linux resolves its own fonts (fontconfig-driven, with STB-loadability checks).
            addLinuxSystemFonts(bakeHeight, pixelPerfect, sizeScale, yOffset);
        }
    }

    /** Registers a deferred system font for the scripts {@code sampleCodepoints} name, if it exists. */
    private void deferSystemFont(String path, int[] sampleCodepoints, int bakeHeight, boolean pixelPerfect, double sizeScale, double yOffset)
    {
        registerDeferredFont(path, false, sampleCodepoints, bakeHeight, pixelPerfect, sizeScale, yOffset);
    }

    /** Registers a deferred broad-coverage fallback (considered for any codepoint), if it exists. */
    private void deferBroadFont(String path, int bakeHeight, boolean pixelPerfect, double sizeScale, double yOffset)
    {
        registerDeferredFont(path, true, null, bakeHeight, pixelPerfect, sizeScale, yOffset);
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

        // Collect, per candidate file, the scripts (sample codepoints) it claims to cover — deduped by
        // path so the same file is registered once, tagged for every script it serves. Preserving
        // first-seen order keeps static fonts (added static-first below) ahead of variable ones, so at
        // load time a glyf font is read before a CFF2 variable font STB can't use (and often before it
        // is even reached). No files are read here — coverage is verified lazily when a script is drawn.
        Map<String, List<Integer>> fileToSamples = new LinkedHashMap<>();

        for (int s = 0; s < langs.length; s++)
        {
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
                if (!new File(file).isFile())
                    continue;
                fileToSamples.computeIfAbsent(file, k -> new ArrayList<>()).add(samples[s]);
            }
        }

        for (Map.Entry<String, List<Integer>> e: fileToSamples.entrySet())
        {
            int[] cps = new int[e.getValue().size()];
            for (int i = 0; i < cps.length; i++)
                cps[i] = e.getValue().get(i);
            registerDeferredFont(e.getKey(), false, cps, bakeHeight, pixelPerfect, sizeScale, yOffset);
        }
    }

    /**
     * Last-resort fallback when fontconfig is absent: register every font under the standard font dirs
     * as a deferred broad fallback. Without fontconfig we can't cheaply tell which file covers which
     * script, so each is considered for any codepoint and read on demand — a scan can turn up hundreds
     * of files, and reading them all up front is exactly what lazy loading avoids.
     */
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
            deferBroadFont(f, bakeHeight, pixelPerfect, sizeScale, yOffset);

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

    /**
     * Picks the font used to render {@code c}. First checks the already-loaded fonts; on a miss it
     * reads in any deferred (registered-but-unread) font that serves {@code c}'s script, then checks
     * again — this is what triggers the lazy load of a system fallback the first time its script is
     * drawn. Falls back to the default font if nothing covers {@code c}.
     */
    private TtfFontInfo findFontForChar(char c)
    {
        TtfFontInfo f = findLoadedFontForChar(c);
        if (f != null && f.firstInBuffer)
            return f;   // already have a shaping-capable cover — the best kind, no need to read more

        // Either nothing covers c yet, or only a STB-only face does. Read in this script's deferred
        // fonts (once per block) so a shaping-capable standalone font gets a chance to be loaded and
        // preferred — this is what keeps a broad .ttc from shadowing e.g. Droid Sans Devanagari.
        if (!resolvedBlocks.contains(blockOf(c)))
        {
            resolveDeferredForCodepoint(c);
            TtfFontInfo resolved = findLoadedFontForChar(c);
            if (resolved != null)
                f = resolved;
        }

        return f != null ? f : defaultFont;
    }

    /**
     * Finds an already-loaded font covering {@code c}, or null if none is loaded yet. A shaping-capable
     * face (first-in-buffer, so it gets a correct AWT font — see {@link TtfFontInfo#firstInBuffer}) is
     * preferred over one that can only be STB per-glyph rendered, even if the STB-only face appears
     * earlier. This lets a proper standalone system font (e.g. Droid Sans Devanagari) win over a
     * shaping-incapable .ttc sub-font that also claims the script. Among faces of equal capability the
     * earliest wins, preserving priority ordering.
     */
    private TtfFontInfo findLoadedFontForChar(char c)
    {
        TtfFontInfo stbOnly = null;
        for (TtfFontInfo font: fonts)
        {
            if (font.supportsCodepoint(c))
            {
                if (font.firstInBuffer)
                    return font;
                if (stbOnly == null)
                    stbOnly = font;
            }
        }
        return stbOnly;
    }

    /**
     * Reads in any deferred fallback font registered for {@code cp}'s Unicode block (plus any broad
     * fallbacks), in priority order, until one covers {@code cp}. Called on a render-thread cache miss,
     * so it does the on-demand file read that laziness trades for a smaller startup. Each block is only
     * scanned once — a codepoint with no available font records the block and never re-reads.
     */
    private void resolveDeferredForCodepoint(int cp)
    {
        Character.UnicodeBlock block = blockOf(cp);
        if (resolvedBlocks.contains(block))
            return;

        synchronized (deferredLock)
        {
            if (resolvedBlocks.contains(block))
                return;

            // Resolving a collection file registers its remaining sub-fonts as new deferred faces (lazy
            // TTC); those must be considered too if the primary face didn't cover cp. We re-scan until a
            // pass resolves nothing new — each resolve marks a source resolved permanently, so the set
            // of matching-unresolved sources strictly shrinks and this terminates. For the common case
            // (primary face covers cp) we break on the first pass and the deferred siblings are never
            // touched.
            boolean coveredByShaper = false;
            boolean progress = true;
            while (progress && !coveredByShaper)
            {
                progress = false;

                for (DeferredFont df: deferredFonts)
                {
                    if (df.resolved)
                        continue;
                    if (!df.broad && !df.blocks.contains(block))
                        continue;

                    df.resolved = true;
                    progress = true;
                    resolveDeferred(df);

                    // Stop only once a shaping-capable face covers the glyph. A broad .ttc (e.g. the
                    // bundled Noto collection) often covers the script with a non-first-in-buffer sub-font
                    // that can't shape (no correct AWT font) — keep reading the block's standalone fonts
                    // (e.g. Droid Sans Devanagari) so the firstInBuffer one is present for
                    // findLoadedFontForChar to prefer. If none turns up we still have the STB-only cover.
                    TtfFontInfo cover = findLoadedFontForChar((char) cp);
                    if (cover != null && cover.firstInBuffer)
                    {
                        coveredByShaper = true;
                        break;
                    }
                }
            }

            resolvedBlocks.add(block);
        }
    }

    /**
     * Loads a single deferred source into the live font list: reads the file and initializes its
     * primary face (deferring the rest) for a path-based source, or STB-initializes the one already-read
     * collection sub-font for a face-based source (lazy TTC). Always runs under {@link #deferredLock}.
     */
    private void resolveDeferred(DeferredFont df)
    {
        if (df.preReadBuffer != null)
        {
            try
            {
                fonts.add(new TtfFontInfo(df.preReadBuffer, df.faceOffset, df.bakeHeight, df.pixelPerfect, df.sizeScale, df.yOffset));
            }
            catch (Exception e)
            {
                // A deferred sub-font STB can't init (e.g. CFF2) — the resolver simply keeps looking.
                if (print_debug)
                    System.err.println("TruetypeFontRenderer: failed to init deferred sub-font at offset " + df.faceOffset + ": " + e.getMessage());
            }
            return;
        }

        addFontFile(df.path, df.broad, df.blocks, df.bakeHeight, df.pixelPerfect, df.sizeScale, df.yOffset);
    }

    /**
     * Registers a single not-yet-initialized sub-font of an already-read collection as a deferred face
     * (lazy TTC). It inherits the collection's {@code broad}/{@code blocks} routing and shares its
     * buffer. Serialized on {@link #deferredLock} like {@link #registerDeferredFont}. Unlike that
     * method it does not re-open resolved blocks: the primary face is loaded eagerly alongside these,
     * so coverage is already available, and in the normal resolve path the siblings are picked up by
     * the enclosing re-scan in {@link #resolveDeferredForCodepoint} without needing re-invalidation.
     */
    private void registerDeferredFace(ByteBuffer buffer, int faceOffset, boolean broad, Set<Character.UnicodeBlock> blocks,
                                      int bakeHeight, boolean pixelPerfect, double sizeScale, double yOffset)
    {
        synchronized (deferredLock)
        {
            deferredFonts.add(new DeferredFont(buffer, faceOffset, broad, blocks, bakeHeight, pixelPerfect, sizeScale, yOffset));
        }
    }

    /** Unicode block of {@code cp}, mapping the "no block" case to a stable non-null sentinel. */
    private static Character.UnicodeBlock blockOf(int cp)
    {
        Character.UnicodeBlock b = null;
        try { b = Character.UnicodeBlock.of(cp); } catch (IllegalArgumentException ignored) { }
        return b != null ? b : Character.UnicodeBlock.BASIC_LATIN;
    }

    /**
     * Registers {@code path} as a deferred fallback without reading it. {@code sampleCodepoints} name
     * the writing systems it should serve (one representative codepoint each); their Unicode blocks
     * become its routing tags. Pass {@code broad = true} (with no samples) for a broad-coverage
     * fallback considered for any codepoint. Non-existent files are skipped.
     */
    private void registerDeferredFont(String path, boolean broad, int[] sampleCodepoints,
                                      int bakeHeight, boolean pixelPerfect, double sizeScale, double yOffset)
    {
        if (path == null || !new File(path).isFile())
            return;

        Set<Character.UnicodeBlock> blocks = new HashSet<>();
        if (sampleCodepoints != null)
            for (int cp: sampleCodepoints)
                blocks.add(blockOf(cp));

        // Registration is serialized against resolveDeferredForCodepoint on deferredLock. Discovery runs
        // on a background thread while the render thread resolves fonts on demand; without this lock the
        // render thread (iterating a copy-on-write snapshot) could finish resolving a block and mark it
        // done just after this font was added but before the invalidation below, stranding the block on
        // an inferior font forever. Under the lock the two operations can't interleave: the block is
        // either resolved before this font exists (then re-opened here) or after (then it's included).
        synchronized (deferredLock)
        {
            deferredFonts.add(new DeferredFont(path, broad, blocks, bakeHeight, pixelPerfect, sizeScale, yOffset));

            // A font registered after a codepoint in its script was already drawn (and its block marked
            // resolved) must re-open that block so the next draw picks the new font up.
            if (broad)
                resolvedBlocks.clear();
            else
                resolvedBlocks.removeAll(blocks);
        }
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

        java.awt.Font awtFont = font.getAwtFont();
        if (awtFont == null)
        {
            double w = 0;
            for (int i = 0; i < text.length(); i++)
            {
                int[] m = font.getGlyphMetrics(stbtt_FindGlyphIndex(font.stbInfo, text.charAt(i)));
                w += m[0] * font.fontScale * sX * 32.0 * font.sizeScale / font.bakeHeight;
            }
            return w;
        }

        double scaleX = sX * 32.0 * font.sizeScale / font.bakeHeight;
        return font.getShapedText(text).advance * scaleX;
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
        TtfFontInfo currentFont = findFontForChar(text.charAt(0));
        currentText.append(text.charAt(0));

        for (int i = 1; i < text.length(); i++)
        {
            char c = text.charAt(i);
            TtfFontInfo font = findFontForChar(c);
            if (font == currentFont)
            {
                currentText.append(c);
            }
            else
            {
                runs.add(new FontRun(currentText.toString(), currentFont));
                currentText.setLength(0);
                currentFont = font;
                currentText.append(c);
            }
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

        java.awt.Font awtFont = font.getAwtFont();
        if (awtFont == null)
        {
            double curX = x;
            for (int i = 0; i < text.length(); i++)
            {
                curX += drawChar(curX, y, z, sX, sY, text.charAt(i), depthtest);
            }
            return curX - x;
        }

        ShapedText shaped = font.getShapedText(text);

        int numGlyphs = shaped.glyphIds.length;
        double scaleX = sX * 32.0 * font.sizeScale / font.bakeHeight;
        double scaleY = sY * 32.0 * font.sizeScale / font.bakeHeight;
        double baselineY = (y - sY * 16) + font.ascent * font.fontScale * scaleY + sY * 32 * font.yOffset;

        if (lwjglWindow.mainRenderPasses.drawingShadow)
        {
            return shaped.advance * scaleX;
        }

        for (int i = 0; i < numGlyphs; i++)
        {
            int glyphId = shaped.glyphIds[i];
            // Skip glyphs the layout engine deleted while shaping (e.g. a virama consumed to form an
            // Indic conjunct). Java flags these with the 0xFFFF/0xFFFE sentinel; feeding that to STB
            // would draw an out-of-range .notdef box where the shaped cluster already rendered.
            if (glyphId < 0 || glyphId >= 0xFFFE)
                continue;

            int texId = font.getOrCreateGlyphTexture(glyphId);
            int[] m = font.getGlyphMetrics(glyphId);
            int bitmapW = m[1];
            int bitmapH = m[2];
            int xoff = m[3];
            int yoff = m[4];

            double gx = x + (shaped.posX[i] + xoff) * scaleX;
            double gy = baselineY + (shaped.posY[i] + yoff) * scaleY;
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

        return shaped.advance * scaleX;
    }
}
