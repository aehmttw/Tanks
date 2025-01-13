package tanks.registry;

import tanks.Game;
import tanks.gui.screen.leveleditor.selector.MetadataSelector;

import java.util.LinkedHashMap;

public class RegistryMetadataSelectors
{
    public LinkedHashMap<String, Class<? extends MetadataSelector>> metadataSelectors = new LinkedHashMap<>();

    public Class<? extends MetadataSelector> getEntry(String name)
    {
        if (!metadataSelectors.containsKey(name))
            Game.exitToCrash(new RuntimeException("The metadata selector for '" + name + "' has not been registered"));

        return metadataSelectors.get(name);
    }
}
