package tanks.tankson;

import tanks.bullet.Bullet;
import tanks.item.ItemBullet;
import tanks.item.ItemMine;
import tanks.tank.Mine;
import tanks.tank.TankAIControlled;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Compatibility
{
    /**
     * Add a static initializer to initialize compatibility table if necessary
     */
    public static final HashMap<String, BiFunction<Object, Object, Object>> compatibility_table = new HashMap<>();

    public static final HashMap<Class<?>, BiFunction<Field, Object, Object>> general_table = new HashMap<>();

    public static final HashMap<String, String> field_table = new HashMap<>();

    static
    {
        addGeneralCase(Boolean.class, Compatibility::applyBoolean);
        compatibility_table.put("bullet", (owner, a) ->
        {
            if (a instanceof HashMap)
            {
                Object b = Serializer.parseObject((Map<String, Object>) a);
                if (b instanceof Bullet)
                {
                    if (owner instanceof TankAIControlled)
                    {
                        if (!((TankAIControlled) owner).enableMovement)
                            ((Bullet) b).recoil = 0;
                    }

                    return new ItemBullet.ItemStackBullet(null, new ItemBullet((Bullet) b), 0);
                }
            }

            throw new RuntimeException("Failed to convert bullet: " + a.getClass() + " " + a.toString());
        });

        compatibility_table.put("mine", (owner, a) ->
        {
            if (a instanceof HashMap)
            {
                Object b = Serializer.parseObject((Map<String, Object>) a);
                if (b instanceof Mine)
                    return new ItemMine.ItemStackMine(null, new ItemMine((Mine) b), 0);
            }

            throw new RuntimeException("Failed to convert mine: " + a.getClass() + " " + a.toString());
        });
    }

    public static <V> void addGeneralCase(Class<V> cls, BiFunction<Field, V, Object> func)
    {
        //noinspection unchecked
        general_table.put(cls, (BiFunction<Field, Object, Object>) func);
    }

    public static Object convert(Field f, Object owner, Object o)
    {
        if (general_table.containsKey(o.getClass()))
            return general_table.get(o.getClass()).apply(f, o);

        return compatibility_table.get(Serializer.getid(f)).apply(owner, o);
    }

    public static String convert(String f)
    {
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
