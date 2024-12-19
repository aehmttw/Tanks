package tanks.gui.screen.leveleditor.selector;

import tanks.Game;
import tanks.GameObject;
import tanks.gui.screen.leveleditor.OverlaySelectNumber;
import tanks.gui.screen.leveleditor.ScreenLevelEditor;

import java.lang.reflect.Field;
import java.util.Locale;

public class SelectorNumber extends MetadataSelector
{
    public static final String selector_name = "number";

    public String format = "%.1f";

    /** Interval: [min, max). Just like <code>for</code> loops. */
    public double min = -99999999;
    public double max = 99999999;
    public double step = 1;

    /** When a metadata keybind is pressed, set the number to the minimum value if it is above the maximum value,
     * or the maximum value if it is below the minimum value. */
    public boolean wrap = false;

    /** When inputted from a text box, rounds it to the nearest number divisible to <code>step</code>. */
    public boolean forceStep = true;
    public boolean allowDecimals = false;

    public SelectorNumber(Field f)
    {
        super(f);
    }

    @Override
    public void openEditorOverlay(ScreenLevelEditor editor)
    {
        Game.screen = new OverlaySelectNumber(Game.screen, editor, this);
    }

    @Override
    public void changeMetadata(ScreenLevelEditor e, GameObject o, int add)
    {
        double number = ((Number) this.getMetadata(o)).doubleValue() + add * step;

        if (wrap)
        {
            number = (number + this.max) % this.max;

            if (number < this.min)
                number += this.min;
        }
        else
            number = Math.max(this.min, Math.min(this.max, number));

        if (this.metadataField.getType().equals(int.class))
            this.setMetadata(e, o, (int) number);
        else
            this.setMetadata(e, o, number);
    }

    @Override
    public void setMetadata(ScreenLevelEditor ed, GameObject o, Object value)
    {
        if (this.metadataField.getType().equals(int.class))
            super.setMetadata(ed, o, ((Number) value).intValue());
        else
            super.setMetadata(ed, o, value);
    }

    public String numberString(GameObject o)
    {
        return String.format(format, ((Number) this.getMetadata(o)).doubleValue(), Locale.ROOT);
    }
}
