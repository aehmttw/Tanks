package tanks.gui;

import tanks.BiConsumer;
import tanks.Drawing;
import tanks.Level;
import tanks.translation.Translation;

import java.util.ArrayList;

public class ButtonList
{
    public ArrayList<Button> buttons;

    public ArrayList<Button> upButtons = new ArrayList<>();
    public ArrayList<Button> downButtons = new ArrayList<>();

    public boolean arrowsEnabled = false;
    public boolean reorder = false;

    /** This many elements at the start cannot be reordered */
    public int fixedFirstElements = 0;

    /** This many elements at the end cannot be reordered */
    public int fixedLastElements = 0;

    public int page;

    public int rows = 6;
    public int columns = 3;

    public boolean horizontalLayout = false;

    public double xOffset;
    public double yOffset;

    public double controlsYOffset;

    public boolean indexPrefix = false;

    public double objWidth = 350;
    public double objHeight = 40;
    public double objXSpace = 380;
    public double objYSpace = 60;

    public double buttonWidth = 350;
    public double buttonHeight = 40;
    public double buttonXSpace = 380;
    public double buttonYSpace = 60;

    public boolean shiftWhenNoPages = true;

    public double imageR = 255;
    public double imageG = 255;
    public double imageB = 255;

    /** If set, text will be white if the level is dark */
    public boolean defaultDarkMode = true;

    /** If set, text will be white unconditionally */
    public boolean manualDarkMode = false;

    public boolean translate = false;

    public boolean hideText = false;

    public BiConsumer<Integer, Integer> reorderBehavior;

    public Button next = new Button(Drawing.drawing.interfaceSizeX / 2 + this.objXSpace / 2, Drawing.drawing.interfaceSizeY / 2, this.objWidth, this.objHeight, "Next page", new Runnable()
    {
        @Override
        public void run()
        {
            page++;
        }
    }
    );

    public Button previous = new Button(Drawing.drawing.interfaceSizeX / 2 - this.objXSpace / 2, 0, this.objWidth, this.objHeight, "Previous page", new Runnable()
    {
        @Override
        public void run()
        {
            page--;
        }
    }
    );

