package tanks.gui;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.screen.ScreenSelector;

public class ImageSelector extends Selector
{
    public ImageSelector(double x, double y, double sX, double sY, String text, String[] o, Runnable f)
    {
        super(x, y, sX, sY, text, o, f);
        this.format = false;
    }

    public ImageSelector(double x, double y, double sX, double sY, String text, String[] o, Runnable f, String hoverText)
    {
        super(x, y, sX, sY, text, o, f, hoverText);
        this.format = false;
    }

    public ImageSelector(double x, double y, double sX, double sY, String text, String[] o)
    {
        super(x, y, sX, sY, text, o);
        this.format = false;
    }

    public ImageSelector(double x, double y, double sX, double sY, String text, String[] o, String hoverText)
    {
        super(x, y, sX, sY, text, o, hoverText);
        this.format = false;
    }

    @Override
    public void draw()
    {
        super.draw();

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.drawInterfaceImage(options[selectedOption], this.posX - this.sizeX / 2 + this.sizeY / 2 + 10, this.posY, this.sizeY, this.sizeY);
    }

    @Override
    public void setScreen()
    {
        ScreenSelector s = new ScreenSelector(this, Game.screen);
        s.drawImages = true;
        Game.screen = s;
    }
}
