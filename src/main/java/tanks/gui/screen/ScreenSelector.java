package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.Selector;

import java.util.ArrayList;

public class ScreenSelector extends Screen
{
    public Screen screen;
    public Selector selector;

    public int rows = 6;
    public int yoffset = -150;
    public int page = 0;

    public boolean drawImages = false;

    public String title;

    Button quit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 300, 350, 40, "Ok", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = screen;

            if (!selector.quick)
                selector.function.run();
        }
    }
    );

    Button next = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 240, 350, 40, "Next page", new Runnable()
    {
        @Override
        public void run()
        {
            page++;
        }
    }
    );

    Button previous = new Button(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 + 240, 350, 40, "Previous page", new Runnable()
    {
        @Override
        public void run()
        {
            page--;
        }
    }
    );

    public ArrayList<Button> buttons = new ArrayList<Button>();

    public ScreenSelector(Selector s, Screen sc)
    {
        this.screen = sc;
        this.selector = s;

        for (int i = 0; i < selector.options.length; i++)
        {
            String n = selector.options[i];

            if (selector.format)
                n = Game.formatString(n);

            int j = i;
            buttons.add(new Button(0, 0, 350, 40, n, new Runnable()
            {
                @Override
                public void run()
                {
                    selector.selectedOption = j;

                    if (selector.quick)
                    {
                        Game.screen = screen;
                        selector.function.run();
                    }
                }
            }
            ));
        }

        if (selector.quick)
            quit.text = "Back";

        this.sortButtons();

        this.music = sc.music;
        this.musicID = sc.musicID;

        this.title = "Select " + s.text.toLowerCase();
    }

    public void sortButtons()
    {
        for (int i = 0; i < buttons.size(); i++)
        {
            int page = i / (rows * 3);
            int offset = 0;

            if (page * rows * 3 + rows < buttons.size())
                offset = -190;

            if (page * rows * 3 + rows * 2 < buttons.size())
                offset = -380;

            buttons.get(i).posY = Drawing.drawing.interfaceSizeY / 2 + yoffset + (i % rows) * 60;

            if (i / rows % 3 == 0)
                buttons.get(i).posX = Drawing.drawing.interfaceSizeX / 2 + offset;
            else if (i / rows % 3 == 1)
                buttons.get(i).posX = Drawing.drawing.interfaceSizeX / 2 + offset + 380;
            else
                buttons.get(i).posX = Drawing.drawing.interfaceSizeX / 2 + offset + 380 * 2;
        }
    }

    @Override
    public void update()
    {
        for (int i = page * rows * 3; i < Math.min(page * rows * 3 + rows * 3, buttons.size()); i++)
        {
            buttons.get(i).enabled = i != selector.selectedOption || selector.quick;
            buttons.get(i).update();
        }

        quit.update();

        if (page > 0)
            previous.update();

        if (buttons.size() > (1 + page) * rows * 3)
            next.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        for (int i = page * rows * 3; i < Math.min(page * rows * 3 + rows * 3, buttons.size()); i++)
        {
            Button b = buttons.get(i);
            b.draw();

            if (drawImages)
            {
                Drawing.drawing.setColor(255, 255, 255);
                Drawing.drawing.drawInterfaceImage("/" + selector.options[i], b.posX - b.sizeX / 2 + b.sizeY / 2 + 10, b.posY, b.sizeY, b.sizeY);
            }
        }

        quit.draw();

        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 210, this.title);

        if (page > 0)
            previous.draw();

        if (buttons.size() > (1 + page) * rows * 3)
            next.draw();
    }
}
