package tanks.registry;

import java.util.LinkedHashMap;
import tanks.generator.LevelGenerator;

public class RegistryGenerator
{
    public LinkedHashMap<String, LevelGenerator> generators = new LinkedHashMap<>();

    public LevelGenerator getEntry(String name)
    {
        return generators.get(name);
    }
}
