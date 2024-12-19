package tanks.registry;

import tanks.gui.screen.leveleditor.selector.MetadataSelector;

import java.util.LinkedHashMap;

public class RegistryMetadataSelectors
{
    public LinkedHashMap<String, Class<? extends MetadataSelector>> metadataSelectors = new LinkedHashMap<>();

    public Class<? extends MetadataSelector> getEntry(String name)
    {
        return metadataSelectors.get(name);
    }
}
