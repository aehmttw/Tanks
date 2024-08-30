package tanks.tankson;

import basewindow.IModel;
import tanks.Drawing;
import tanks.Game;
import tanks.bullet.Bullet;
import tanks.item.Item;
import tanks.tank.Explosion;
import tanks.tank.Mine;
import tanks.tank.TankAIControlled;

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

    protected static Object getDefault(Class<?> c) throws InstantiationException, IllegalAccessException
    {
        if (!defaults.containsKey(c))
            defaults.put(c, c.newInstance());

        return defaults.get(c);
    }

    public static Object parseObject(String s)
    {
        return parseObject(s + " ", new int[]{0});
    }

    public static Object parseObject(String s, int[] index)
    {
        skipWhitespace(s, index);
        char next = s.charAt(index[0]);
        if (next == '"')
            return parseString(s, index);
        else if (next == '{')
            return parseMap(s, index);
        else if (next == '[')
            return parseArray(s, index);
        else if ("1234567890-Ii".indexOf(next) >= 0)
            return parseNumber(s, index);
        else if (tokenMatches(s, index, "true"))
            return true;
        else if (tokenMatches(s, index, "false"))
            return false;
        else if (tokenMatches(s, index, "null"))
            return null;
        else
            return error("Failed to parse object", s, index[0]);
    }

    public static double parseNumber(String s, int[] index)
    {
        skipWhitespace(s, index);
        int start = index[0];
        while ("+-0123456789.eE".indexOf(s.charAt(index[0])) >= 0)
            index[0]++;

        String infinity = "infinity";
        if (s.length() - index[0] > infinity.length())
            if (s.substring(index[0], index[0] + infinity.length()).toLowerCase(Locale.ROOT).equals(infinity))
                index[0] += infinity.length();

        try
        {
            return Double.parseDouble(s.substring(start, index[0]));
        }
        catch (Exception e)
        {
            return (double) error(e.toString(), s, index[0]);
        }
    }

    public static Object parseMap(String s, int[] index)
    {
        HashMap<String, Object> map = new HashMap<>();

        skipWhitespace(s, index);
        if (s.charAt(index[0]) != '{')
            error("Failed to start parsing map", s, index[0]);
        index[0]++;

        while (true)
        {
            skipWhitespace(s, index);
            char next = s.charAt(index[0]);
            if (next == '}')
            {
                index[0]++;
                return toTanksONable(map);
            }
            if (next != '"')
                error("Failed to parse map key", s, index[0]);
            String key = parseString(s, index);
            skipWhitespace(s, index);
            next = s.charAt(index[0]);
            if (next != ':' && next != '=')
                error("Failed to parse map key-value", s, index[0]);
            index[0]++;
            skipWhitespace(s, index);
            map.put(key, parseObject(s, index));
            skipWhitespace(s, index);
            if (s.charAt(index[0]) == ',')
                index[0]++;
            else if (s.charAt(index[0]) != '}')
                error("Failed to parse map object", s, index[0]);
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
                    o = Game.registryItem.getEntry(type).item.newInstance();
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

    public static ArrayList<Object> parseArray(String s, int[] index)
    {
        ArrayList<Object> array = new ArrayList<>();

        skipWhitespace(s, index);
        if (s.charAt(index[0]) != '[')
            error("Failed to start parsing array", s, index[0]);
        index[0]++;

        while (true)
        {
            skipWhitespace(s, index);
            char next = s.charAt(index[0]);
            if (next == ']')
            {
                index[0]++;
                return array;
            }
            array.add(parseObject(s, index));
            skipWhitespace(s, index);
            if (s.charAt(index[0]) == ',')
                index[0]++;
            else if (s.charAt(index[0]) != ']')
                error("Failed to parse array object", s, index[0]);
        }
    }

    public static String parseString(String s, int[] index)
    {
        skipWhitespace(s, index);
        int start = s.indexOf('"', index[0]);
        StringBuilder b = new StringBuilder();
        for (index[0] = start + 1;; index[0]++)
        {
            if (s.charAt(index[0]) == '\\')
            {
                if (s.charAt(index[0] + 1) == '"')
                    b.append("\"");
                else if (s.charAt(index[0] + 1) == 'n')
                    b.append('\n');
                else if (s.charAt(index[0] + 1) == '&')
                    b.append('\u00A7');
                else
                    error("Failed to parse escape sequence", s, index[0]);

                index[0]++;
            }
            else if (s.charAt(index[0]) != '"')
                b.append(s.charAt(index[0]));
            else
            {
                index[0]++;
                return b.toString();
            }
        }
    }

    public static void skipWhitespace(String s, int[] index)
    {
        while (true)
        {
            char c = s.charAt(index[0]);
            if (" \t\n\r".startsWith(c + ""))
                index[0]++;
            else
                return;
        }
    }

    public static boolean tokenMatches(String s, int[] index, String token)
    {
        if (s.startsWith(token, index[0]))
        {
            index[0] += token.length();
            return true;
        }
        else
            return false;
    }

    public static String objectToString(Object o)
    {
        if (o instanceof String || o instanceof Enum || o instanceof IModel)
            return "\"" + o.toString() + "\"";
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
            s.append("]");
            return s.toString();
        }
        else if (o instanceof HashMap)
        {
            StringBuilder s = new StringBuilder("{");
            HashMap<?, ?> h = ((HashMap<?, ?>) o);

            ArrayList<String> keys = new ArrayList<String>((Collection<? extends String>) h.keySet());
            if (keys.contains("name"))
            {
                keys.remove("name");
                keys.add(0, "name");
            }

            if (keys.contains("obj_type"))
            {
                keys.remove("obj_type");
                keys.add(0, "obj_type");
            }

            for (String el: keys)
            {
                s.append("\"").append(el.toString()).append("\":").append(objectToString(h.get(el))).append(",");
            }
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

    public static Object error(String error, String s, int index)
    {
        throw new RuntimeException(error + ": " + s.substring(0, index) + ">>> Error location <<<" + s.substring(index));
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
