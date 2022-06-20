package tanks.gui.property;

import java.util.LinkedHashMap;

public class UIPropertyInt extends UIProperty<Integer>
{
    public UIPropertyInt(LinkedHashMap<String, UIProperty> map, String name, Integer value)
    {
        super(map, name, value);
    }
}
