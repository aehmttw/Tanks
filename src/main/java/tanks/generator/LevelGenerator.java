package tanks.generator;

import tanks.gui.property.UIProperty;

import java.util.LinkedHashMap;

public abstract class LevelGenerator
{
    public LinkedHashMap<String, UIProperty> properties = new LinkedHashMap<>();
}
