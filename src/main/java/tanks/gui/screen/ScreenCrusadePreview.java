package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.*;
import tanks.network.event.EventShareCrusade;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.gui.TextBox;
import tanks.hotbar.item.Item;

import java.io.IOException;
import java.util.ArrayList;

public class ScreenCrusadePreview extends Screen implements IItemScreen
{
    public enum Mode {options, levels, items}

    public Crusade crusade;
    public Mode mode = Mode.options;

    public ButtonList levelButtons;
    public ButtonList itemButtons;

    public ScreenCrusadePreview instance = this;
    public Screen previous;

    public double titleOffset = -this.objYSpace * 4.5;

    public boolean uploadMode;

    public TextBox crusadeName;
    public boolean downloaded = false;

    public Button upload = new Button(this.centerX, this.centerY + this.objYSpace * 4, this.objWidth, this.objHeight, "Share", new Runnable()
    {
        @Override
        public void run()
        {
            if (ScreenPartyHost.isServer)
            {
                Game.screen = ScreenPartyHost.activeScreen;
                EventShareCrusade e = new EventShareCrusade(crusade, crusade.name);
                e.clientID = Game.clientID;
                Game.eventsIn.add(e);
            }
            else
            {
                Game.screen = new ScreenPartyLobby();
                Game.eventsOut.add(new EventShareCrusade(crusade, crusade.name));
            }

            Game.cleanUp();
        }
    });

