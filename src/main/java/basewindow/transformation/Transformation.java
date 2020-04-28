package basewindow.transformation;

import basewindow.BaseWindow;

public abstract class Transformation
{
    public BaseWindow window;

    public Transformation(BaseWindow window)
    {
        this.window = window;
    }

    public abstract void apply();
}
