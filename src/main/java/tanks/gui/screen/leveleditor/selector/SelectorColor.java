package tanks.gui.screen.leveleditor.selector;

import tanks.Game;
import tanks.GameObject;
import tanks.gui.screen.leveleditor.OverlaySelectColor;
import tanks.gui.screen.leveleditor.OverlaySelectNumber;
import tanks.gui.screen.leveleditor.ScreenLevelEditor;

import java.lang.reflect.Field;

public class SelectorColor extends SelectorNumber
{
    public static final String selector_name = "color";

    public SelectorColor(Field f)
    {
        super(f);

        this.min = 0;
        this.max = 16777216;
        this.step = 1;
        this.format = "%.0f";
    }

    @Override
    public void openEditorOverlay(ScreenLevelEditor editor)
    {
        Game.screen = new OverlaySelectColor(Game.screen, editor, this);
    }

    @Override
    public void changeMetadata(ScreenLevelEditor e, GameObject o, int add)
    {

    }

    @Override
    public String getMetadataDisplayString(GameObject o)
    {
        int number = (int) this.getMetadata(o);

        int r = (number / (256 * 256)) % 256;
        int g = (number / (256)) % 256;
        int b = number % 256;


        return String.format("\u00A7%03d%03d%03d255%d/%d/%d", r, g, b, r, g, b);
    }
}
