package tanks.tankson;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Binary storage for the TanksON objects embedded in level, crusade, tank and item text.
 * Surrounding level syntax and section headers stay literal. Objects retain their original TanksON
 * version marker, but are written as maps so their property ids can be shared across the document.
 */
public final class TanksONDocument
{
    private static final Pattern OBJECT_START = Pattern.compile(
        "/\\*TANKSON v([^*\\r\\n]+)\\*/|^(?:\\[[^\\]\\r\\n]*\\])?[ \\t]*(?=\\{[ \\t]*\"obj_type\"\\s*:)", Pattern.MULTILINE);

    private TanksONDocument()
    {
    }

    /** Returns null when the document has no supported objects. */
    public static byte[] encode(String text)
    {
        Object value = toValue(text);
        return value == null ? null : ObjectBuffer.toBytes(value);
    }

    /** Parsed document parts can be nested directly in an event ObjectBuffer. */
    public static Object toValue(String text)
    {
        ArrayList<Object> parts = new ArrayList<>();
        int copied = 0;
        int search = 0;
        Matcher marker = OBJECT_START.matcher(text);
        while (marker.find(search))
        {
            int objectStart = marker.end();
            search = Math.max(marker.end(), marker.start() + 1);
            String version = marker.group(1);
            // Preserve the original version marker (or its absence for 1.0), without schema conversion.
            if (version != null && !version.equals("1.0") && !version.equals(Serializer.TANKSON_VERSION))
                continue;

            TanksON.ParserState state = new TanksON.ParserState(text);
            state.index = objectStart;
            Object value;
            try
            {
                value = TanksON.parseObject(state);
            }
            catch (RuntimeException e)
            {
                continue;
            }

            if (!(value instanceof Map) || !ObjectBuffer.isObject((Map<?, ?>) value))
                continue;

            parts.add(text.substring(copied, objectStart));
            parts.add(compactNumbers(value));
            copied = state.index;
            search = state.index;
        }

        if (parts.isEmpty())
            return null;

        parts.add(text.substring(copied));
        return parts;
    }

    public static String decode(byte[] bytes)
    {
        return fromValue(ObjectBuffer.parse(bytes));
    }

    public static String fromValue(Object value)
    {
        if (!(value instanceof List))
            throw new IllegalArgumentException("Invalid ObjectBuffer document");

        StringBuilder text = new StringBuilder();
        for (Object part: (List<?>) value)
        {
            if (part instanceof String)
                text.append((String) part);
            else if (part instanceof Map && ObjectBuffer.isObject((Map<?, ?>) part))
                text.append(TanksON.toString(part));
            else
                throw new IllegalArgumentException("Invalid ObjectBuffer document part");
        }
        return text.toString();
    }

    /** Text parsing produces doubles; exact small integers take fewer bytes as zigzag varints. */
    private static Object compactNumbers(Object value)
    {
        if (value instanceof Double)
        {
            double d = (double) value;
            if (d >= Integer.MIN_VALUE && d <= Integer.MAX_VALUE && d == (int) d &&
                Double.doubleToRawLongBits(d) != Double.doubleToRawLongBits(-0.0))
                return (int) d;
        }
        else if (value instanceof Map)
        {
            Map<?, ?> map = (Map<?, ?>) value;
            HashMap<Object, Object> compact = new HashMap<>();
            for (Map.Entry<?, ?> entry: map.entrySet())
                compact.put(entry.getKey(), compactNumbers(entry.getValue()));
            return compact;
        }
        else if (value instanceof List)
        {
            ArrayList<Object> compact = new ArrayList<>();
            for (Object item: (List<?>) value)
                compact.add(compactNumbers(item));
            return compact;
        }
        return value;
    }
}
