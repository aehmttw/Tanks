package tanks.gui;

import tanks.Drawing;

import java.util.ArrayList;

public class ButtonList
{
    public ArrayList<Button> buttons;
    public int page;

    public int rows = 6;
    public int columns = 3;

    public int xOffset;
    public int yOffset;

    public int controlsYOffset;

    public boolean indexPrefix = false;

    Button next = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2, 350, 40, "Next page", new Runnable()
    {
        @Override
        public void run()
        {
            page++;
        }
    }
    );

    Button previous = new Button(Drawing.drawing.interfaceSizeX / 2 - 190, 0, 350, 40, "Previous page", new Runnable()
    {
        @Override
        public void run()
        {
            page--;
        }
    }
    );

    public ButtonList(ArrayList<Button> buttons, int page, int xOffset, int yOffset)
    {
        this.buttons = buttons;
        this.page = page;

        this.xOffset = xOffset;
        this.yOffset = yOffset;

        this.sortButtons();
    }

    public ButtonList(ArrayList<Button> buttons, int page, int xOffset, int yOffset, int controlsYOffset)
    {
        this.buttons = buttons;
        this.page = page;

        this.xOffset = xOffset;
        this.yOffset = yOffset;

        this.controlsYOffset = controlsYOffset;

        this.sortButtons();
    }

    public void sortButtons()
    {
        this.next.posX = Drawing.drawing.interfaceSizeX / 2 + 190 + xOffset;
        this.next.posY = Drawing.drawing.interfaceSizeY / 2 + ((rows + 3) / 2.0) * 60 + yOffset + controlsYOffset;

        this.next.image = "play.png";
        this.next.imageSizeX = 25;
        this.next.imageSizeY = 25;
        this.next.imageXOffset = 145;

        this.previous.image = "play.png";
        this.previous.imageSizeX = -25;
        this.previous.imageSizeY = 25;
        this.previous.imageXOffset = -145;

        this.previous.posX = Drawing.drawing.interfaceSizeX / 2 - 190 + xOffset;
        this.previous.posY = Drawing.drawing.interfaceSizeY / 2 + ((rows + 3) / 2.0) * 60 + yOffset + controlsYOffset;

        for (int i = 0; i < buttons.size(); i++)
        {
            int page = i / (rows * columns);

            int entries = rows * columns + Math.min(0, buttons.size() - (page + 1) * rows * columns);
            int cols = entries / rows + Math.min(1, entries % rows);

            int offset = -190 * (cols - 1);

            buttons.get(i).posY = Drawing.drawing.interfaceSizeY / 2 + yOffset + (i % rows - (rows - 1) / 2.0) * 60;
            buttons.get(i).posX = Drawing.drawing.interfaceSizeX / 2 + offset + ((i / rows) % columns) * 380 + xOffset;
        }
    }

    public void update()
    {
        while (page * rows * columns >= buttons.size() && page > 0)
            page--;

        for (int i = page * rows * columns; i < Math.min(page * rows * columns + rows * columns, buttons.size()); i++)
        {
            buttons.get(i).update();
        }

        previous.enabled = page > 0;
        next.enabled = buttons.size() > (1 + page) * rows * columns;

        if (rows * columns < buttons.size())
        {
            previous.update();
            next.update();
        }
    }

    public void draw()
    {
        previous.enabled = page > 0;
        next.enabled = buttons.size() > (1 + page) * rows * columns;

        if (rows * columns < buttons.size())
        {
            Drawing.drawing.setInterfaceFontSize(24);
            Drawing.drawing.setColor(0, 0, 0);
            Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2 + xOffset, 20 + Drawing.drawing.interfaceSizeY / 2 + yOffset + controlsYOffset + ((rows + 1) / 2.0) * 60,
                    "Page " + (page + 1) + " of " + (buttons.size() / (rows * columns) + Math.min(1, buttons.size() % (rows * columns))));

            previous.draw();
            next.draw();
        }

        for (int i = Math.min(page * rows * columns + rows * columns, buttons.size()) - 1; i >= page * rows * columns; i--)
        {
            Button b = buttons.get(i);
            String n = b.text;

            if (indexPrefix)
                b.text = (i + 1) + ". " + n;

            b.draw();

            if (indexPrefix)
                b.text = n;
        }
    }
}
