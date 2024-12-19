package tanks.gui.screen.leveleditor.selector;

import tanks.Game;
import tanks.gui.screen.leveleditor.OverlaySelectBlockHeight;
import tanks.gui.screen.leveleditor.ScreenLevelEditor;
import tanks.obstacle.ObstacleStackable;

import java.lang.reflect.Field;

public class SelectorStackHeight extends SelectorNumber
{
    public static final String selector_name = "stack_height";

    public SelectorStackHeight(Field f)
    {
        super(f);

        this.min = 0.5;
        this.max = ObstacleStackable.default_max_height;
        this.step = 0.5;
    }

    @Override
    public void openEditorOverlay(ScreenLevelEditor editor)
    {
        Game.screen = new OverlaySelectBlockHeight(Game.screen, editor, this);
    }
}
