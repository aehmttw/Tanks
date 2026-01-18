package tanks.registry;

import tanks.item.ItemIcon;

import java.util.LinkedHashMap;

public class RegistryItemIcon
{
    public LinkedHashMap<String, ItemIcon> itemIcons = new LinkedHashMap<>();

    public ItemIcon getItemIcon(String icon)
    {
        return itemIcons.get(icon);
    }
}
