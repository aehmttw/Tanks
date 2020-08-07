package tanks.hotbar.item.property;

import java.util.LinkedHashMap;

public class ItemPropertyImageSelector extends ItemProperty<Integer>
{
    public String[] values;

    public ItemPropertyImageSelector(LinkedHashMap<String, ItemProperty> map, String name, String[] values, int value)
    {
        super(map, name, value);
        this.values = values;
    }

    public String getString()
    {
        return this.values[this.value];
    }
}
