package tanks.gui;

import tanks.gui.input.InputBindingGroup;

public interface ITrigger
{
    void update();

    void draw();

    default void onClick() {}

    default void doubleClick() {}

    void setPosition(double x, double y);

    double getPositionX();

    double getPositionY();

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

    /**
     * @return Size, in number of textboxes, this trigger takes up in a tanksonable editor
     */
    default int getSize()
    {
        return 1;
    }
}
