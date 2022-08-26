package tanks;

@FunctionalInterface
public interface Consumer<T>
{
    void accept(T t);
}
