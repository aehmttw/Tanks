package tanks.gui.property;

import java.util.LinkedHashMap;

public class UIPropertyDouble extends UIProperty<Double>
{
    public UIPropertyDouble(LinkedHashMap<String, UIProperty> map, String name, Double value)
    {
        super(map, name, value);
    }
}