    public Button download = new Button(this.centerX, this.centerY + this.objYSpace * 4, this.objWidth, this.objHeight, "Download", new Runnable()
    {
        @Override
        public void run()
        {
            BaseFile file = Game.game.fileManager.getFile(Game.homedir + Game.crusadeDir + "/" + crusadeName.inputText.replace(" ", "_") + ".tanks");

            boolean success = false;
            if (!file.exists())
            {
                try
                {
                    if (file.create())
                    {
                        file.startWriting();
                        file.println(crusade.contents);
                        file.stopWriting();
                        success = true;
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace(Game.logger);
                    e.printStackTrace();
                }
            }

            if (success)
            {
                download.enabled = false;
                downloaded = true;
                download.setText("Crusade downloaded!");
            }
        }
    });

    public Button quit = new Button(this.centerX, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = previous;
        }
    }
    );

    public Button options = new Button(this.centerX - this.objXSpace, 60, this.objWidth, this.objHeight, "Overview", () -> mode = Mode.options);

    public Button levels = new Button(this.centerX, 60, this.objWidth, this.objHeight, "Levels", () -> mode = Mode.levels);

    public Button items = new Button(this.centerX + this.objXSpace, 60, this.objWidth, this.objHeight, "Shop", () -> mode = Mode.items);

    public ScreenCrusadePreview(Crusade c, Screen previous, boolean upload)
    {
        super(350, 40, 380, 60);

        this.previous = previous;
        this.uploadMode = upload;

        this.music = "menu_4.ogg";
        this.musicID = "menu";

        this.crusade = c;

        for (Item i: c.crusadeItems)
        {
            i.importProperties();
        }

        if (Drawing.drawing.interfaceScaleZoom > 1)
        {
            this.levelButtons = new ButtonList(new ArrayList<>(), 0, this.centerX - Drawing.drawing.interfaceSizeX / 2, this.centerY - Drawing.drawing.interfaceSizeY / 2);
            this.itemButtons = new ButtonList(new ArrayList<>(), 0,  this.centerX - Drawing.drawing.interfaceSizeX / 2, this.centerY - Drawing.drawing.interfaceSizeY / 2);

            this.titleOffset = -195;

            this.levelButtons.controlsYOffset = -30;
            this.itemButtons.controlsYOffset = -30;
        }
        else
        {
            this.levelButtons = new ButtonList(new ArrayList<>(), 0,  this.centerX - Drawing.drawing.interfaceSizeX / 2, this.centerY - Drawing.drawing.interfaceSizeY / 2 - 30);
            this.itemButtons = new ButtonList(new ArrayList<>(), 0,  this.centerX - Drawing.drawing.interfaceSizeX / 2, this.centerY - Drawing.drawing.interfaceSizeY / 2 - 30);
        }

        this.itemButtons.objYSpace = this.objYSpace;
        this.levelButtons.objYSpace = this.objYSpace;

        this.refreshLevelButtons();
        this.refreshItemButtons();

        this.levelButtons.indexPrefix = true;

        crusadeName = new TextBox(this.centerX, this.centerY + this.objYSpace * 3, this.objWidth, this.objHeight, "Crusade save name", () ->
        {
            if (crusadeName.inputText.equals(""))
                crusadeName.inputText = crusadeName.previousInputText;
            updateDownloadButton();
        }
                , crusade.name.replace("_", " "));

        crusadeName.enableCaps = true;

        this.updateDownloadButton();
    }

    public void refreshLevelButtons()
    {
        this.levelButtons.buttons.clear();

        for (int i = 0; i < this.crusade.levels.size(); i++)
        {
            int j = i;
            this.levelButtons.buttons.add(new Button(0, 0, this.objWidth, this.objHeight, this.crusade.levels.get(i).levelName.replace("_", " "), () ->
            {
                String level = crusade.levels.get(j).levelString;

                ScreenCrusadePreviewLevel s = new ScreenCrusadePreviewLevel(crusade, level, j, Game.screen);
                Level l = new Level(level);
                l.customTanks = crusade.customTanks;
                l.loadLevel(s);
                Game.screen = s;
            }));
        }

        this.levelButtons.sortButtons();
    }

    public void refreshItemButtons()
    {
        this.itemButtons.buttons.clear();

        for (int i = 0; i < this.crusade.crusadeItems.size(); i++)
        {
            Button b = new Button(0, 0, this.objWidth, this.objHeight, this.crusade.crusadeItems.get(i).name);

            b.image = crusade.crusadeItems.get(i).icon;
            b.imageXOffset = - b.sizeX / 2 + b.sizeY / 2 + 10;
            b.imageSizeX = b.sizeY;
            b.imageSizeY = b.sizeY;

            int p = crusade.crusadeItems.get(i).price;

            if (p == 0)
                b.setSubtext("Free!");
            else if (p == 1)
                b.setSubtext("1 coin");
            else
                b.setSubtext("%d coins", p);

            this.itemButtons.buttons.add(b);
        }

        this.itemButtons.sortButtons();
    }

    @Override
    public void update()
    {
        options.enabled = mode != Mode.options;
        levels.enabled = mode != Mode.levels;
        items.enabled = mode != Mode.items;

        options.update();
        levels.update();
        items.update();

        if (mode == Mode.levels)
        {
            levelButtons.update();

            quit.update();
        }
        else if (mode == Mode.options)
        {
            quit.update();

            if (uploadMode)
            {
                upload.update();
            }
            else
            {
                crusadeName.update();
                download.update();
            }
        }
        else if (mode == Mode.items)
        {
            itemButtons.update();
            quit.update();
        }
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        double extraHeight = ((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2;
        double width = Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale;

        Drawing.drawing.setColor(0, 0, 0, 127);
        Drawing.drawing.fillInterfaceRect(this.centerX, -extraHeight / 2, width, extraHeight);
        Drawing.drawing.fillInterfaceRect(this.centerX, 60, width, 120);

        options.draw();
        levels.draw();
        items.draw();

        if (mode == Mode.levels)
        {
            quit.draw();
            levelButtons.draw();

            Drawing.drawing.setInterfaceFontSize(this.titleSize);
            Drawing.drawing.setColor(0, 0, 0);
            Drawing.drawing.displayInterfaceText(this.centerX, this.centerY + titleOffset, "Crusade levels");
        }
        else if (mode == Mode.options)
        {
            Drawing.drawing.setInterfaceFontSize(this.textSize * 2);
            Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 1.5, crusade.name.replace("_", " "));
            Drawing.drawing.setInterfaceFontSize(this.textSize);
            Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 0.5, "Levels: %d", crusade.levels.size());
            Drawing.drawing.displayInterfaceText(this.centerX, this.centerY + this.objYSpace * 0.5, "Starting lives: %d", crusade.startingLives);
            Drawing.drawing.displayInterfaceText(this.centerX, this.centerY + this.objYSpace * 1, "Bonus life frequency: %d", crusade.bonusLifeFrequency);

            quit.draw();

            Drawing.drawing.setInterfaceFontSize(this.titleSize);
            Drawing.drawing.setColor(0, 0, 0);
            Drawing.drawing.displayInterfaceText(this.centerX, this.centerY + titleOffset, "Crusade details");

            if (uploadMode)
            {
                upload.draw();
            }
            else
            {
                crusadeName.draw();
                download.draw();
            }
        }
        else if (mode == Mode.items)
        {
            quit.draw();
            itemButtons.draw();

            Drawing.drawing.setInterfaceFontSize(this.titleSize);
            Drawing.drawing.setColor(0, 0, 0);
            Drawing.drawing.displayInterfaceText(this.centerX, this.centerY + titleOffset, "Crusade items");
        }
    }

    @Override
    public void addItem(Item i)
    {
        crusade.crusadeItems.add(i);
        Game.screen = new ScreenItemEditor(i, instance);
    }

    @Override
    public void removeItem(Item i)
    {
        this.crusade.crusadeItems.remove(i);
        this.refreshItemButtons();
    }

    @Override
    public void refreshItems()
    {
        this.refreshItemButtons();
    }

    public void updateDownloadButton()
    {
        BaseFile file = Game.game.fileManager.getFile(Game.homedir + Game.crusadeDir + "/" + crusadeName.inputText.replace(" ", "_") + ".tanks");

        if (file.exists())
        {
            download.setText("Pick a different name...");
            download.enabled = false;
        }
        else
        {
            download.setText("Download");
            download.enabled = true;
        }
    }

    @Override
    public void setupLayoutParameters()
    {
        if (Drawing.drawing.interfaceScaleZoom > 1)
        {
            this.objYSpace = 55;
            this.centerY -= 5;
        }
    }
}
