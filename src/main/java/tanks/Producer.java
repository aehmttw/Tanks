package tanks;

@FunctionalInterface
public interface Producer<T>
{
    T produce();
}
