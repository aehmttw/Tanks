package tanks.tankson;

import tanks.Game;

import java.lang.reflect.Field;

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
            for (Field f : m.getClass().getFields())
            {
                Property p = f.getAnnotation(Property.class);
                if (p != null)
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
            }
        }
        catch (Exception e)
        {
            Game.exitToCrash(e);
        }

        return m;
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
