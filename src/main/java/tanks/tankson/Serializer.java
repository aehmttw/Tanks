package tanks.tankson;

import tanks.Game;
import tanks.item.Item;
import tanks.tank.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public final class Serializer {

    public static HashMap<String, Tank>userTanks = new HashMap<>();

    public static boolean isTanksONable(Object o)
    {
        if (o != null) {
            Class c = o.getClass();
            while (c != null) {
                if (c.isAnnotationPresent(TanksONable.class))
                    return true;
                else
                    c = c.getSuperclass();
            }
        }
        return false;
    }

    public static boolean isTanksONable(Field f)
    {
        if (f != null) {
            Class c = f.getType();
            while (c != null) {
                if (c.isAnnotationPresent(TanksONable.class))
                    return true;
                else
                    c = c.getSuperclass();
            }
        }
        return false;
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

    public static String getid(Field f) {
        return f.getAnnotation(Property.class).id();
    }

    public static Map<String, Object> toMap(Object o)
    {
        if (isTanksONable(o)) {
            HashMap<String, Object> p = new HashMap<>();
            p.put("obj_type", getAnnotation(o, TanksONable.class).value());
            for (Field f : o.getClass().getFields()){
                if (f.isAnnotationPresent(Property.class)) {
                    try {
                        Object o2 = f.get(o);
                        if (o2 instanceof Tank) {
                            if (((Tank) o2).fromRegistry) {
                                //Link all default tanks by object
                                p.put(getid(f), "<" + ((Tank) o2).name + ">");
                            } else {
                                //Link all User Tanks at some point (awaiting thinking)
                                p.put(getid(f), toMap(o2));
                            }
                        }else if (isTanksONable(f)) {
                            p.put(getid(f), toMap(o2));
                        } else if (o2 instanceof ArrayList) {
                            if (!((ArrayList) o2).isEmpty() && isTanksONable(((ArrayList) o2).get(0))){
                                ArrayList<Map<String,Object>> o3s = new ArrayList<>();
                                for (Object o3 : ((ArrayList) o2)) {
                                    o3s.add(toMap(o3));
                                }
                                p.put(getid(f), o3s);
                            } else {
                                p.put(getid(f), f.get(o));
                            }
                        } else if (o2 instanceof Enum) {
                            p.put(getid(f), ((Enum) o2).name());
                        } else if (o2 instanceof Serializable) {
                            p.put(getid(f), ((Serializable) o2).serialize());
                        } else {
                            p.put(getid(f), f.get(o));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
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
        return parseObject((Map<String,Object>)TanksON.parseObject(s));
    }

    public static Object parseObject(Map<String, Object> m)
    {
        if (m == null) {
            return null;
        }
        Object o = null;
        switch ((String) m.get("obj_type"))
        {
            case "tank":
                o = new TankAIControlled("", 0, 0, 50, 0, 0, 0, 0, TankAIControlled.ShootAI.none);
                break;
            case "bullet":
            {
                try {
                    o = Game.registryBullet.getEntry((String) m.get("bullet_type")).bullet.newInstance();
                } catch (Exception ignore){}
                break;
            }
            case "mine":
                o = new Mine();
                break;
            case "item":
            {
                try {
                    o = Game.registryItem.getEntry((String) m.get("item_type")).item.getConstructor().newInstance();
                } catch (Exception ignore){}
                break;
            }
            case "item_stack":
                o = ((Item) m.get("item")).getStack(null);
                break;
            case "shop_item":
                o = new Item.ShopItem();
                break;
            case "crusade_shop_item":
                o = new Item.CrusadeShopItem();
                break;
            case "explosion":
                o = new Explosion();
                break;
            case "spawned_tank":
                o = new TankAIControlled.SpawnedTankEntry((TankReference) parseObject((Map) m.get("tank")), (Double) m.get("weight"));
                break;
            case "tank_ref":
                o = new TankReference((String) m.get("tank"));
                break;
            default:
                throw new RuntimeException("Bad object type: " + (String) m.get("obj_type"));
        }
        for (Field f : o.getClass().getFields())
        {
            if (f.isAnnotationPresent(Property.class))
            {
                try
                {
                    Object o2 = f.get(o);
                    if (isTanksONable(f))
                    {
                        Object o3 = m.get(getid(f));
                        f.set(o, parseObject((Map) o3));
                    }
                    else if (o2 instanceof ArrayList)
                    {
                        if (((ArrayList) m.get(getid(f))).get(0) instanceof Map)
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
                catch (Exception ignore) {}
            }
        }
        return o;
    }

}