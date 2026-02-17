package tanks.registry;

import java.util.LinkedHashMap;
import tanks.Game;
import tanks.gui.screen.leveleditor.selector.MetadataSelector;

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
