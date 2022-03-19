package tanks.gui.input;

import tanks.Game;

public class InputBinding
{
    public enum InputType {keyboard, mouse}

    public int input;
    public InputType inputType;

    public int defaultInput;
    public InputType defaultInputType;

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
        if (inputType == InputType.keyboard)
            return Game.game.window.pressedKeys.contains(input);
        else if (inputType == InputType.mouse)
            return Game.game.window.pressedButtons.contains(input);

        return false;
    }

    public boolean isValid()
    {
        if (inputType == InputType.keyboard)
            return Game.game.window.validPressedKeys.contains(input);
        else if (inputType == InputType.mouse)
            return Game.game.window.validPressedButtons.contains(input);

        return false;
    }

    public void invalidate()
    {
        if (inputType == InputType.keyboard)
            Game.game.window.validPressedKeys.remove((Integer) input);
        else if (inputType == InputType.mouse)
            Game.game.window.validPressedButtons.remove((Integer) input);
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
        else if (inputType == InputType.keyboard)
        {
            String s = Game.game.window.getKeyText(input);
            return s.substring(0, 1).toUpperCase() + s.substring(1);
        }
        else
            return "None";
    }

    public void reset()
    {
        this.inputType = this.defaultInputType;
        this.input = this.defaultInput;
    }
}
