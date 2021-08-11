package tanks.hotbar.item.property;

import java.util.LinkedHashMap;

public class ItemPropertyString extends ItemProperty<String>
{
    public ItemPropertyString(LinkedHashMap<String, ItemProperty> map, String name, String value)
    {
        super(map, name, value);
    }
}
