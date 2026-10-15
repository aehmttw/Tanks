package tanks.tankson;

import java.util.*;

/** A map of TanksONable objects keyed by their own {@code @Property(id = "name")} value. */
public class NamedList<T> extends LinkedHashMap<String, T> implements Serializable
{
    public NamedList()
    {
        super();
    }

    public NamedList(int initialCapacity)
    {
        super(initialCapacity);
    }

    public NamedList(int initialCapacity, float loadFactor)
    {
        super(initialCapacity, loadFactor);
    }

    public NamedList(int initialCapacity, float loadFactor, boolean accessOrder)
    {
        super(initialCapacity, loadFactor, accessOrder);
    }

    public NamedList(Map<? extends String, ? extends T> m)
    {
        super(m);
    }

    /** Key an element by its own name, so callers never derive the key themselves. */
    public T add(T t)
    {
        return this.put(Serializer.getName(t), t);
    }

    @Override
    public Object serialize()
    {
        ArrayList<Object> els = new ArrayList<>(this.size());
        for (T t: this.values())
            els.add(Serializer.toMap(t));

        return els;
    }

    @Override
    public Serializable deserialize(Object o)
    {
        this.clear();

        // Briefly, a NamedList was written as an escaped string holding the array.
        if (o instanceof String)
            o = TanksON.parseObject((String) o);

        if (!(o instanceof ArrayList))
            throw new RuntimeException("Expected an array of named objects, got: " + o);

        ArrayList<?> els = (ArrayList<?>) o;

        // Older files wrote maps as a [[keys],[values]] pair; the names are in the objects anyway.
        if (els.size() == 2 && els.get(0) instanceof ArrayList && els.get(1) instanceof ArrayList)
            els = (ArrayList<?>) els.get(1);

        for (Object el: els)
        {
            if (!(el instanceof Map))
                throw new RuntimeException("Expected a named object, got: " + el);

            this.add((T) Serializer.parseObject((Map<String, Object>) el));
        }

        return this;
    }
}
