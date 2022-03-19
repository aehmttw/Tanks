package tanks.gui.property;

import java.util.LinkedHashMap;

public class UIPropertyImageSelector extends UIProperty<Integer>
{
    public String[] values;

    public UIPropertyImageSelector(LinkedHashMap<String, UIProperty> map, String name, String[] values, int value)
    {
        super(map, name, value);
        this.values = values;
    }

    public String getString()
    {
        return this.values[this.value];
    }
}
