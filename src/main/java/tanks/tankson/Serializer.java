package tanks.tankson;

import tanks.Game;
import tanks.bullet.Bullet;
import tanks.item.Item;
import tanks.tank.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

public final class Serializer
{

    public static HashMap<Class<?>, Object> defaults = new HashMap<>();

    public static HashMap<String, Tank> userTanks = new HashMap<>();

    public static Class<?> getCorrectClass(Object o)
    {
        if (o instanceof TankAIControlled)
        {
            return TankAIControlled.class;
        }
        else
        {
            return o.getClass();
        }
    }

    public static Object getDefault(Class<?> c)
    {
        if (defaults.containsKey(c))
        {
            return defaults.get(c);
        }
        else
        {
            try
            {
                Object o = c.getConstructor().newInstance();
                defaults.put(c, o);
                return o;
            }
            catch (Exception ignore)
            {
                return null;
            }
        }
    }

    public static boolean isTanksONable(Object o)
    {
        return isTanksONable(o.getClass());
    }

    public static boolean isTanksONable(Field f)
    {
        if (f == null)
            return false;
        return isTanksONable(f.getType());
    }

    public static boolean isTanksONable(Class c) {
        while (c != null && !c.isAnnotationPresent(TanksONable.class))
            c = c.getSuperclass();
        return c != null;
    }

    public static <A extends Annotation> A getAnnotation(Object o, Class<A> a)
    {
        Class<?> target = o.getClass();
        while (target != null)
        {
            A r = target.getAnnotation(a);

            if (r != null)
                return r;

            target = target.getSuperclass();
        }

        return null;
    }

    public static String getid(Field f)
    {
        return f.getAnnotation(Property.class).id();
    }

