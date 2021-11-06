package tanks.gui.input;

import tanks.Game;
import tanks.translation.Translation;

public class InputBindingGroup
{
    public String name;
    public InputBinding input1;
    public InputBinding input2;

    public InputBindingGroup(String name, InputBinding i1, InputBinding i2)
    {
        this.name = name;
        this.input1 = i1;
        this.input2 = i2;

        Game.game.inputBindings.add(this);
    }

    public InputBindingGroup(String name, InputBinding i1)
    {
        this.name = name;
        this.input1 = i1;
        this.input2 = new InputBinding();

        Game.game.inputBindings.add(this);
    }

    public boolean isPressed()
    {
        return input1.isPressed() || input2.isPressed();
    }

    public boolean isValid()
    {
        return input1.isValid() || input2.isValid();
    }

    public void invalidate()
    {
        input1.invalidate();
        input2.invalidate();
    }

    @Override
    public String toString()
    {
        return this.name + "=" + this.input1 + "," + this.input2;
    }

    public String getInputs()
    {
        if (input2.inputType == null)
            return input1.getInputName();
        else if (input1.inputType == null)
            return input2.getInputName();
        else if ((input1.getInputName().startsWith("Left") && input2.getInputName().startsWith("Right")) ||
                (input2.getInputName().startsWith("Left") && input1.getInputName().startsWith("Right")))
            return Game.formatString(input1.getInputName().replace("Left ", "").replace("Right ", ""));
        else
            return Translation.translate("%s or %s", Translation.translate(input1.getInputName()), Translation.translate(input2.getInputName()));
    }

    public void reset()
    {
        this.input1.reset();
        this.input2.reset();
    }
}
