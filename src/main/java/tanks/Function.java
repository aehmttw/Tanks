package tanks;

@FunctionalInterface
public interface Function<T, U>
{
    U apply(T t);
}
