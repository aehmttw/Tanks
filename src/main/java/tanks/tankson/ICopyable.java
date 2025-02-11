package tanks.tankson;

import tanks.Game;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("unchecked")
public interface ICopyable<T>
{
    HashMap<Class<? extends ICopyable<?>>, ArrayList<Field>> copyFields = new HashMap<>();

    /**
     * Clone this object's properties to another object
     * @param m the another object
     * @return the same object passed to it, for convenience
     */
    default T clonePropertiesTo(T m)
    {
        try
        {
            ArrayList<Field> fields = copyFields.computeIfAbsent((Class<? extends ICopyable<?>>) m.getClass(), k -> new ArrayList<>());
            if (fields.isEmpty())
            {
                for (Field f : m.getClass().getFields())
                {
                    Property p = f.getAnnotation(Property.class);
                    if (p == null) continue;
                    copyTo(m, f);
                    fields.add(f);
                }
            }
            else
            {
                //noinspection ForLoopReplaceableByForEach
                for (int i = 0; i < fields.size(); i++)
                    copyTo(m, fields.get(i));
            }
        }
        catch (Exception e)
        {
            Game.exitToCrash(e);
        }

        return m;
    }

    default void copyTo(T m, Field f)
    {
        try
        {
            Object v = f.get(this);
            if (v instanceof ICopyable)
                f.set(m, ((ICopyable<?>) v).getCopy());
            else
                f.set(m, v);
        }
        catch (Exception ignored) { }
    }

    /**
     * Gets a template copy of this, not to be added to the game field but to be used as a template
     * @return a template copy
     */
    default T getCopy()
    {
        try
        {
            T t = (T) this.getClass().getConstructor().newInstance();
            this.clonePropertiesTo(t);
            return t;
        }
        catch (Exception e)
        {
            Game.exitToCrash(e);
        }

        return null;
    }
}
