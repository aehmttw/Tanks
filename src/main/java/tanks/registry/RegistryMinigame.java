package tanks.registry;

import tanks.minigames.Minigame;

import java.util.LinkedHashMap;

public class RegistryMinigame
{
    public LinkedHashMap<String, Class<? extends Minigame>> minigames = new LinkedHashMap<>();
    public LinkedHashMap<String, String> minigameDescriptions = new LinkedHashMap<>();

    public Class<? extends Minigame> getEntry(String name)
    {
        return minigames.get(name);
    }
}
