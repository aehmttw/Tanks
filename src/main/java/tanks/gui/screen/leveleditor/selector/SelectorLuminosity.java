package tanks.gui.screen.leveleditor.selector;

import java.lang.reflect.Field;

public class SelectorLuminosity extends SelectorNumber
{
    public static final String selector_name = "luminosity";

    public SelectorLuminosity(Field f)
    {
        super(f);

        this.min = 0.5;
        this.max = 8;
        this.step = 0.5;
    }
}
