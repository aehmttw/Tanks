package tanks.tankson;

import java.lang.reflect.Field;

public class MonitoredFieldPointer<T> extends FieldPointer<T>
{
    public Runnable onEdit;

    public MonitoredFieldPointer(Object o, Field f, Runnable onEdit)
    {
        super(o, f);
        this.onEdit = onEdit;
    }

    public MonitoredFieldPointer(Object o, Field f, boolean nullable, Runnable onEdit)
    {
        super(o, f, nullable);
        this.onEdit = onEdit;
    }

    @Override
    public void set(T val)
    {
        super.set(val);
        onEdit.run();
    }

    @Override
    public <U> Pointer<U> cast()
    {
        return new MonitoredFieldPointer<>(object, field, nullable, onEdit);
    }
}
