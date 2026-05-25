package tanks;

import basewindow.Color;

public interface IDrawableLightSource extends IDrawable
{
    boolean lit();

    // 50 brightness = diameter of 1 tile
    double getBrightness();

    Color getColor();
}
