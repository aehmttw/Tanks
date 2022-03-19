package tanks.gui.property;

import java.util.LinkedHashMap;

public class UIPropertyBoolean extends UIProperty<Boolean>
{
    public UIPropertyBoolean(LinkedHashMap<String, UIProperty> map, String name, Boolean value)
    {
        super(map, name, value);
    }
}
