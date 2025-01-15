package tanks;

public interface Generator<T>
{
    boolean hasNext();
    T next();
}
