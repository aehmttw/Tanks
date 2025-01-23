package tanks.tankson;

import tanks.tank.Explosion;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.function.Function;

public class Compatibility {
    public static final HashMap<String, Function<Object, Object>> compatibility_table = new HashMap<>();
    public static final HashMap<String, String> field_table = new HashMap<>();

    public static void init() {
        compatibility_table.put("explode_on_destroy", Compatibility::updateExplosion);
    }

    public static Object convert(Field f, Object v) {
        return compatibility_table.get(Serializer.getid(f)).apply(v);
    }

    public static String convert(String f) {
        return field_table.get(f);
    }

    // Explosions 1.6.b -> 1.6.c
    public static Object updateExplosion(Object o) {
        if (o instanceof Boolean) {
            if ((Boolean) o) {
                return new Explosion();
            } else {
                return null;
            }
        } else {
            throw new RuntimeException("Update Explosions from 1.6.b --> 1.6.c");
        }
    }
}
