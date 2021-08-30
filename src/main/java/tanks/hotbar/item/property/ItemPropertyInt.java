package tanks.hotbar.item.property;

import java.util.LinkedHashMap;

public class ItemPropertyInt extends ItemProperty<Integer>
{
    public ItemPropertyInt(LinkedHashMap<String, ItemProperty> map, String name, Integer value)
    {
        super(map, name, value);
    }
}
