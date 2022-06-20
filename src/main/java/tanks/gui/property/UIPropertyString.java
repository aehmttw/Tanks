package tanks.gui.property;

import java.util.LinkedHashMap;

public class UIPropertyString extends UIProperty<String>
{
    public UIPropertyString(LinkedHashMap<String, UIProperty> map, String name, String value)
    {
        super(map, name, value);
    }
}
