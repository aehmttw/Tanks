package tanks.tankson;

import java.util.ArrayList;

public class MonitoredArrayListIndexPointer<T> extends ArrayListIndexPointer<T>
{
    public Runnable onEdit;

    public MonitoredArrayListIndexPointer(Class<T> tClass, ArrayList<T> l, int i, Runnable onEdit)
    {
        super(tClass, l, i);
        this.onEdit = onEdit;
    }

    public MonitoredArrayListIndexPointer(Class<T> tClass, ArrayList<T> l, int i, boolean nullable, Runnable onEdit)
    {
        super(tClass, l, i, nullable);
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
        return new MonitoredArrayListIndexPointer<U>((Class<U>) tClass, (ArrayList<U>) this.arrayList, this.index, this.nullable, this.onEdit);
    }
}
