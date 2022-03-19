package tanks.gui.property;

import java.util.LinkedHashMap;

public class UIPropertySelector extends UIProperty<Integer>
{
    public String[] values;
    public String[] images;

    public UIPropertySelector(LinkedHashMap<String, UIProperty> map, String name, String[] values, int value)
    {
        super(map, name, value);
        this.values = values;
    }

    public UIPropertySelector(LinkedHashMap<String, UIProperty> map, String name, String[] values, String[] images, int value)
    {
        this(map, name, values, value);
        this.images = images;
    }

    public String getString()
    {
        return this.values[this.value];
    }
}
