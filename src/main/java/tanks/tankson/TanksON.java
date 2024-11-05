package tanks.tankson;

import basewindow.IModel;
import tanks.Drawing;
import tanks.Game;
import tanks.bullet.Bullet;
import tanks.item.Item;
import tanks.tank.Explosion;
import tanks.tank.Mine;
import tanks.tank.TankAIControlled;
import tanks.tank.TankReference;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Tanks: The Crusades Object Notation
 * (totally not some other object notation)
 */
public class TanksON
{
    protected static TankAIControlled defaultTank = new TankAIControlled("", 0, 0, 50, 0, 0, 0, 0, TankAIControlled.ShootAI.none);
    protected static HashMap<Class<?>, Object> defaults = new HashMap<>();

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

    protected static Object getDefault(Class<?> c) throws InstantiationException, IllegalAccessException
    {
        if (!defaults.containsKey(c))
            defaults.put(c, c.newInstance());

        return defaults.get(c);
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
                return toTanksONable(map);
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

    public static Object toTanksONable(HashMap<String, Object> map)
    {
        Object o;

        try
        {
            if (!map.containsKey("obj_type"))
                return map;

            String name = (String) map.get("obj_type");

            switch (name)
            {
                case "tank":
                    o = new TankAIControlled("", 0, 0, 50, 0, 0, 0, 0, TankAIControlled.ShootAI.none);
                    break;
                case "tank_ref":
                    o = new TankReference("");
                    break;
                case "bullet":
                {
                    String type = (String) map.get("bullet_type");
                    o = Game.registryBullet.getEntry(type).bullet.newInstance();
                    break;
                }
                case "mine":
                    o = new Mine();
                    break;
                case "item":
                {
                    String type = (String) map.get("item_type");
                    o = Game.registryItem.getEntry(type).item.getConstructor().newInstance();
                    break;
                }
                case "item_stack":
                {
                    o = ((Item) map.get("item")).getStack(null);
                    break;
                }
                case "shop_item":
                {
                    o = new Item.ShopItem();
                    break;
                }
                case "crusade_shop_item":
                {
                    o = new Item.CrusadeShopItem();
                    break;
                }
                case "explosion":
                {
                    o = new Explosion();
                    break;
                }
                case "spawned_tank":
                {
                    o = new TankAIControlled.SpawnedTankEntry(null, 0);
                    break;
                }
                default:
                    throw new RuntimeException("Bad object type: " + name);
            }

            for (Field f: o.getClass().getFields())
            {
                String id = null;

                Property i = f.getAnnotation(Property.class);
                if (i != null)
                    id = i.id();

                if (id != null && map.containsKey(id))
                {
                    if (HashSet.class.isAssignableFrom(f.getType()))
                        f.set(o, new HashSet<>((ArrayList<?>) map.get(id)));
                    else if (IModel.class.isAssignableFrom(f.getType()) && map.get(id) != null)
                        f.set(o, Drawing.drawing.createModel((String) map.get(id)));
                    else if (f.getType().isEnum())
                        f.set(o, Enum.valueOf((Class<? extends Enum>) f.getType(), (String) map.get(id)));
                    else if (f.getType() == int.class)
                        f.set(o, (int) Math.max(i.minValue(), Math.min(i.maxValue(), ((Double) map.get(id)).intValue())));
                    else if (f.getType() == double.class)
                        f.set(o, Math.max(i.minValue(), Math.min(i.maxValue(), ((double) map.get(id)))));
                    else
                        f.set(o, map.get(id));
                }
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        return o;
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

    public static String objectToString(Object o)
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
                s.append(objectToString(el)).append(",");
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

            ArrayList<String> keys = new ArrayList<String>((Collection<? extends String>) h.keySet());
            if (keys.remove("name"))
                keys.add(0, "name");

            if (keys.remove("obj_type"))
                keys.add(0, "obj_type");

            for (String el: keys)
            {
                s.append("\"").append(convertString(el)).append("\":").append(objectToString(h.get(el))).append(",");
            }

            if (s.charAt(s.length() - 1) == ',')
                s.deleteCharAt(s.length() - 1);

            s.append("}");
            return s.toString();
        }
        else if (getAnnotation(o.getClass(), TanksONable.class) != null)
        {
            try
            {
                HashMap<String, Object> h = new HashMap<>();

                h.put("obj_type", getAnnotation(o.getClass(), TanksONable.class).value());
                if (o instanceof Bullet)
                    h.put("bullet_type", ((Bullet) o).typeName);
                else if (o instanceof Item)
                    h.put("item_type", ((Item) o).getClass().getField("item_class_name").get(null));

                for (Field f : o.getClass().getFields())
                {
                    String id = null;

                    Property i = f.getAnnotation(Property.class);
                    if (i != null)
                        id = i.id();

                    if (o instanceof TankAIControlled && equals(f.get(o), f.get(defaultTank)))
                        id = null;
                    else if ((o instanceof Bullet || o instanceof Mine) && equals(f.get(o), f.get(getDefault(o.getClass()))))
                        id = null;

                    if (id != null)
                        h.put(id, f.get(o));
                }
                return objectToString(h);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
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
