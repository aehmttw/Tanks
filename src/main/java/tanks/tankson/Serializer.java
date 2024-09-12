package tanks.tankson;

import basewindow.Model;
import com.google.gson.Gson;

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
            p.put("type", o.getClass().getName());
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
                        } else if (o2 instanceof Model) {
                            p.put(f.getName(), o2.toString());
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
}