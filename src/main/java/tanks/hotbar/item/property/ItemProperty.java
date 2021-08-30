package tanks.hotbar.item.property;

import java.util.LinkedHashMap;

public abstract class ItemProperty<T>
{
    public String name;
    public T value;

    public ItemProperty(LinkedHashMap<String, ItemProperty> map, String name, T value)
    {
        this.name = name;
        this.value = value;
        map.put(this.name, this);
    }
}
