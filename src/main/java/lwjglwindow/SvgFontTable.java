package lwjglwindow;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Reads an OpenType {@code SVG } table (color emoji rendered as per-glyph SVG documents) out of a
 * font's raw byte buffer, and produces a small, self-contained SVG string for a single glyph that
 * {@code svgSalamander} can rasterize.
 *
 * <p>Why we build a per-glyph SVG rather than render the source document directly: a single SVG
 * document in the table can cover thousands of glyph ids (Noto's main document is ~14&nbsp;MB and
 * holds ~2700 glyphs). Every glyph's artwork lives in a {@code <g id="glyphN">} group that pulls
 * shared geometry out of one top-level {@code <defs>} via {@code <use xlink:href="#id">} and
 * {@code fill="url(#id)"}. Parsing the whole document per glyph would be far too slow and
 * memory-hungry, so instead we extract just the target group plus the {@code <defs>} elements it
 * transitively references and wrap them in a minimal {@code <svg>}.
 *
 * <p>STB and AWT don't expose this table, so we parse the sfnt table directory ourselves.
 */
public class SvgFontTable
{
    private final ByteBuffer buffer;
    private final int unitsPerEm;

    // Parallel arrays of SVG document records, sorted by startGlyph. docStart/docLen are absolute
    // positions within `buffer`.
    private final int[] startGlyph;
    private final int[] endGlyph;
    private final int[] docStart;
    private final int[] docLen;

    // Lazily-decoded document text, and a lazily-built id -> <defs> element index, keyed by record.
    private final Map<Integer, String> docText = new HashMap<>();
    private final Map<Integer, Map<String, String>> docDefs = new HashMap<>();

    private static final Pattern REF_PATTERN =
        Pattern.compile("(?:xlink:)?href=\"#([^\"]+)\"|url\\(#([^)]+)\\)");

    private SvgFontTable(ByteBuffer buffer, int unitsPerEm, int[] startGlyph, int[] endGlyph, int[] docStart, int[] docLen)
    {
        this.buffer = buffer;
        this.unitsPerEm = unitsPerEm;
        this.startGlyph = startGlyph;
        this.endGlyph = endGlyph;
        this.docStart = docStart;
        this.docLen = docLen;
    }

    /**
     * Parses the {@code SVG } table from a font, or returns null if the font has no usable one.
     *
     * @param fontOffset table-directory offset for this font within {@code buffer} (0 for a plain
     *                   .ttf, the {@code stbtt_GetFontOffsetForIndex} value for a .ttc member).
     */
    public static SvgFontTable tryParse(ByteBuffer buffer, int fontOffset)
    {
        try
        {
            int numTables = u16(buffer, fontOffset + 4);
            int svgOffset = -1;
            int headOffset = -1;
            for (int i = 0; i < numTables; i++)
            {
                int rec = fontOffset + 12 + i * 16;
                int tag = u32(buffer, rec);
                if (tag == 0x53564720) // 'SVG '
                    svgOffset = u32(buffer, rec + 8);
                else if (tag == 0x68656164) // 'head'
                    headOffset = u32(buffer, rec + 8);
            }

            if (svgOffset < 0)
                return null;

            int unitsPerEm = (headOffset >= 0) ? u16(buffer, headOffset + 18) : 1000;
            if (unitsPerEm <= 0)
                unitsPerEm = 1000;

            // SVG table header: version(u16), svgDocumentListOffset(u32), reserved(u32).
            int listOffset = svgOffset + u32(buffer, svgOffset + 2);
            int numEntries = u16(buffer, listOffset);

            int[] sg = new int[numEntries];
            int[] eg = new int[numEntries];
            int[] ds = new int[numEntries];
            int[] dl = new int[numEntries];
            for (int i = 0; i < numEntries; i++)
            {
                int rec = listOffset + 2 + i * 12;
                sg[i] = u16(buffer, rec);
                eg[i] = u16(buffer, rec + 2);
                ds[i] = listOffset + u32(buffer, rec + 4); // docOffset is relative to the list
                dl[i] = u32(buffer, rec + 8);
            }

            return new SvgFontTable(buffer, unitsPerEm, sg, eg, ds, dl);
        }
        catch (Exception e)
        {
            System.err.println("SvgFontTable: failed to parse 'SVG ' table: " + e.getMessage());
            return null;
        }
    }

