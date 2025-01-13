package tanks.tankson;

import java.util.ArrayList;

public class ArrayListIndexPointer<T> extends Pointer<T>
{
    protected ArrayList<T> arrayList;
    protected int index;
    protected boolean deleted = false;

    public ArrayListIndexPointer(ArrayList<T> l, int i)
    {
        this.arrayList = l;
        this.index = i;
        this.nullable = false;
    }

    public ArrayListIndexPointer(ArrayList<T> l, int i, boolean nullable)
    {
        this.arrayList = l;
        this.index = i;
        this.nullable = nullable;
    }

    @Override
    public void set(T val)
    {
        if (this.deleted)
            throw new RuntimeException("Attempted to access deleted arraylist pointer!");

        if (!nullable && val == null)
        {
            System.out.println(this);
            this.deleted = true;
            this.arrayList.remove(this.index);
        }
        else
            this.arrayList.set(this.index, val);
    }

    @Override
    public T get()
    {
        if (this.deleted)
            return null;

        return this.arrayList.get(this.index);
    }

    @Override
    public Class<T> getType()
    {
        return (Class<T>) this.arrayList.get(this.index).getClass();
    }

    @Override
    public <U> Pointer<U> cast()
    {
        return new ArrayListIndexPointer<>((ArrayList<U>) arrayList, index, nullable);
    }
}
