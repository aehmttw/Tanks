package tanks.gui.screen.leveleditor.selector;

import tanks.Game;
import tanks.obstacle.Obstacle;

import java.lang.reflect.Field;

public class SelectorGroupID extends SelectorNumber
{
    public static final String selector_name = "group_id";

    public SelectorGroupID(Field f)
    {
        super(f);
        this.min = 0;
        this.format = "%.0f";
    }
}