    public int getUnitsPerEm()
    {
        return unitsPerEm;
    }

    /** Index of the document record covering {@code glyphId}, or -1. */
    private int findRecord(int glyphId)
    {
        for (int i = 0; i < startGlyph.length; i++)
            if (glyphId >= startGlyph[i] && glyphId <= endGlyph[i])
                return i;
        return -1;
    }

    public boolean hasGlyph(int glyphId)
    {
        return findRecord(glyphId) >= 0;
    }

    /**
     * Builds the inner SVG markup for {@code glyphId} — its {@code <g id="glyphN">} group plus the
     * transitively-referenced {@code <defs>} elements — or null if there is no SVG artwork for this
     * glyph. The caller wraps this in an {@code <svg>} with an explicit {@code width}/{@code height}/
     * {@code viewBox} (see the note in the renderer): the artwork is in font design units with a
     * y-down, baseline-origin coordinate system and carries no viewport of its own.
     */
    public String buildGlyphContent(int glyphId)
    {
        int rec = findRecord(glyphId);
        if (rec < 0)
            return null;

        String doc = getDoc(rec);
        if (doc == null)
            return null;

        String group = extractElement(doc, "glyph" + glyphId);
        if (group == null)
            return null;

        Map<String, String> defs = getDefs(rec, doc);

        // Transitively collect referenced def ids, starting from the group.
        Set<String> needed = new LinkedHashSet<>();
        ArrayDeque<String> queue = new ArrayDeque<>();
        collectRefs(group, queue, needed);
        while (!queue.isEmpty())
        {
            String id = queue.poll();
            String el = defs.get(id);
            if (el != null)
                collectRefs(el, queue, needed);
        }

        StringBuilder sb = new StringBuilder(group.length() + 256);
        if (!needed.isEmpty())
        {
            sb.append("<defs>");
            for (String id : needed)
            {
                String el = defs.get(id);
                if (el != null)
                    sb.append(el);
            }
            sb.append("</defs>");
        }
        sb.append(group);
        return sb.toString();
    }

    /** Adds every {@code #id} referenced by {@code element} (via href or url()) to the work set. */
    private static void collectRefs(String element, ArrayDeque<String> queue, Set<String> needed)
    {
        Matcher m = REF_PATTERN.matcher(element);
        while (m.find())
        {
            String id = m.group(1) != null ? m.group(1) : m.group(2);
            if (needed.add(id))
                queue.add(id);
        }
    }

    private String getDoc(int rec)
    {
        String cached = docText.get(rec);
        if (cached != null)
            return cached;

        byte[] bytes = new byte[docLen[rec]];
        int pos = buffer.position();
        buffer.position(docStart[rec]);
        buffer.get(bytes);
        buffer.position(pos);

        String s;
        try
        {
            if (bytes.length >= 2 && (bytes[0] & 0xFF) == 0x1F && (bytes[1] & 0xFF) == 0x8B)
                s = new String(gunzip(bytes), StandardCharsets.UTF_8);
            else
                s = new String(bytes, StandardCharsets.UTF_8);
        }
        catch (Exception e)
        {
            System.err.println("SvgFontTable: failed to read SVG document: " + e.getMessage());
            return null;
        }

        docText.put(rec, s);
        return s;
    }

    /** Builds (once per document) an id -> element-text index over the document's {@code <defs>}. */
    private Map<String, String> getDefs(int rec, String doc)
    {
        Map<String, String> cached = docDefs.get(rec);
        if (cached != null)
            return cached;

        Map<String, String> index = new HashMap<>();
        int ds = doc.indexOf("<defs>");
        int de = doc.indexOf("</defs>");
        if (ds >= 0 && de > ds)
            indexElements(doc, ds + 6, de, index);

        docDefs.put(rec, index);
        return index;
    }

