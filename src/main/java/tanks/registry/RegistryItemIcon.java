package tanks.registry;

import java.util.LinkedHashMap;

import tanks.item.ItemIcon;

public class RegistryItemIcon
{
    public LinkedHashMap<String, ItemIcon> itemIcons = new LinkedHashMap<>();

    public ItemIcon getItemIcon(String icon)
    {
        return itemIcons.get(icon);
    }
}