    public static Map<String, Object> toMap(Object o)
    {
        if (isTanksONable(o))
        {
            HashMap<String, Object> p = new HashMap<>();
            p.put("obj_type", getAnnotation(o, TanksONable.class).value());
            if (o instanceof Item)
                try
                {
                    p.put("item_type", ((Item) o).getClass().getField("item_class_name").get(null));
                }
                catch (Exception ignore)
                {
                }
            else if (o instanceof Bullet)
                p.put("bullet_type", ((Bullet) o).typeName);


            for (Field f : o.getClass().getFields())
            {
                try
                {
                    if (f.isAnnotationPresent(Property.class) && (!(o instanceof Tank || o instanceof Bullet || o instanceof Mine || o instanceof Explosion) || !Objects.equals(f.get(getDefault(getCorrectClass(o))), f.get(o))))
                    {
                        Object o2 = f.get(o);
                        if (o2 != null && isTanksONable(f))
                        {
                            p.put(getid(f), toMap(o2));
                        }
                        else if (o2 instanceof ArrayList)
                        {
                            if (!((ArrayList) o2).isEmpty() && isTanksONable(((ArrayList) o2).get(0)))
                            {
                                ArrayList<Map<String, Object>> o3s = new ArrayList<>();
                                for (Object o3 : ((ArrayList) o2))
                                {
                                    o3s.add(toMap(o3));
                                }
                                p.put(getid(f), o3s);
                            }
                            else
                            {
                                p.put(getid(f), f.get(o));
                            }
                        }
                        else if (o2 instanceof Enum)
                            p.put(getid(f), ((Enum) o2).name());
                        else if (o2 instanceof Serializable)
                            p.put(getid(f), ((Serializable) o2).serialize());
                        else
                            p.put(getid(f), f.get(o));
                    }
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
            return p;
        }
        return null;
    }

    public static String toTanksON(Object o)
    {
        return TanksON.toString(toMap(o));
    }

    public static Object fromTanksON(String s)
    {
        Object o = TanksON.parseObject(s);
        if (o instanceof Map)
            return parseObject((Map<String, Object>) o);
        else
            throw new RuntimeException("Unexpected type of object: " + o.toString());
    }

    public static Object parseObject(Map<String, Object> m)
    {
        if (m == null)
            return null;
        Object o = null;
        Set<String> processed = new HashSet<>();
        processed.add("obj_type");
        switch ((String) m.get("obj_type"))
        {
            case "tank":
                o = new TankAIControlled("", 0, 0, 50, 0, 0, 0, 0, TankAIControlled.ShootAI.none);
                break;
            case "player_tank":
                o = new TankPlayer();
                break;
            case "bullet":
            {
                try
                {
                    processed.add("bullet_type");
                    o = Game.registryBullet.getEntry((String) m.get("bullet_type")).bullet.newInstance();
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
                break;
            }
            case "mine":
                o = new Mine();
                break;
            case "item":
            {
                try
                {
                    processed.add("item_type");
                    o = Game.registryItem.getEntry((String) m.get("item_type")).item.getConstructor().newInstance();
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
                break;
            }
            case "item_stack":
            {
                processed.add("item");
                Item i = (Item) parseObject((Map) m.get("item"));
                o = (i.getStack(null));
                break;
            }
            case "shop_item":
                o = new Item.ShopItem();
                break;
            case "crusade_shop_item":
                o = new Item.CrusadeShopItem();
                break;
            case "shop_build":
                o = new TankPlayer.ShopTankBuild();
                break;
            case "crusade_shop_build":
                o = new TankPlayer.CrusadeShopTankBuild();
                break;
            case "explosion":
                o = new Explosion();
                break;
            case "spawned_tank":
                processed.add("tank");
                processed.add("weight");
                o = new TankAIControlled.SpawnedTankEntry((ITankField) parseObject((Map) m.get("tank")), (Double) m.get("weight"));
                break;
            case "tank_ref": {
                processed.add("tank");
                o = new TankReference((String) m.get("tank"));
                break;
            }
            default:
                throw new RuntimeException("Bad object type: " + (String) m.get("obj_type"));
        }

        for (Field f : o.getClass().getFields())
        {
            if (f.isAnnotationPresent(Property.class) && m.containsKey(getid(f)))
            {
                processed.add(getid(f));
                try
                {
                    Object o2 = f.get(o);
                    if (isTanksONable(f))
                    {
                        Object o3 = m.get(getid(f));
                        try {
                            f.set(o, parseObject((Map<String, Object>) o3));
                        } catch (ClassCastException e) {
                            f.set(o, Compatibility.convert(f, o3));
                        }
                    }
                    else if (o2 instanceof ArrayList)
                    {
                        ArrayList arr = (ArrayList) m.get(getid(f));
                        if (!arr.isEmpty() && (arr.get(0) instanceof Map))
                        {
                            ArrayList o3s = new ArrayList();
                            for (Map o3 : ((ArrayList<Map>) m.get(getid(f))))
                            {
                                o3s.add(parseObject(o3));
                            }
                            f.set(o, o3s);
                        }
                        else
                            f.set(o, m.get(getid(f)));
                    }
                    else if (o2 instanceof HashSet)
                        f.set(o, new HashSet<>((ArrayList) m.get(getid(f))));
                    else if (o2 instanceof Enum)
                        f.set(o, Enum.valueOf((Class<? extends Enum>) f.getType(), (String) m.get(getid(f))));
                    else if (o2 instanceof Serializable)
                        f.set(o, ((Serializable) o2).deserialize((String) m.get(getid(f))));
                    else if (o2 instanceof Integer)
                        f.set(o, ((Double) m.get(getid(f))).intValue());
                    else if (o2 instanceof Boolean)
                        f.set(o, m.get(getid(f)));
                    else
                        f.set(o, m.get(getid(f)));
                }
                catch (Exception e)
                {
                    System.out.println(getid(f));
                    throw new RuntimeException(e);
                }
            }
        }

        Set<String> unused = new HashSet<>(m.keySet());
        unused.removeAll(processed);
        for (String k : unused) {
            try {
                o.getClass().getField(Compatibility.convert(k)).set(o, m.get(k));
            } catch (ClassCastException e) {
                try {
                    Field f = o.getClass().getField(Compatibility.convert(k));
                    f.set(o, Compatibility.convert(f, m.get(k)));
                } catch (NoSuchFieldException | IllegalAccessException f) {
                    throw new RuntimeException(f);
                }
            } catch (NoSuchFieldException | NullPointerException | IllegalAccessException e) {
                System.out.println("Unconvertable field found!");
            }
        }


        return o;
    }

}