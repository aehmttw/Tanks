package tanks.tankson;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.function.Function;

public class Compatibility {
    /** Add a static initializer to initialize compatibility table if necessary */
    public static final HashMap<String, Function<Object, Object>> compatibility_table = new HashMap<>();
    public static final HashMap<String, String> field_table = new HashMap<>();

    public static Object convert(Field f, Object o) {
        try
        {
            if (o instanceof Boolean)
                return ((Boolean) o) ? f.getType().getConstructor().newInstance() : null;
        }
        catch (Exception ignored)
        {

        }

        return compatibility_table.get(Serializer.getID(f)).apply(o);
    }

    public static String convert(String f) {
        return field_table.get(f);
    }
}
