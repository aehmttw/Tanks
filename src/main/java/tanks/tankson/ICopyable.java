package tanks.tankson;

import basewindow.Color;
import tanks.Game;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static tanks.tankson.CopyableFields.copyFields;

@SuppressWarnings("unchecked")
public interface ICopyable<T>
{
    /**
     * Clone this object's properties to another object
     * @param m the another object
     * @return the same object passed to it, for convenience
     */
    default T clonePropertiesTo(T m)
    {
        try
        {
            // Do not use ComputeIfAbsent. This breaks the iOS compiler.
            if (!copyFields.containsKey(m.getClass()))
                copyFields.put((Class<? extends ICopyable<?>>) m.getClass(), new ArrayList<>());

            ArrayList<Field> fields = copyFields.get((Class<? extends ICopyable<?>>) m.getClass());
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
            {
                f.set(m, ((ICopyable<?>) v).getCopy());
            }
            else if (v instanceof Color)
                ((Color)f.get(m)).set((Color) v);
            else if (v instanceof ArrayList)
            {
                f.set(m, new ArrayList<>());
                ArrayList a = (ArrayList) f.get(m);
                for (Object o: (ArrayList) v)
                {
                    if (o instanceof ICopyable)
                        a.add(((ICopyable<?>) o).getCopy());
                    else
                        a.add(o);
                }
            }
            else
            {
                f.set(m, v);
            }
        }
        catch (Exception ignored) {}
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
