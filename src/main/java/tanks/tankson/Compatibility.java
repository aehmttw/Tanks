package tanks.tankson;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Compatibility {
    /** Add a static initializer to initialize compatibility table if necessary */
    public static final HashMap<String, Function<Object, Object>> compatibility_table = new HashMap<>();
    public static final HashMap<Class<?>, BiFunction<Field, Object, Object>> general_table = new HashMap<>();
    public static final HashMap<String, String> field_table = new HashMap<>();

    static
    {
        addGeneralCase(Boolean.class, Compatibility::applyBoolean);
    }

    public static <V> void addGeneralCase(Class<V> cls, BiFunction<Field, V, Object> func)
    {
        //noinspection unchecked
        general_table.put(cls, (BiFunction<Field, Object, Object>) func);
    }

    public static Object convert(Field f, Object o) {
        if (general_table.containsKey(o.getClass()))
            return general_table.get(o.getClass()).apply(f, o);

        return compatibility_table.get(Serializer.getid(f)).apply(o);
    }

    public static String convert(String f) {
        return field_table.get(f);
    }

    private static Object applyBoolean(Field f, Boolean b)
    {
        try
        {
            return b ? f.getType().getConstructor().newInstance() : null;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
