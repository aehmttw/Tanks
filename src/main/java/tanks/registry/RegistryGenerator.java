package tanks.registry;

import tanks.generator.LevelGenerator;

import java.util.LinkedHashMap;

public class RegistryGenerator
{
    public LinkedHashMap<String, LevelGenerator> generators = new LinkedHashMap<>();

    public LevelGenerator getEntry(String name)
    {
        return generators.get(name);
    }
}
