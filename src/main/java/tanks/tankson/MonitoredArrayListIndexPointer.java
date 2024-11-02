package tanks.tankson;

import java.util.ArrayList;

public class MonitoredArrayListIndexPointer<T> extends ArrayListIndexPointer<T>
{
    public Runnable onEdit;

    public MonitoredArrayListIndexPointer(ArrayList<T> l, int i, Runnable onEdit)
    {
        super(l, i);
        this.onEdit = onEdit;
    }

    public MonitoredArrayListIndexPointer(ArrayList<T> l, int i, boolean nullable, Runnable onEdit)
    {
        super(l, i, nullable);
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
        return new MonitoredArrayListIndexPointer<>((ArrayList<U>) arrayList, index, nullable, onEdit);
    }
}
