package tanks.hotbar.item.property;

import java.util.LinkedHashMap;

public class ItemPropertyDouble extends ItemProperty<Double>
{
    public ItemPropertyDouble(LinkedHashMap<String, ItemProperty> map, String name, Double value)
    {
        super(map, name, value);
    }
}
