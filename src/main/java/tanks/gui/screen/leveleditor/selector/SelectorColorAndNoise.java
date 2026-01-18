package tanks.gui.screen.leveleditor.selector;

import tanks.Game;
import tanks.GameObject;
import tanks.gui.screen.leveleditor.OverlaySelectColorAndNoise;
import tanks.gui.screen.leveleditor.ScreenLevelEditor;

import java.lang.reflect.Field;

public class SelectorColorAndNoise extends SelectorNumber
{
    public static final String selector_name = "color_with_noise";

    public SelectorColorAndNoise(Field f)
    {
        super(f);

        this.min = 0;
        this.max = 281474976710656.0;
        this.step = 1;
        this.format = "%.0f";
    }

    @Override
    public void openEditorOverlay(ScreenLevelEditor editor)
    {
        Game.screen = new OverlaySelectColorAndNoise(Game.screen, editor, this);
    }

    @Override
    public void changeMetadata(ScreenLevelEditor e, GameObject o, int add)
    {

    }

    @Override
    public String getMetadataDisplayString(GameObject o)
    {
        long number = (long) this.getMetadata(o);

        long r = (number / (256 * 256)) % 256;
        long g = (number / (256)) % 256;
        long b = number % 256;

        return String.format("\u00A7%03d%03d%03d255%d/%d/%d", r, g, b, r, g, b);
    }
}