    /**
     * Walks the top-level elements in {@code [from, to)} and maps each {@code id} to its full source
     * text. The Noto {@code <defs>} contains only self-closing {@code <path/>} and gradient
     * elements (no nesting), so a linear scan matching self-closing {@code />} or a same-tag close
     * is sufficient.
     */
    private static void indexElements(String s, int from, int to, Map<String, String> index)
    {
        int i = from;
        while (i < to)
        {
            int lt = s.indexOf('<', i);
            if (lt < 0 || lt >= to)
                break;

            int tagEnd = lt + 1;
            while (tagEnd < to && isTagNameChar(s.charAt(tagEnd)))
                tagEnd++;
            String tag = s.substring(lt + 1, tagEnd);

            int gt = s.indexOf('>', tagEnd);
            if (gt < 0 || gt >= to)
                break;

            int end;
            if (s.charAt(gt - 1) == '/')
            {
                end = gt + 1; // self-closing element
            }
            else
            {
                int close = s.indexOf("</" + tag, gt);
                if (close < 0)
                    break;
                end = s.indexOf('>', close) + 1;
            }

            String element = s.substring(lt, end);
            String id = attr(element, "id");
            if (id != null)
                index.put(id, element);

            i = end;
        }
    }

    /**
     * Extracts the {@code <g id="targetId">...</g>} element (with balanced nesting) from {@code doc},
     * or null if absent.
     */
    private static String extractElement(String doc, String targetId)
    {
        String marker = "id=\"" + targetId + "\"";
        int idPos = doc.indexOf(marker);
        if (idPos < 0)
            return null;

        int start = doc.lastIndexOf('<', idPos);
        if (start < 0)
            return null;

        int tagEnd = start + 1;
        while (tagEnd < doc.length() && isTagNameChar(doc.charAt(tagEnd)))
            tagEnd++;
        String tag = doc.substring(start + 1, tagEnd);

        int gt = doc.indexOf('>', idPos);
        if (gt < 0)
            return null;
        if (doc.charAt(gt - 1) == '/')
            return doc.substring(start, gt + 1); // self-closing

        // Balance nested same-tag opens until the matching close.
        String openTag = "<" + tag;
        String closeTag = "</" + tag;
        int depth = 1;
        int i = gt + 1;
        while (i < doc.length() && depth > 0)
        {
            int nextOpen = doc.indexOf(openTag, i);
            int nextClose = doc.indexOf(closeTag, i);
            if (nextClose < 0)
                return null;
            if (nextOpen >= 0 && nextOpen < nextClose)
            {
                // Only count as a nested open if it's a real tag boundary (followed by space or >).
                char after = doc.charAt(nextOpen + openTag.length());
                if (after == ' ' || after == '>' || after == '\t' || after == '\n')
                    depth++;
                i = nextOpen + openTag.length();
            }
            else
            {
                depth--;
                i = nextClose + closeTag.length();
                if (depth == 0)
                    return doc.substring(start, doc.indexOf('>', i) + 1);
            }
        }
        return null;
    }

    private static String attr(String element, String name)
    {
        String key = name + "=\"";
        int p = element.indexOf(key);
        if (p < 0)
            return null;
        int start = p + key.length();
        int end = element.indexOf('"', start);
        return end < 0 ? null : element.substring(start, end);
    }

    private static boolean isTagNameChar(char c)
    {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '-' || c == '_' || c == ':';
    }

    // sfnt/OpenType data is big-endian. Read it explicitly rather than via ByteBuffer's order,
    // because the font buffer comes from LWJGL's BufferUtils, which uses native (little-endian) order.
    private static int u16(ByteBuffer b, int pos)
    {
        return ((b.get(pos) & 0xFF) << 8) | (b.get(pos + 1) & 0xFF);
    }

    private static int u32(ByteBuffer b, int pos)
    {
        return ((b.get(pos) & 0xFF) << 24) | ((b.get(pos + 1) & 0xFF) << 16)
            | ((b.get(pos + 2) & 0xFF) << 8) | (b.get(pos + 3) & 0xFF);
    }

    private static byte[] gunzip(byte[] data) throws Exception
    {
        try (InputStream in = new GZIPInputStream(new ByteArrayInputStream(data)))
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream(data.length * 4);
            byte[] chunk = new byte[8192];
            int n;
            while ((n = in.read(chunk)) > 0)
                out.write(chunk, 0, n);
            return out.toByteArray();
        }
    }
}
