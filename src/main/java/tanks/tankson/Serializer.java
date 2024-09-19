package tanks.tankson;

import basewindow.Model;
import com.google.gson.Gson;
import tanks.Game;
import tanks.item.Item;
import tanks.tank.*;
import tanks.bullet.Bullet;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class Serializer {

    public static HashMap<String, Tank>userTanks = new HashMap<>();

    //Debugging GSON
    private static final Gson gson = new Gson();

    public static boolean isTanksONable(Object o) {
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

    public static boolean isTanksONable(Field f) {
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

    public static Map<String, Object> toMap(Object o) {
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

    public static String toJson(Object o) {
        return gson.toJsonTree(toMap(o)).toString();
    }

    public static Object fromJson(String s) {
        return parseObject(gson.fromJson(s,Map.class));
    }

    public static Object parseObject(Map m) {
        Object o = null;
        switch ((String) m.get("obj_type")) {
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
            {
                o = ((Item) m.get("item")).getStack(null);
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
                throw new RuntimeException("Bad object type: " + (String) m.get("obj_type"));
        }
        for (Field f : o.getClass().getFields()){
            if (f.isAnnotationPresent(Property.class)) {
                try {
                    Object o2 = f.get(o);
                    if (isTanksONable(f)) {
                        Object o3 = m.get(getid(f));
                        if (o3 instanceof String) {
                            f.set(o, include((String) o3));
                        } else {
                            f.set(o, parseObject((Map) o3));
                        }
                    } else if (o2 instanceof ArrayList) {
                        if (((ArrayList) m.get(getid(f))).get(0) instanceof Map){
                            ArrayList o3s = new ArrayList();
                            for (Map o3 : ((ArrayList<Map>) m.get(getid(f)))) {
                                o3s.add(parseObject(o3));
                            }
                            f.set(o,o3s);
                        } else {
                            f.set(o,m.get(getid(f)));
                        }
                    } else if (o2 instanceof Enum) {
                        f.set(o,Enum.valueOf((Class<? extends Enum>) f.getType(), (String) m.get(getid(f))));
                    } else if (o2 instanceof Serializable) {
                        f.set(o,((Serializable) o2).deserialize((String) m.get(getid(f))));
                    } else {
                        f.set(o,m.get(getid(f)));
                    }
                } catch (Exception ignore) {
                }
            }
        }
        return o;
    }

    public static Object include(String s) {
        Object o = null;
        if (s.startsWith("<") && s.endsWith(">")) {
            s = s.replace("<","").replace(">","");
            switch (s) {
                case ("dummy"):
                    o = (Object) new TankDummy(s,0,0,0);
                    break;
                case ("brown"):
                    o = (Object) new TankBrown(s,0,0,0);
                    break;
                case ("gray"):
                    o = (Object) new TankGray(s,0,0,0);
                    break;
                case ("mint"):
                    o = (Object) new TankMint(s,0,0,0);
                    break;
                case ("yellow"):
                    o = (Object) new TankYellow(s,0,0,0);
                    break;
                case ("magenta"):
                    o = (Object) new TankMagenta(s,0,0,0);
                    break;
                case ("red"):
                    o = (Object) new TankRed(s,0,0,0);
                    break;
                case ("green"):
                    o = (Object) new TankGreen(s,0,0,0);
                    break;
                case ("purple"):
                    o = (Object) new TankPurple(s,0,0,0);
                    break;
                case ("blue"):
                    o = (Object) new TankBlue(s,0,0,0);
                    break;
                case ("white"):
                    o = (Object) new TankWhite(s,0,0,0);
                    break;
                case ("cyan"):
                    o = (Object) new TankCyan(s,0,0,0);
                    break;
                case ("orange"):
                    o = (Object) new TankOrange(s,0,0,0);
                    break;
                case ("maroon"):
                    o = (Object) new TankMaroon(s,0,0,0);
                    break;
                case ("mustard"):
                    o = (Object) new TankMustard(s,0,0,0);
                    break;
                case ("medic"):
                    o = (Object) new TankMedic(s,0,0,0);
                    break;
                case ("orangered"):
                    o = (Object) new TankOrangeRed(s,0,0,0);
                    break;
                case ("gold"):
                    o = (Object) new TankGold(s,0,0,0);
                    break;
                case ("darkgreen"):
                    o = (Object) new TankDarkGreen(s,0,0,0);
                    break;
                case ("black"):
                    o = (Object) new TankBlack(s,0,0,0);
                    break;
                case ("mimic"):
                    o = (Object) new TankMimic(s,0,0,0);
                    break;
                case ("lightblue"):
                    o = (Object) new TankLightBlue(s,0,0,0);
                    break;
                case ("pink"):
                    o = (Object) new TankPink(s,0,0,0);
                    break;
                case ("mini"):
                    o = (Object) new TankMini(s,0,0,0);
                    break;
                case ("salmon"):
                    o = (Object) new TankSalmon(s,0,0,0);
                    break;
                case ("lightpink"):
                    o = (Object) new TankLightPink(s,0,0,0);
                    break;
                case ("boss"):
                    o = (Object) new TankBoss(s,0,0,0);
                    break;
                default:
                    throw new RuntimeException("Invalid Default Tank Linked Use Parenthesis for a custom tank: (customTank)");
            }
        } else if (s.startsWith("(") && s.endsWith(")")) {
            throw new RuntimeException("Linking non-default tanks non supported yet!");
        } else {
            throw new RuntimeException("Corrupted Tank Reference!");
        }
        return o;
    }
}