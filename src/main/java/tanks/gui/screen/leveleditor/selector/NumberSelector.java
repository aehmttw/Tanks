package tanks.gui.screen.leveleditor.selector;

import tanks.Game;
import tanks.GameObject;
import tanks.gui.screen.leveleditor.OverlaySelectNumber;

import java.util.Locale;

public class NumberSelector<T extends GameObject> extends LevelEditorSelector<T>
{
    public String format = "%.1f";

    /** Interval: [min, max). Just like <code>for</code> loops. */
    public double min = -99999999;
    public double max = 99999999;
    public double step = 1;

    /** When a metadata keybind is pressed, set the number to the minimum value if it is above the maximum value,
     * or the maximum value if it is below the minimum value. */
    public boolean loop = false;

    /** When inputted from a text box, rounds it to the nearest number divisible to <code>step</code>. */
    public boolean forceStep = true;
    public boolean allowDecimals = false;
    public double number;

    @Override
    public void baseInit()
    {
        if (this.init)
            return;

        this.id = "number";
        this.title = "Number Selector";

        super.baseInit();
        this.property = "number";
        this.number = Math.max(this.min, Math.min(this.max, this.number));
    }

    @Override
    public void onSelect()
    {
        Game.screen = new OverlaySelectNumber(Game.screen, editor, this);
    }

    public static final Locale US_LOCALE = new Locale("en", "US");

    public String numberString()
    {
        return String.format(US_LOCALE, format, number);   // bruh
    }

    public void changeMetadata(int add)
    {
        this.number += add * step;

        if (loop)
        {
            this.number = (this.number + this.max) % this.max;

            if (this.number < this.min)
                this.number += this.min;
        }
        else
            this.number = Math.max(this.min, Math.min(this.max, this.number));
    }

    @Override
    public void load()
    {
        this.button.setText(buttonText, number);
    }

    public String getMetadata()
    {
        return numberString();
    }

    public void setMetadata(String d)
    {
        this.number = Double.parseDouble(d);
    }
}
