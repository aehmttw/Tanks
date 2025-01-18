package tanks.gui.input;

import tanks.Game;
import tanks.translation.Translation;

import java.util.HashSet;

public class InputBindingGroup
{
    public String name;
    public InputBinding input1;
    public InputBinding input2;

    public int modifier = -1;
    public HashSet<Integer> subModifiers;

    public InputBindingGroup(String name, InputBinding i1, InputBinding i2)
    {
        this.name = name;
        this.input1 = i1;
        this.input2 = i2;

        Game.game.inputBindings.put(name, this);
    }

    public InputBindingGroup(String name, InputBinding i1)
    {
        this.name = name;
        this.input1 = i1;
        this.input2 = new InputBinding();

        Game.game.inputBindings.put(name, this);
    }

    public boolean isPressed()
    {
        return modifierPressed() && (input1.isPressed() || input2.isPressed());
    }

    public boolean isValid()
    {
        return modifierPressed() && (input1.isValid() || input2.isValid());
    }

    public boolean modifierPressed()
    {
        if (modifier < 0)
        {
            if (subModifiers == null || subModifiers.isEmpty())
                return true;
            return Game.game.window.pressedKeys.stream().noneMatch(k -> subModifiers.contains(k));
        }

        return Game.game.window.pressedKeys.contains(modifier);
    }

    public InputBindingGroup setModifier(int key)
    {
        InputBindingGroup b = new InputBindingGroup(name, input1, input2);
        b.modifier = key;
        if (subModifiers == null)
            subModifiers = new HashSet<>();
        subModifiers.add(key);
        return b;
    }

    public boolean doubleValid()
    {
        return input1.doubleValid() || input2.doubleValid();
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
        if (input1.inputType == null)
            return input2.getInputName();
        if ((input1.getInputName().startsWith("Left") && input2.getInputName().startsWith("Right")) ||
                (input2.getInputName().startsWith("Left") && input1.getInputName().startsWith("Right")))
            return Game.formatString(input1.getInputName().replace("Left ", "").replace("Right ", ""));
        return Translation.translate("%s or %s", Translation.translate(input1.getInputName()), Translation.translate(input2.getInputName()));
    }

    public void reset()
    {
        this.input1.reset();
        this.input2.reset();
    }
}
