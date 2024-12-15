package tanks.tankson;

import basewindow.IModel;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * Tanks: The Crusades Object Notation
 * (totally not some other object notation)
 */
public class TanksON
{
    protected static class ParserState
    {
        public String s;
        public int index = 0;
        
        protected ParserState(String s)
        {
            this.s = s;
        }
        
        protected char nextChar()
        {
            return s.charAt(index);
        }

        public boolean tokenMatches(String token)
        {
            if (s.startsWith(token, index))
            {
                index += token.length();
                return true;
            }
            else
                return false;
        }

        public void skipWhitespace()
        {
            while (true)
            {
                if (" \t\n\r".startsWith(this.nextChar() + ""))
                    index++;
                else
                    return;
            }
        }
    }

    public static Object parseObject(String s)
    {
        return parseObject(new ParserState(s));
    }

    public static Object parseObject(ParserState s)
    {
        s.skipWhitespace();
        char next = s.nextChar();
        if (next == '"')
            return parseString(s, false);
        else if (next == '{')
            return parseMap(s);
        else if (next == '[')
            return parseArray(s);
        else if ("1234567890-Ii".indexOf(next) >= 0)
            return parseNumber(s);
        else if (s.tokenMatches("true"))
            return true;
        else if (s.tokenMatches("false"))
            return false;
        else if (s.tokenMatches("null"))
            return null;
        else
            return error("Failed to parse object", s);
    }

    public static double parseNumber(ParserState s)
    {
        s.skipWhitespace();
        int start = s.index;
        while ("+-0123456789.eE".indexOf(s.nextChar()) >= 0)
            s.index++;

        String infinity = "infinity";
        if (s.s.length() - s.index > infinity.length())
            if (s.s.substring(s.index, s.index + infinity.length()).toLowerCase(Locale.ROOT).equals(infinity))
                s.index += infinity.length();

        try
        {
            return Double.parseDouble(s.s.substring(start, s.index));
        }
        catch (Exception e)
        {
            return (double) error(e.toString(), s);
        }
    }

    public static Object parseMap(ParserState s)
    {
        HashMap<String, Object> map = new HashMap<>();

        s.skipWhitespace();
        if (s.nextChar() != '{')
            error("Failed to start parsing map", s);
        s.index++;

        while (true)
        {
            s.skipWhitespace();
            char next = s.nextChar();
            if (next == '}')
            {
                s.index++;
                return map;
            }
            String key;
            if (next != '"')
                key = parseString(s, true);
            else
                key = parseString(s);
            s.skipWhitespace();
            next = s.nextChar();
            if (next != ':' && next != '=')
                error("Failed to parse map key-value", s);
            s.index++;
            s.skipWhitespace();
            map.put(key, parseObject(s));
            s.skipWhitespace();
            if (s.nextChar() == ',')
                s.index++;
            else if (s.nextChar() != '}')
                error("Failed to parse map object", s);
        }
    }

    public static ArrayList<Object> parseArray(ParserState s)
    {
        ArrayList<Object> array = new ArrayList<>();

        s.skipWhitespace();
        if (s.nextChar() != '[')
            error("Failed to start parsing array", s);
        s.index++;

        while (true)
        {
            s.skipWhitespace();
            char next = s.nextChar();
            if (next == ']')
            {
                s.index++;
                return array;
            }
            array.add(parseObject(s));
            s.skipWhitespace();
            if (s.nextChar() == ',')
                s.index++;
            else if (s.nextChar() != ']')
                error("Failed to parse array object", s);
        }
    }

    public static String convertString(String s)
    {
        return s.replace("\\", "\\\\")
                .replace("\n", "\\n")
                .replace("\u00A7", "\\&")
                .replace("\"", "\\\"");
    }

    public static String parseString(ParserState s)
    {
        return parseString(s, false);
    }

    public static String parseString(ParserState s, boolean unquotedKey)
    {
        s.skipWhitespace();
        int start = unquotedKey ? s.index - 1 : s.s.indexOf('"', s.index);
        StringBuilder b = new StringBuilder();
        for (s.index = start + 1;; s.index++)
        {
            if (s.nextChar() == '\\')
            {
                if (s.s.charAt(s.index + 1) == '"')
                    b.append("\"");
                else if (s.s.charAt(s.index + 1) == 'n')
                    b.append('\n');
                else if (s.s.charAt(s.index + 1) == '&')
                    b.append('\u00A7');
                else if (s.s.charAt(s.index + 1) == '\\')
                    b.append("\\");
                else
                    error("Failed to parse escape sequence", s);

                s.index++;
            }
            else if (s.nextChar() != '"' && !(unquotedKey && (s.nextChar() == '=' || s.nextChar() == ':')))
                b.append(s.nextChar());
            else
            {
                if (s.nextChar() == '"')
                   s.index++;
                return b.toString();
            }
        }
    }

    public static String toString(Object o)
    {
        if (o instanceof String || o instanceof Enum || o instanceof IModel)
            return "\"" + convertString(o.toString()) + "\"";
        else if (o instanceof Number)
            return o.toString();
        else if (o instanceof Boolean)
            return o.toString();
        else if (o == null)
            return "null";
        else if (o instanceof AbstractCollection)
        {
            StringBuilder s = new StringBuilder("[");
            for (Object el: (AbstractCollection<?>) o)
            {
                s.append(toString(el)).append(",");
            }

            if (s.charAt(s.length() - 1) == ',')
                s.deleteCharAt(s.length() - 1);

            s.append("]");
            return s.toString();
        }
        else if (o instanceof HashMap)
        {
            StringBuilder s = new StringBuilder("{");
            HashMap<?, ?> h = ((HashMap<?, ?>) o);

            ArrayList<String> keys = new ArrayList<>((Collection<? extends String>) h.keySet());
            if (keys.remove("name"))
                keys.add(0, "name");

            if (keys.remove("obj_type"))
                keys.add(0, "obj_type");

            for (String el: keys)
            {
                s.append("\"").append(convertString(el)).append("\":").append(toString(h.get(el))).append(",");
            }

            if (s.charAt(s.length() - 1) == ',')
                s.deleteCharAt(s.length() - 1);

            s.append("}");
            return s.toString();
        }
        else
            throw new RuntimeException("Failed to turn object to string: " + o);
    }

    public static Object error(String error, ParserState s)
    {
        throw new RuntimeException(error + ": " + s.s.substring(0, s.index) + ">>> Error location <<<" + s.s.substring(s.index));
    }

    public static <A extends Annotation> A getAnnotation(Class<?> target, Class<A> a)
    {
        while (target != null)
        {
            A r = target.getAnnotation(a);

            if (r != null)
                return r;

            target = target.getSuperclass();
        }

        return null;
    }

    public static boolean equals(Object a, Object b)
    {
        if (a == null && b == null)
            return true;
        else
            return a != null && a.equals(b);
    }
}
