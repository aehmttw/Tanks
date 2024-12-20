package tanks.gui.screen.leveleditor.selector;

import tanks.Game;
import tanks.GameObject;
import tanks.gui.screen.leveleditor.OverlaySelectNumber;

import java.util.Locale;

public class SelectorNumber<T extends GameObject> extends LevelEditorSelector<T, Double>
{
    public String format = "%.1f";

    /** Interval: [min, max). Just like <code>for</code> loops. */
    public double min = -99999999;
    public double max = 99999999;
    public double defaultNum = 0;
    public double step = 1;

    /** When a metadata keybind is pressed, set the number to the minimum value if it is above the maximum value,
     * or the maximum value if it is below the minimum value. */
    public boolean loop = false;

    /** When set to true, inputs from a text box will be rounded to the nearest number divisible by <code>step</code>. */
    public boolean forceStep = true;
    public boolean allowDecimals = false;

    @Override
    public void baseInit()
    {
        if (this.init)
            return;

        this.id = "number";
        this.title = "Number Selector";

        super.baseInit();
        if (!modified)
        {
            setObject(defaultNum);
            modified = false;
        }
        limit(min, max);
    }

    @Override
    public void onSelect()
    {
        Game.screen = new OverlaySelectNumber(Game.screen, editor, this);
    }

    public String numberString()
    {
        return String.format(Locale.US, format, number());   // bruh
    }

    protected void changeMetadata(int add)
    {
        double number = number();
        number += add * step;

        if (loop)
        {
            number = (number + this.max) % this.max;

            if (number < this.min)
                number += this.min;
        }
        else
            number = Math.max(this.min, Math.min(this.max, number));
        setObject(number);
    }

    public double number()
    {
        return getObject();
    }

    public void setNumber(Number number)
    {
        setObject(number.doubleValue());
    }

    public void min(double min)
    {
        limit(min, max);
    }

    public void max(double max)
    {
        limit(min, max);
    }

    public void limit(double min, double max)
    {
         setObject(Math.min(max, Math.max(min, getObject())));
    }

    @Override
    public void load()
    {
        this.button.setText(buttonText, number());
    }

    public String getMetadata()
    {
        return numberString();
    }

    public void setMetadata(String d)
    {
        setObject(Double.parseDouble(d));
    }
}
