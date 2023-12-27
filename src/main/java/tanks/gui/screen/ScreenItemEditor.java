package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.gui.*;
import tanks.hotbar.item.Item;
import tanks.gui.property.*;
import tanks.translation.Translation;

import java.io.IOException;
import java.util.ArrayList;

public class ScreenItemEditor extends Screen implements IConditionalOverlayScreen
{
    public Item item;
    public IItemScreen screen;

    public int rows = 4;
    public int yoffset = -150;

    public int page = 0;

    public boolean drawBehindScreen = false;
    public String message = null;

    public ArrayList<ITrigger> properties = new ArrayList<>();

    public Button next = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 240, this.objWidth, this.objHeight, "Next page", () -> page++);

    public Button previous = new Button(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 + 240, this.objWidth, this.objHeight, "Previous page", () -> page--);

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

    public Button dismissMessage = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + Drawing.drawing.objHeight, Drawing.drawing.objWidth, Drawing.drawing.objHeight, "Ok", () -> message = null);

    public Button delete = new Button(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 - 250, this.objWidth, this.objHeight, "Delete item", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = (Screen) screen;
            screen.removeItem(item);
        }
    }
    );

    public Button save = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 - 250, this.objWidth, this.objHeight, "Save to template", new Runnable()
    {
        @Override
        public void run()
        {
            item.exportProperties();
            BaseFile f = Game.game.fileManager.getFile(Game.homedir + Game.itemDir + "/" + item.name.replace(" ", "_") + ".tanks");

            if (!f.exists())
            {
                try
                {
                    f.create();
                    f.startWriting();
                    f.println(item.toString());
                    f.stopWriting();

                    message = "Item added to templates!";
                }
                catch (IOException e)
                {
                    Game.exitToCrash(e);
                }
            }
            else
                message = "An item template with this name already exists!";
        }
    }
    );

    public ScreenItemEditor(Item item, IItemScreen s)
    {
        this(item, s, false, false);
    }

    public ScreenItemEditor(Item item, IItemScreen s, boolean omitPrice, boolean omitUnlockLevel)
    {
        super(350, 40, 380, 60);

        this.allowClose = false;

        this.next.image = "icons/forward.png";
        this.next.imageSizeX = 25;
        this.next.imageSizeY = 25;
        this.next.imageXOffset = 145;

        this.previous.image = "icons/back.png";
        this.previous.imageSizeX = 25;
        this.previous.imageSizeY = 25;
        this.previous.imageXOffset = -145;

        this.item = item;
        this.screen = s;

        this.music = ((Screen)s).music;
        this.musicID = ((Screen)s).musicID;

        for (UIProperty p: this.item.properties.values())
        {
            String name = Game.formatString(p.name);

            if (p.name.equals("price") && omitPrice)
                continue;

            if (p.name.equals("unlocks-after-level") && omitUnlockLevel)
                continue;

            if (p instanceof UIPropertyInt)
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
                t.allowNegatives = true;

                properties.add(t);
            }
            else if (p instanceof UIPropertyDouble)
            {
                TextBox t = new TextBox(0, 0, this.objWidth, this.objHeight, name, () -> {}, p.value + "");
                t.function = () ->
                {
                    try
                    {
                        if (t.inputText.length() == 0)
                            t.inputText = p.value + "";
                        else
                            p.value = Double.parseDouble(t.inputText);
                    }
                    catch (Exception e)
                    {
                        p.value = 0.0;
                        t.inputText = "0.0";
                    }
                };

                t.allowDoubles = true;
                t.allowLetters = false;
                t.allowSpaces = false;
                t.allowNegatives = true;

                properties.add(t);
            }
            else if (p instanceof UIPropertyString)
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
            else if (p instanceof UIPropertyBoolean)
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
            else if (p instanceof UIPropertySelector)
            {
                Selector t = new Selector(0, 0, this.objWidth, this.objHeight, name, ((UIPropertySelector) p).values, () -> {});
                t.selectedOption = (int) p.value;
                t.images = ((UIPropertySelector) p).images;

                t.function = () -> p.value = t.selectedOption;

                t.drawBehindScreen = this.drawBehindScreen;

                properties.add(t);
            }
            else if (p instanceof UIPropertyImageSelector)
            {
                SelectorImage t = new SelectorImage(0, 0, this.objWidth, this.objHeight, name, ((UIPropertyImageSelector) p).values, () -> {});
                t.drawImages = true;
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
        if (this.message != null)
            this.dismissMessage.update();
        else
        {
            for (int i = page * rows * 3; i < Math.min(page * rows * 3 + rows * 3, properties.size()); i++)
            {
                if (properties.get(i) instanceof Selector)
                    ((Selector) properties.get(i)).drawBehindScreen = this.drawBehindScreen;

                properties.get(i).update();
            }

            back.update();
            delete.update();
            save.update();

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
    }

    @Override
    public void draw()
    {
        if (this.drawBehindScreen)
        {
            this.enableMargins = ((Screen) this.screen).enableMargins;
            ((Screen) this.screen).draw();
        }
        else
        {
            Drawing.drawing.setLighting(Level.currentLightIntensity, Math.max(Level.currentLightIntensity * 0.75, Level.currentShadowIntensity));
            this.drawDefaultBackground();
        }

        if (Game.screen instanceof ScreenSelector)
            return;

        if (this.message != null)
        {
            this.dismissMessage.draw();

            if (Level.isDark())
                Drawing.drawing.setColor(255, 255, 255);
            else
                Drawing.drawing.setColor(0, 0, 0);

            Drawing.drawing.setInterfaceFontSize(Drawing.drawing.textSize);
            Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 60, this.message);
        }
        else
        {
            back.draw();
            delete.draw();
            save.draw();

            previous.enabled = page > 0;
            next.enabled = (properties.size() > (1 + page) * rows * 3);

            if (rows * 3 < properties.size())
            {
                previous.draw();
                next.draw();

                if (Level.isDark())
                    Drawing.drawing.setColor(255, 255, 255);
                else
                    Drawing.drawing.setColor(0, 0, 0);

                Drawing.drawing.setInterfaceFontSize(this.textSize);
                Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 200,
                        "Page %d of %d", (page + 1), (properties.size() / (rows * 3) + Math.min(1, properties.size() % (rows * 3))));
            }

            for (int i = Math.min(page * rows * 3 + rows * 3, properties.size()) - 1; i >= page * rows * 3; i--)
            {
                properties.get(i).draw();
            }

            if (Level.isDark())
                Drawing.drawing.setColor(255, 255, 255);
            else
                Drawing.drawing.setColor(0, 0, 0);

            Drawing.drawing.setInterfaceFontSize(this.titleSize);
            Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 300, "%s item properties", Translation.translate(item.getTypeName()));
        }
    }

    @Override
    public double getOffsetX()
    {
        if (drawBehindScreen)
            return ((Screen)screen).getOffsetX();
        else
            return super.getOffsetX();
    }

    @Override
    public double getOffsetY()
    {
        if (drawBehindScreen)
            return ((Screen)screen).getOffsetY();
        else
            return super.getOffsetY();
    }

    @Override
    public double getScale()
    {
        if (drawBehindScreen)
            return ((Screen)screen).getScale();
        else
            return super.getScale();
    }

    @Override
    public boolean isOverlayEnabled()
    {
        if (screen instanceof IConditionalOverlayScreen)
            return ((IConditionalOverlayScreen) screen).isOverlayEnabled();

        return screen instanceof ScreenGame || screen instanceof ILevelPreviewScreen || screen instanceof IOverlayScreen;
    }

    @Override
    public void onAttemptClose()
    {
        ((Screen)this.screen).onAttemptClose();
    }
}
