package tanks.hotbar.item.property;

import java.util.LinkedHashMap;

public class ItemPropertyBoolean extends ItemProperty<Boolean>
{
    public ItemPropertyBoolean(LinkedHashMap<String, ItemProperty> map, String name, Boolean value)
    {
        super(map, name, value);
    }
}
