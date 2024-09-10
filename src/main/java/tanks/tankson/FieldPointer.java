package tanks.tankson;

import java.lang.reflect.Field;

public class FieldPointer<T>
{
    protected final Object object;
    protected final Field field;
    public final boolean nullable;

    public FieldPointer(Object o, Field f)
    {
        this.object = o;
        this.field = f;

        Property p = f.getAnnotation(Property.class);
        if (p != null)
            this.nullable = p.nullable();
        else
            this.nullable = true;
    }

    public FieldPointer(Object o, Field f, boolean nullable)
    {
        this.object = o;
        this.field = f;

        this.nullable = nullable;
    }

    public void set(T val)
    {
        try
        {
            field.set(object, val);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public T get()
    {
        try
        {
            return (T) field.get(object);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public Class<T> getType()
    {
        return (Class<T>) field.getType();
    }
}
