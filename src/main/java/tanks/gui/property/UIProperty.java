package tanks.gui.property;

import java.util.LinkedHashMap;

public abstract class UIProperty<T>
{
    public String name;
    public T value;

    public UIProperty(LinkedHashMap<String, UIProperty> map, String name, T value)
    {
        this.name = name;
        this.value = value;
        map.put(this.name, this);
    }
}
