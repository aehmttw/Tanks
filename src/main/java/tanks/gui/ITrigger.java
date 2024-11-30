package tanks.gui;

import tanks.gui.input.InputBindingGroup;

public interface ITrigger
{
    void update();

    void draw();

    default void onClick() {}

    default void doubleClick() {}

    void setPosition(double x, double y);

    default InputBindingGroup getKeybind() { return null; }

    default void updateKeybind()
    {
        InputBindingGroup keybind = getKeybind();

        if (keybind == null)
            return;

        if (keybind.isValid())
        {
            keybind.invalidate();
            onClick();

            if (keybind.doubleValid())
                doubleClick();
        }
    }
}
