package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.gui.*;
import tanks.hotbar.item.Item;
import tanks.hotbar.item.property.*;

import java.util.ArrayList;

public class ScreenEditItem extends Screen implements IOverlayScreen
{
    public Item item;
    public IItemScreen screen;

    public int rows = 4;
    public int yoffset = -150;

    public int page = 0;

    public boolean drawBehindScreen = false;

    public ArrayList<ITrigger> properties = new ArrayList<>();

    public Button next = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 240, this.objWidth, this.objHeight, "Next page", new Runnable()
    {
        @Override
        public void run()
        {
            page++;
        }
    }
    );

    public Button previous = new Button(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 + 240, this.objWidth, this.objHeight, "Previous page", new Runnable()
    {
        @Override
        public void run()
        {
            page--;
        }
    }
    );

    public Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 300, this.objWidth, this.objHeight, "Ok", new Runnable()
    {
        @Override
        public void run()
        {
            item.exportProperties();
            screen.refreshItems();
            Game.screen = (Screen) screen;
        }
    }
    );

    public Button delete = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 250, this.objWidth, this.objHeight, "Delete item", new Runnable()
    {
        @Override
        public void run()
        {
            screen.removeItem(item);
            Game.screen = (Screen) screen;
        }
    }
    );

    public ScreenEditItem(Item item, IItemScreen s)
    {
        this.item = item;
        this.screen = s;

        this.music = ((Screen)s).music;
        this.musicID = ((Screen)s).musicID;

        for (ItemProperty p: this.item.properties.values())
        {
            String name = Game.formatString(p.name);

            if (p instanceof ItemPropertyInt)
            {
                TextBox t = new TextBox(0, 0, this.objWidth, this.objHeight, name, () -> {}, p.value + "");
                t.function = () ->
                {
                    if (t.inputText.length() == 0)
                        t.inputText = p.value + "";
                    else
                        p.value = Integer.parseInt(t.inputText);
                };

                t.maxChars = 9;
                t.allowLetters = false;
                t.allowSpaces = false;

                properties.add(t);
            }
            else if (p instanceof ItemPropertyDouble)
            {
                TextBox t = new TextBox(0, 0, this.objWidth, this.objHeight, name, () -> {}, p.value + "");
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
                TextBox t = new TextBox(0, 0, this.objWidth, this.objHeight, name, () -> {}, p.value + "");
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
                Selector t = new Selector(0, 0, this.objWidth, this.objHeight, name, new String[]{"Yes", "No"}, () -> {});

                if ((boolean) p.value)
                    t.selectedOption = 0;
                else
                    t.selectedOption = 1;

                t.function = () -> p.value = t.selectedOption == 0;

                t.drawBehindScreen = this.drawBehindScreen;

                properties.add(t);
            }
            else if (p instanceof ItemPropertySelector)
            {
                Selector t = new Selector(0, 0, this.objWidth, this.objHeight, name, ((ItemPropertySelector) p).values, () -> {});
                t.selectedOption = (int) p.value;

                t.function = () -> p.value = t.selectedOption;

                t.drawBehindScreen = this.drawBehindScreen;

                properties.add(t);
            }
            else if (p instanceof ItemPropertyImageSelector)
            {
                ImageSelector t = new ImageSelector(0, 0, this.objWidth, this.objHeight, name, ((ItemPropertyImageSelector) p).values, () -> {});
                t.selectedOption = (int) p.value;

                t.function = () -> p.value = t.selectedOption;

                t.drawBehindScreen = this.drawBehindScreen;

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
            if (properties.get(i) instanceof Selector)
                ((Selector) properties.get(i)).drawBehindScreen = this.drawBehindScreen;

            properties.get(i).update();
        }

        back.update();
        delete.update();

        previous.enabled = page > 0;
        next.enabled = (properties.size() > (1 + page) * rows * 3);

        if (rows * 3 < properties.size())
        {
            previous.update();
            next.update();
        }

        if (Game.game.input.editorPause.isValid())
        {
            back.function.run();
            Game.game.input.editorPause.invalidate();
        }
    }

    @Override
    public void draw()
    {
        if (this.drawBehindScreen)
            ((Screen)this.screen).draw();
        else
            this.drawDefaultBackground();

        if (Game.screen instanceof ScreenSelector)
            return;

        back.draw();
        delete.draw();

        previous.enabled = page > 0;
        next.enabled = (properties.size() > (1 + page) * rows * 3);

        if (rows * 3 < properties.size())
        {
            previous.draw();
            next.draw();

            if (Level.currentColorR + Level.currentColorG + Level.currentColorB < 127 * 3)
                Drawing.drawing.setColor(255, 255, 255);
            else
                Drawing.drawing.setColor(0, 0, 0);

            Drawing.drawing.setInterfaceFontSize(24);
            Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 200,
                    "Page " + (page + 1) + " of " + (properties.size() / (rows * 3) + Math.min(1, properties.size() % (rows * 3))));
        }

        for (int i = Math.min(page * rows * 3 + rows * 3, properties.size()) - 1; i >= page * rows * 3; i--)
        {
            properties.get(i).draw();
        }

        if (Level.currentColorR + Level.currentColorG + Level.currentColorB < 127 * 3)
            Drawing.drawing.setColor(255, 255, 255);
        else
            Drawing.drawing.setColor(0, 0, 0);

        Drawing.drawing.setInterfaceFontSize(24);
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 300, item.getTypeName() + " item properties");
    }

    @Override
    public boolean showOverlay()
    {
        return false;
    }
}
