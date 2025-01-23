package tanks.tankson;

import java.lang.reflect.Field;

public class Compatibility {
    public static Object convert(Field f, Object v) {
        try
        {
            if (v instanceof Boolean && (Boolean) v)
                return v.getClass().getConstructor().newInstance();
        }
        catch (Exception e)
        {
            System.err.println("Could not convert " + f.getName());
            e.printStackTrace();
        }
        return null;
    }
}
