package tanks.gui.screen.leveleditor.selector;

import tanks.Game;
import tanks.GameObject;
import tanks.gui.screen.leveleditor.OverlaySelectBeatBlockPattern;
import tanks.gui.screen.leveleditor.ScreenLevelEditor;

import java.lang.reflect.Field;

public class SelectorBeatPattern extends SelectorGroupID
{
    public static final String selector_name = "beat_pattern";

    public SelectorBeatPattern(Field f)
    {
        super(f);
        this.max = 7;
    }

    @Override
    public void openEditorOverlay(ScreenLevelEditor e)
    {
        Game.screen = new OverlaySelectBeatBlockPattern(Game.screen, e, this);
    }

    @Override
    public String getMetadataDisplayString(GameObject o)
    {
        int number = (int) this.getMetadata(o);
        return (int) Math.pow(2, (int) (number / 2)) + (number % 2 == 0 ? "a" : "b");
    }
}
