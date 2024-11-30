package tanks.gui.input;

import tanks.Game;
import tanks.Panel;

public class InputBinding
{
    /** In milliseconds because {@code System.currentTimeMillis()} is used. */
    public static int doubleClickTime = 300;
    public enum InputType {keyboard, mouse}

    public int input;
    public InputType inputType;

    public int defaultInput;
    public InputType defaultInputType;

    public int rapidClicks = 0;
    public long lastClick;

    public InputBinding(InputType type, int value)
    {
        this.inputType = type;
        this.input = value;

        this.defaultInputType = type;
        this.defaultInput = value;
    }

    public InputBinding()
    {

    }

    public boolean isPressed()
    {
        if (Panel.selectedTextBox != null)
            return false;

        if (inputType == InputType.keyboard)
            return Game.game.window.pressedKeys.contains(input);
        if (inputType == InputType.mouse)
            return Game.game.window.pressedButtons.contains(input);

        return false;
    }

    public boolean isValid()
    {
        if (Panel.selectedTextBox != null)
            return false;

        if (inputType == InputType.keyboard)
            return Game.game.window.validPressedKeys.contains(input);
        if (inputType == InputType.mouse)
            return Game.game.window.validPressedButtons.contains(input);

        return false;
    }

    public boolean doubleValid()
    {
        if (rapidClicks < 2)
            return false;

        rapidClicks = 0;
        return System.currentTimeMillis() - this.lastClick < doubleClickTime;
    }

    public void invalidate()
    {
        if (inputType == InputType.keyboard)
            Game.game.window.validPressedKeys.remove((Integer) input);
        else if (inputType == InputType.mouse)
            Game.game.window.validPressedButtons.remove((Integer) input);

        long time = System.currentTimeMillis();
        if (time - this.lastClick < doubleClickTime)
            rapidClicks++;
        else
            rapidClicks = 1;

        this.lastClick = time;
    }

    @Override
    public String toString()
    {
        return this.inputType + ":" + this.input;
    }

    public String getInputName()
    {
        if (inputType == InputType.mouse)
            return "Mouse button " + (input + 1);
        if (inputType == InputType.keyboard)
        {
            String s = Game.game.window.getKeyText(input);
            return s.substring(0, 1).toUpperCase() + s.substring(1);
        }
        return "None";
    }

    public void reset()
    {
        this.inputType = this.defaultInputType;
        this.input = this.defaultInput;
    }
}