    public Button first = new Button(Drawing.drawing.interfaceSizeX / 2 - this.objXSpace - this.objHeight * 2, Drawing.drawing.interfaceSizeY / 2, this.objHeight, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            page = 0;
        }
    }
    );

    public Button last = new Button(Drawing.drawing.interfaceSizeX / 2 + this.objXSpace + this.objHeight * 2, Drawing.drawing.interfaceSizeY / 2, this.objHeight, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            page = (buttons.size() - 1) / rows / columns;
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

    protected ButtonList()
    {

    }

    public void sortButtons()
    {
        this.next.posX = Drawing.drawing.interfaceSizeX / 2 + this.objXSpace / 2 + xOffset;
        this.next.posY = Drawing.drawing.interfaceSizeY / 2 + ((rows + 3) / 2.0) * this.objYSpace + yOffset + controlsYOffset;

        this.previous.posX = Drawing.drawing.interfaceSizeX / 2 - this.objXSpace / 2 + xOffset;
        this.previous.posY = Drawing.drawing.interfaceSizeY / 2 + ((rows + 3) / 2.0) * this.objYSpace + yOffset + controlsYOffset;

        this.last.posX = this.next.posX + this.objXSpace / 2 + this.objHeight / 2;
        this.last.posY = this.next.posY;

        this.first.posX = this.previous.posX - this.objXSpace / 2 - this.objHeight / 2;
        this.first.posY = this.previous.posY;

        this.next.image = "icons/forward.png";
        this.next.imageSizeX = 25;
        this.next.imageSizeY = 25;
        this.next.imageXOffset = 145;

        this.previous.image = "icons/back.png";
        this.previous.imageSizeX = 25;
        this.previous.imageSizeY = 25;
        this.previous.imageXOffset = -145;

        this.last.image = "icons/last.png";
        this.last.imageSizeX = 20;
        this.last.imageSizeY = 20;
        this.last.imageXOffset = 0;

        this.first.image = "icons/first.png";
        this.first.imageSizeX = 20;
        this.first.imageSizeY = 20;
        this.first.imageXOffset = 0;

        double oy = shiftWhenNoPages && (this.buttons.size() <= rows * columns) ? 30 : 0;

        for (int i = 0; i < buttons.size(); i++)
        {
            int page = i / (rows * columns);

            int entries = rows * columns + Math.min(0, buttons.size() - (page + 1) * rows * columns);
            int cols = entries / rows + Math.min(1, entries % rows);
            int rs = Math.min(rows, buttons.size());

            int r = i % rows;
            int c = ((i / rows) % columns);

            if (horizontalLayout)
            {
                r = (i / columns) % rows;
                c = i % columns;
                cols = Math.min(columns, buttons.size());
                rs = Math.min(buttons.size() / cols + Math.min(1, entries % cols), rows);
            }

            double offset = -this.buttonXSpace / 2 * (cols - 1);

            buttons.get(i).posY = Drawing.drawing.interfaceSizeY / 2 + yOffset + (r - (rs - 1) / 2.0) * this.buttonYSpace + oy;
            buttons.get(i).posX = Drawing.drawing.interfaceSizeX / 2 + offset + c * this.buttonXSpace + xOffset;
            buttons.get(i).sizeX = this.buttonWidth;
            buttons.get(i).sizeY = this.buttonHeight;
            buttons.get(i).translated = this.translate;
            buttons.get(i).imageR = this.imageR;
            buttons.get(i).imageG = this.imageG;
            buttons.get(i).imageB = this.imageB;

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

            double s = this.objHeight * 0.8;

            Button up = new Button(b.posX + b.sizeX / 2 - b.sizeY / 2 - b.sizeY, b.posY, s, s, "", () -> reorderBehavior.accept(finalI - 1, finalI));

            up.image = "icons/arrow_up.png";
            up.imageSizeX = 15;
            up.imageSizeY = 15;
            this.upButtons.add(up);

            Button down = new Button(b.posX + b.sizeX / 2 - b.sizeY / 2, b.posY, s, s, "", () -> reorderBehavior.accept(finalI + 1, finalI));

            down.image = "icons/arrow_down.png";
            down.imageSizeX = 15;
            down.imageSizeY = 15;
            this.downButtons.add(down);

            if (this.objHeight != this.buttonHeight)
            {
                up.posX = b.posX - this.objHeight / 2;
                up.posY = b.posY + b.sizeY / 2 - this.objHeight / 2;
                down.posX = b.posX + this.objHeight / 2;
                down.posY = b.posY + b.sizeY / 2 - this.objHeight / 2;
            }

            if (horizontalLayout)
            {
                up.image = "icons/back.png";
                down.image = "icons/forward.png";
            }
        }
    }

    public void update()
    {
        while (page * rows * columns >= buttons.size() && page > 0)
            page--;


        for (int n = 0; n < this.upButtons.size(); n++)
        {
            this.upButtons.get(n).enabled = true;
            this.downButtons.get(n).enabled = true;
        }

        if (this.arrowsEnabled && this.buttons.size() > this.fixedFirstElements)
        {
            upButtons.get(this.fixedFirstElements).enabled = false;
            downButtons.get(downButtons.size() - 1 - this.fixedLastElements).enabled = false;
        }

        for (int i = page * rows * columns; i < Math.min(page * rows * columns + rows * columns, buttons.size()); i++)
        {
            boolean e = this.buttons.get(i).enabled;
            if (this.arrowsEnabled && this.reorder)
                this.buttons.get(i).enabled = false;

            buttons.get(i).update();

            if (this.arrowsEnabled && this.reorder)
                this.buttons.get(i).enabled = e;

            if (this.reorder && i >= this.fixedFirstElements && this.buttons.size() - i > this.fixedLastElements)
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

            if ((buttons.size() - 1) / rows / columns >= 2)
            {
                last.update();
                first.update();
            }
        }
    }

    public void draw()
    {
        previous.enabled = page > 0;
        next.enabled = buttons.size() > (1 + page) * rows * columns;

        first.enabled = previous.enabled;
        last.enabled = next.enabled;

        if (this.arrowsEnabled && this.buttons.size() > this.fixedFirstElements)
        {
            upButtons.get(fixedFirstElements).enabled = false;
            downButtons.get(downButtons.size() - fixedLastElements - 1).enabled = false;
        }

        if (rows * columns < buttons.size())
        {
            Drawing.drawing.setInterfaceFontSize(objHeight * 0.6);

            if (Level.isDark() && defaultDarkMode || manualDarkMode)
                Drawing.drawing.setColor(255, 255, 255);
            else
                Drawing.drawing.setColor(0, 0, 0);

            Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2 + xOffset, 20 + Drawing.drawing.interfaceSizeY / 2 + yOffset + controlsYOffset + ((rows + 1) / 2.0) * this.objYSpace,
                    Translation.translate("Page %d of %d", (page + 1), (buttons.size() / (rows * columns) + Math.min(1, buttons.size() % (rows * columns)))));

            previous.draw();
            next.draw();

            if ((buttons.size() - 1) / rows / columns >= 2)
            {
                last.draw();
                first.draw();
            }
        }

        for (int i = Math.min(page * rows * columns + rows * columns, buttons.size()) - 1; i >= page * rows * columns; i--)
        {
            Button b = buttons.get(i);
            String n = b.text;

            boolean e = b.enabled;
            boolean hover = b.enableHover;
            if (this.arrowsEnabled && this.reorder)
            {
                b.enabled = false;
                b.enableHover = false;
            }

            if (indexPrefix)
                b.text = (i + 1) + ". " + n;


            b.draw();

            if (indexPrefix)
                b.text = n;

            if (this.reorder && i >= this.fixedFirstElements && this.buttons.size() - i > this.fixedLastElements)
            {
                upButtons.get(i).draw();
                downButtons.get(i).draw();
            }

            b.enabled = e;
            b.enableHover = hover;
        }
    }

    public SavedFilesList clone()
    {
        SavedFilesList s = new SavedFilesList();
        s.page = this.page;
        s.xOffset = this.xOffset;
        s.yOffset = this.yOffset;
        s.buttons = new ArrayList<>();
        s.buttons.addAll(this.buttons);

        return s;
    }

    public void filter(String s)
    {
        for (int i = 0; i < this.buttons.size(); i++)
        {
            if (!buttons.get(i).text.toLowerCase().contains(s.toLowerCase()))
            {
                buttons.remove(i);
                i--;
            }
        }
    }
}
