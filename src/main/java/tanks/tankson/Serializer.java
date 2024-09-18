package tanks.tankson;

import basewindow.Model;
import com.google.gson.Gson;
import tanks.bullet.Bullet;
import tanks.tank.Explosion;
import tanks.tank.TankAIControlled;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class Serializer {

    //Debugging GSON
    private static final Gson gson = new Gson();

    public static boolean isTanksONable(Object o) {
        if (o != null)
            return o.getClass().isAnnotationPresent(TanksONable.class);
        else
            return false;
    }

    public static Map<String, Object> toMap(Object o) {
        if (isTanksONable(o)) {
            HashMap<String, Object> p = new HashMap<>();
            p.put("obj_type", o.getClass().getName());
            for (Field f : o.getClass().getFields()){
                if (f.isAnnotationPresent(Property.class)) {
                    try {
                        Object o2 = f.get(o);
                        if (isTanksONable(o2)) {
                            p.put(f.getName(), toMap(o2));
                        } else if (o2 instanceof ArrayList) {
                            if (!((ArrayList) o2).isEmpty() && isTanksONable(((ArrayList) o2).get(0))){
                                ArrayList<Map<String,Object>> o3s = new ArrayList<>();
                                for (Object o3 : ((ArrayList) o2)) {
                                    o3s.add(toMap(o3));
                                }
                                p.put(f.getName(), o3s);
                            } else {
                                p.put(f.getName(), f.get(o));
                            }
                        } else if (o2 instanceof Enum) {
                            p.put(f.getName(), ((Enum) o2).name());
                        } else if (o2 instanceof Serializable) {
                            p.put(f.getName(), ((Serializable) o2).serialize());
                        } else {
                            p.put(f.getName(), f.get(o));
                        }
                    } catch (Exception ignore) {
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
        Object o;
        if (m.get("obj_type").equals("tanks.tank.TankAIControlled")) {
            o = (Object) new TankAIControlled("", 0, 0, 0, 0, 0, 0, 0, TankAIControlled.ShootAI.none);
        } else if (m.get("obj_type").equals("tanks.tank.Explosion")) {
            o = (Object) new Explosion();
        } else if (m.get("obj_type").equals("")) {
            o = (Object) new Bullet();
        } else {
            o = null;
        }
        for (Field f : TankAIControlled.class.getFields()){
            if (f.isAnnotationPresent(Property.class)) {
                try {
                    Object o2 = f.get(o);
                    if (isTanksONable(o2)) {
                        f.set(o2, parseObject((Map) m.get(f.getName())));
                    } else if (o2 instanceof ArrayList) {
                        if (!((ArrayList) o2).isEmpty() && isTanksONable(((ArrayList) o2).get(0))){
                            ArrayList o3s = new ArrayList();
                            for (Map o3 : ((ArrayList<Map>) m.get(f.getName()))) {
                                o3s.add(parseObject(o3));
                            }
                            f.set(o2,o3s);
                        } else {
                            f.set(o2,m.get(f.getName()));
                        }
                    } else if (o2 instanceof Enum) {
                        f.set(o2,Enum.valueOf((Class<? extends Enum>) f.getType(), (String) m.get(f.getName())));
                    } else if (o2 instanceof Serializable) {
                        f.set(o2,((Serializable) o2).deserialize((String) m.get(f.getName())));
                    } else {
                        f.set(o2,m.get(f.getName()));
                    }
                } catch (Exception ignore) {
                }
            }
        }
        return o;
    }
}