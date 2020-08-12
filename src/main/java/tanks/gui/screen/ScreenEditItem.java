package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.*;
import tanks.hotbar.item.Item;
import tanks.hotbar.item.property.*;

import java.util.ArrayList;

public class ScreenEditItem extends Screen
{
    public Item item;
    public ItemScreen screen;

    public int rows = 4;
    public int yoffset = -120;

    public int page = 0;

    public ArrayList<ITrigger> properties = new ArrayList<>();

    public Button next = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 240, 350, 40, "Next page", new Runnable()
    {
        @Override
        public void run()
        {
            page++;
        }
    }
    );

    public Button previous = new Button(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 + 240, 350, 40, "Previous page", new Runnable()
    {
        @Override
        public void run()
        {
            page--;
        }
    }
    );

    public Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 300, 350, 40, "Ok", new Runnable()
    {
        @Override
        public void run()
        {
            item.exportProperties();
            screen.refreshItems();
            Game.screen = screen;
        }
    }
    );

    public Button delete = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 220, 350, 40, "Delete item", new Runnable()
    {
        @Override
        public void run()
        {
            screen.removeItem(item);
            Game.screen = screen;
        }
    }
    );

    public ScreenEditItem(Item item, ItemScreen s)
    {
        this.item = item;
        this.screen = s;

        this.music = s.music;
        this.musicID = s.musicID;

        for (ItemProperty p: this.item.properties.values())
        {
            String name = Game.formatString(p.name);

            if (p instanceof ItemPropertyInt)
            {
                TextBox t = new TextBox(0, 0, 350, 40, name, () -> {}, p.value + "");
                t.function = () ->
                {
                    if (t.inputText.length() == 0)
                        t.inputText = p.value + "";
                    else
                        p.value = Integer.parseInt(t.inputText);
                };

                t.allowLetters = false;
                t.allowSpaces = false;

                properties.add(t);
            }
            else if (p instanceof ItemPropertyDouble)
            {
                TextBox t = new TextBox(0, 0, 350, 40, name, () -> {}, p.value + "");
                t.function = () ->
                {
                    if (t.inputText.length() == 0)
                        t.inputText = p.value + "";
                    else
                        p.value = Double.parseDouble(t.inputText);
                };

                t.allowDoubles = true;
                t.allowLetters = false;
                t.allowSpaces = false;

                properties.add(t);
            }
            else if (p instanceof ItemPropertyString)
            {
                TextBox t = new TextBox(0, 0, 350, 40, name, () -> {}, p.value + "");
                t.function = () ->
                {
                    if (t.inputText.length() == 0)
                        t.inputText = (String) p.value;
                    else
                        p.value = t.inputText;
                };

                t.enableCaps = true;
                t.allowSpaces = true;

                properties.add(t);
            }
            else if (p instanceof ItemPropertyBoolean)
            {
                Selector t = new Selector(0, 0, 350, 40, name, new String[]{"Yes", "No"}, () -> {});

                if ((boolean) p.value)
                    t.selectedOption = 0;
                else
                    t.selectedOption = 1;

                t.function = () -> p.value = t.selectedOption == 0;

                properties.add(t);
            }
            else if (p instanceof ItemPropertySelector)
            {
                Selector t = new Selector(0, 0, 350, 40, name, ((ItemPropertySelector) p).values, () -> {});
                t.selectedOption = (int) p.value;

                t.function = () -> p.value = t.selectedOption;

                properties.add(t);
            }
            else if (p instanceof ItemPropertyImageSelector)
            {
                ImageSelector t = new ImageSelector(0, 0, 350, 40, name, ((ItemPropertyImageSelector) p).values, () -> {});
                t.selectedOption = (int) p.value;

                t.function = () -> p.value = t.selectedOption;

                properties.add(t);
            }
        }

        for (int i = 0; i < properties.size(); i++)
        {
            int page = i / (rows * 3);
            int offset = 0;

            if (page * rows * 3 + rows < properties.size())
                offset = -190;

            if (page * rows * 3 + rows * 2 < properties.size())
                offset = -380;

            double posY = Drawing.drawing.interfaceSizeY / 2 + yoffset + (i % rows) * 90;

            if (i / rows % 3 == 0)
                properties.get(i).setPosition(Drawing.drawing.interfaceSizeX / 2 + offset, posY);
            else if (i / rows % 3 == 1)
                properties.get(i).setPosition(Drawing.drawing.interfaceSizeX / 2 + offset + 380, posY);
            else
                properties.get(i).setPosition(Drawing.drawing.interfaceSizeX / 2 + offset + 380 * 2, posY);
        }
    }

    @Override
    public void update()
    {
        for (int i = page * rows * 3; i < Math.min(page * rows * 3 + rows * 3, properties.size()); i++)
        {
            properties.get(i).update();
        }

        back.update();
        delete.update();

        if (page > 0)
            previous.update();

        if (properties.size() > (1 + page) * rows * 3)
            next.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        back.draw();
        delete.draw();

        if (page > 0)
            previous.draw();

        if (properties.size() > (1 + page) * rows * 3)
            next.draw();

        for (int i = page * rows * 3; i < Math.min(page * rows * 3 + rows * 3, properties.size()); i++)
        {
            properties.get(i).draw();
        }

        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.setInterfaceFontSize(24);
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 270, item.getTypeName() + " item properties");
    }
}
