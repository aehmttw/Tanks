package tanks.tankson;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

public class CopyableFields
{
    // Placing this in ICopyable breaks the iOS compiler.
    public static HashMap<Class<? extends ICopyable<?>>, ArrayList<Field>> copyFields = new HashMap<>();
}
