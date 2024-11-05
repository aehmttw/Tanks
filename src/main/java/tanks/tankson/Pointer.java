package tanks.tankson;

public abstract class Pointer<T>
{
    public boolean nullable;

    public abstract void set(T val);

    public abstract T get();

    public abstract Class<T> getType();

    public abstract <U> Pointer<U> cast();
}
