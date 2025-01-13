package tanks.gui.screen.leveleditor.selector;

import tanks.Game;
import tanks.GameObject;
import tanks.gui.screen.leveleditor.OverlaySelectRotation;
import tanks.gui.screen.leveleditor.ScreenLevelEditor;
import tanks.translation.Translation;

import java.lang.reflect.Field;

public class SelectorRotation extends SelectorNumber
{
    public static final String selector_name = "rotation";

    public SelectorRotation(Field f)
    {
        super(f);
        this.wrap = true;
        this.format = "%.0f";
        this.min = 0;
        this.max = Math.PI * 2;
        this.step = Math.PI / 2;
    }

    @Override
    public void openEditorOverlay(ScreenLevelEditor e)
    {
        Game.screen = new OverlaySelectRotation(Game.screen, e, this);
    }

    @Override
    public String getMetadataDisplayString(GameObject o)
    {
        int i = (int) Math.round((double) this.getMetadata(o) / Math.PI * 2);

        if (i == 0)
            return Translation.translate("right");
        else if (i == 1)
            return Translation.translate("down");
        else if (i == 2)
            return Translation.translate("left");
        else if (i == 3)
            return Translation.translate("up");
        else
            return "???";
    }
}
