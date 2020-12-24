package tanks.hotbar.item.property;

import java.util.LinkedHashMap;

public class ItemPropertySelector extends ItemProperty<Integer>
{
    public String[] values;
    public String[] images;

    public ItemPropertySelector(LinkedHashMap<String, ItemProperty> map, String name, String[] values, int value)
    {
        super(map, name, value);
        this.values = values;
    }

    public ItemPropertySelector(LinkedHashMap<String, ItemProperty> map, String name, String[] values, String[] images, int value)
    {
        this(map, name, values, value);
        this.images = images;
    }

    public String getString()
    {
        return this.values[this.value];
    }
}
