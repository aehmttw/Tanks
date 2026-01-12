package tanks.tankson;

import java.util.ArrayList;

public class ArrayListIndexPointer<T> extends Pointer<T>
{
    protected ArrayList<T> arrayList;
    protected int index;
    protected boolean deleted = false;
    protected Class<T> tClass;

    public ArrayListIndexPointer(Class<T> tClass, ArrayList<T> l, int i)
    {
        this.tClass = tClass;
        this.arrayList = l;
        this.index = i;
        this.nullable = false;
    }

    public ArrayListIndexPointer(Class<T> tClass, ArrayList<T> l, int i, boolean nullable)
    {
        this.tClass = tClass;
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

    public int getIndex()
    {
        return this.index;
    }

    @Override
    public Class<T> getType()
    {
        return this.tClass;
    }

    @Override
    public <U> Pointer<U> cast()
    {
        return new ArrayListIndexPointer<U>((Class<U>) tClass, (ArrayList<U>) this.arrayList, this.index, this.nullable);
    }
}
