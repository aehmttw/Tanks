package tanks.gui;

import tanks.Drawing;
import tanks.Game;
import tanks.IDrawable;

import java.util.ArrayList;

public class ScrollList implements IDrawable, ITrigger
{
    public double posX;
    public double posY;
    public double sizeX;
    public double sizeY;
    public Object[] listItems;

    public double margin = Drawing.drawing.objHeight + 10;
    public double scroll = 0;
    public double scrollPixels = 50;

    public ScrollList(double x, double y, double sX, double sY, ArrayList<Object> listItems) {
        this(x, y, sX, sY, listItems.toArray(new Object[0]));
    }

    public ScrollList(double x, double y, double sX, double sY, Object... listItems)
    {
        this.posX = x;
        this.posY = y;
        this.sizeX = sX;
        this.sizeY = sY;
        this.listItems = listItems;
    }

    @Override
    public void update()
    {
        if (Game.game.window.validScrollUp) {
            scroll += scrollPixels;
            Game.game.window.validScrollUp = false;
        }

        if (Game.game.window.validScrollDown) {
            scroll -= scrollPixels;
            Game.game.window.validScrollDown = false;
        }

        for (Object o : this.listItems)
        {
            if (o instanceof ITrigger)
                ((ITrigger) o).update();
        }
    }

    @Override
    public void draw()
    {
        for (int i = 0; i < this.listItems.length; i++)
        {
            double x = this.posX;
            double y = this.posY + scroll + i * margin;

            if (y < this.posY || y > this.posY + this.sizeY)
                continue;

            Object o = this.listItems[i];

            if (o instanceof ITrigger)
            {
                ((ITrigger) o).setPosition(x, y);
                ((ITrigger) o).draw();
            }
            else if (o instanceof String)
                Drawing.drawing.drawInterfaceText(x, y, (String) o);
        }
    }

    @Override
    public void setPosition(double x, double y) {
        this.posX = x;
        this.posY = y;
    }
}
