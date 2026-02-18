package tanks.registry;

import java.util.LinkedHashMap;

import tanks.minigames.Minigame;

public class RegistryMinigame
{
    public LinkedHashMap<String, Class<? extends Minigame>> minigames = new LinkedHashMap<>();
    public LinkedHashMap<String, String> minigameDescriptions = new LinkedHashMap<>();

    public Class<? extends Minigame> getEntry(String name)
    {
        return minigames.get(name);
    }
}
