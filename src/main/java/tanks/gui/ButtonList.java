package tanks.gui;

import tanks.BiConsumer;
import tanks.Drawing;
import tanks.Level;

import java.util.ArrayList;

public class ButtonList
{
    public ArrayList<Button> buttons;

    public ArrayList<Button> upButtons = new ArrayList<>();
    public ArrayList<Button> downButtons = new ArrayList<>();

    public boolean arrowsEnabled = false;
    public boolean reorder = false;

    public int page;

    public int rows = 6;
    public int columns = 3;

    public double xOffset;
    public double yOffset;

    public double controlsYOffset;

    public boolean indexPrefix = false;

    public double objWidth = 350;
    public double objHeight = 40;
    public double objXSpace = 380;
    public double objYSpace = 60;

    public boolean hideText = false;

    public BiConsumer<Integer, Integer> reorderBehavior;

    Button next = new Button(Drawing.drawing.interfaceSizeX / 2 + this.objXSpace / 2, Drawing.drawing.interfaceSizeY / 2, this.objWidth, this.objHeight, "Next page", new Runnable()
    {
        @Override
        public void run()
        {
            page++;
        }
    }
    );

    Button previous = new Button(Drawing.drawing.interfaceSizeX / 2 - this.objXSpace / 2, 0, this.objWidth, this.objHeight, "Previous page", new Runnable()
    {
        @Override
        public void run()
        {
            page--;
        }
    }
    );

    public ButtonList(ArrayList<Button> buttons, int page, double xOffset, double yOffset)
    {
        this.buttons = buttons;
        this.page = page;

        this.xOffset = xOffset;
        this.yOffset = yOffset;

        this.sortButtons();
    }

    public ButtonList(ArrayList<Button> buttons, int page, double xOffset, double yOffset, double controlsYOffset)
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
        this.next.posX = Drawing.drawing.interfaceSizeX / 2 + this.objXSpace / 2 + xOffset;
        this.next.posY = Drawing.drawing.interfaceSizeY / 2 + ((rows + 3) / 2.0) * this.objYSpace + yOffset + controlsYOffset;

        this.next.image = "play.png";
        this.next.imageSizeX = 25;
        this.next.imageSizeY = 25;
        this.next.imageXOffset = 145;

        this.previous.image = "play.png";
        this.previous.imageSizeX = -25;
        this.previous.imageSizeY = 25;
        this.previous.imageXOffset = -145;

        this.previous.posX = Drawing.drawing.interfaceSizeX / 2 - this.objXSpace / 2 + xOffset;
        this.previous.posY = Drawing.drawing.interfaceSizeY / 2 + ((rows + 3) / 2.0) * this.objYSpace + yOffset + controlsYOffset;

        for (int i = 0; i < buttons.size(); i++)
        {
            int page = i / (rows * columns);

            int entries = rows * columns + Math.min(0, buttons.size() - (page + 1) * rows * columns);
            int cols = entries / rows + Math.min(1, entries % rows);

            double offset = -this.objXSpace / 2 * (cols - 1);

            buttons.get(i).posY = Drawing.drawing.interfaceSizeY / 2 + yOffset + (i % rows - (rows - 1) / 2.0) * this.objYSpace;
            buttons.get(i).posX = Drawing.drawing.interfaceSizeX / 2 + offset + ((i / rows) % columns) * this.objXSpace + xOffset;
            buttons.get(i).sizeX = this.objWidth;
            buttons.get(i).sizeY = this.objHeight;

            if (hideText)
                buttons.get(i).text = "";
        }

        if (this.arrowsEnabled)
            this.setupArrows();
    }

    public void setupArrows()
    {
        this.arrowsEnabled = true;

        this.upButtons.clear();
        this.downButtons.clear();

        for (int i = 0; i < this.buttons.size(); i++)
        {
            Button b = this.buttons.get(i);

            int finalI = i;

            Button up = new Button(b.posX + b.sizeX / 2 - b.sizeY / 2 - b.sizeY, b.posY, b.sizeY * 0.8, b.sizeY * 0.8, "", new Runnable()
            {
                @Override
                public void run()
                {
                    reorderBehavior.accept(finalI - 1, finalI);
                }
            });

            up.image = "vertical_arrow.png";
            up.imageSizeX = 15;
            up.imageSizeY = 15;
            this.upButtons.add(up);

            Button down = new Button(b.posX + b.sizeX / 2 - b.sizeY / 2, b.posY, b.sizeY * 0.8, b.sizeY * 0.8, "", new Runnable()
            {
                @Override
                public void run()
                {
                    reorderBehavior.accept(finalI + 1, finalI);
                }
            });

            down.image = "vertical_arrow.png";
            down.imageSizeX = 15;
            down.imageSizeY = -15;
            this.downButtons.add(down);
        }
    }

    public void update()
    {
        while (page * rows * columns >= buttons.size() && page > 0)
            page--;

        if (this.arrowsEnabled && this.buttons.size() > 0)
        {
            upButtons.get(0).enabled = false;
            downButtons.get(downButtons.size() - 1).enabled = false;
        }

        for (int i = page * rows * columns; i < Math.min(page * rows * columns + rows * columns, buttons.size()); i++)
        {
            if (this.arrowsEnabled)
                buttons.get(i).enabled = !this.reorder;

            buttons.get(i).update();

            if (this.reorder)
            {
                upButtons.get(i).update();
                downButtons.get(i).update();
            }
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

        if (this.arrowsEnabled && this.buttons.size() > 0)
        {
            upButtons.get(0).enabled = false;
            downButtons.get(downButtons.size() - 1).enabled = false;
        }

        if (rows * columns < buttons.size())
        {
            Drawing.drawing.setInterfaceFontSize(objHeight * 0.6);

            if (Level.isDark())
                Drawing.drawing.setColor(255, 255, 255);
            else
                Drawing.drawing.setColor(0, 0, 0);

            Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2 + xOffset, 20 + Drawing.drawing.interfaceSizeY / 2 + yOffset + controlsYOffset + ((rows + 1) / 2.0) * this.objYSpace,
                    "Page " + (page + 1) + " of " + (buttons.size() / (rows * columns) + Math.min(1, buttons.size() % (rows * columns))));

            previous.draw();
            next.draw();
        }

        for (int i = Math.min(page * rows * columns + rows * columns, buttons.size()) - 1; i >= page * rows * columns; i--)
        {
            Button b = buttons.get(i);
            String n = b.text;

            if (this.arrowsEnabled)
                buttons.get(i).enabled = !this.reorder;

            if (indexPrefix)
                b.text = (i + 1) + ". " + n;

            b.draw();

            if (indexPrefix)
                b.text = n;

            if (this.reorder)
            {
                upButtons.get(i).draw();
                downButtons.get(i).draw();
            }
        }
    }
}
