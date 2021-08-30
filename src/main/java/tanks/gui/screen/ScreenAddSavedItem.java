package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.Crusade;
import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.gui.Button;
import tanks.gui.SavedFilesList;
import tanks.hotbar.item.Item;

import java.util.ArrayList;

public class ScreenAddSavedItem extends Screen implements IConditionalOverlayScreen
{
    public static int itemPage;

    public SavedFilesList items;
    public boolean drawBehindScreen;

    public IItemScreen itemScreen;
    public Button back;

    public boolean deleting = false;

    public boolean removeNow = false;

    public int builtInItemsCount = 0;

    public Button quit = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = ((Screen)itemScreen);
            back.function.run();
        }
    }
    );

    public Button deleteMode = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Delete templates", new Runnable()
    {
        @Override
        public void run()
        {
            deleting = !deleting;

            if (deleting)
                deleteMode.text = "Stop deleting";
            else
                deleteMode.text = "Delete templates";

            for (Button b: items.buttons)
                b.enabled = !deleting;
        }
    }
    );

    public Button delete = new Button(0, 0, 32, 32, "x", new Runnable()
    {
        @Override
        public void run()
        {
            removeNow = true;
        }
    });

    public ScreenAddSavedItem(IItemScreen itemScreen, Button prev)
    {
        super(350, 40, 380, 60);
        
        this.music = ((Screen)itemScreen).music;
        this.musicID = ((Screen)itemScreen).musicID;
        this.itemScreen = itemScreen;
        this.back = prev;

        items = new SavedFilesList(Game.homedir + Game.itemDir, itemPage, 0, -30,
                (name, file) ->
                {
                    try
                    {
                        file.startReading();
                        Item i = Item.parseItem(null, file.nextLine());
                        i.importProperties();
                        file.stopReading();
                        itemScreen.addItem(i);
                    }
                    catch (Exception e)
                    {
                        Game.exitToCrash(e);
                    }
                }, (file) -> null,
                (file, b) ->
                {
                    try
                    {
                        file.startReading();
                        Item i = Item.parseItem(null, file.nextLine());
                        file.stopReading();

                        b.image = i.icon;
                        b.imageXOffset = - b.sizeX / 2 + b.sizeY / 2 + 10;
                        b.imageSizeX = b.sizeY;
                        b.imageSizeY = b.sizeY;

                        int p = i.price;
                        String price = p + " ";
                        if (p == 0)
                            price = "Free!";
                        else if (p == 1)
                            price += "coin";
                        else
                            price += "coins";

                        b.subtext = price;
                    }
                    catch (Exception e)
                    {
                        Game.exitToCrash(e);
                    }
                });

        ArrayList<String> items = Game.game.fileManager.getInternalFileContents("/items/items.tanks");

        for (String s: items)
        {
            builtInItemsCount++;
            Item i = Item.parseItem(null, s);

            Button b = new Button(0, 0, this.items.objWidth, this.items.objHeight, "", new Runnable()
            {
                @Override
                public void run()
                {
                    Item i = Item.parseItem(null, s);
                    i.importProperties();
                    itemScreen.addItem(i);
                }
            }
            );

            this.items.buttons.add(b);

            b.text = i.name;

            b.image = i.icon;
            b.imageXOffset = - b.sizeX / 2 + b.sizeY / 2 + 10;
            b.imageSizeX = b.sizeY;
            b.imageSizeY = b.sizeY;

            int p = i.price;
            String price = p + " ";
            if (p == 0)
                price = "Free!";
            else if (p == 1)
                price += "coin";
            else
                price += "coins";

            b.subtext = price;
        }

        this.items.sortButtons();

        delete.textOffsetY = -1;
        delete.textOffsetX = 1;

        delete.textColR = 255;
        delete.textColG = 255;
        delete.textColB = 255;

        delete.unselectedColR = 255;
        delete.unselectedColG = 0;
        delete.unselectedColB = 0;

        delete.selectedColR = 255;
        delete.selectedColG = 127;
        delete.selectedColB = 127;

        delete.fontSize = this.textSize;
    }

    @Override
    public void update()
    {
        items.update();
        quit.update();
        deleteMode.update();

        if (deleting)
        {
            for (int i = Math.min(items.page * items.rows * items.columns + items.rows * items.columns, items.buttons.size()) - 1; i >= items.page * items.rows * items.columns; i--)
            {
                if (i >= items.buttons.size() - builtInItemsCount)
                    continue;

                Button b = items.buttons.get(i);
                delete.posX = b.posX + b.sizeX / 2 - b.sizeY / 2;
                delete.posY = b.posY;
                delete.update();

                if (removeNow)
                {
                    removeNow = false;
                    BaseFile f = Game.game.fileManager.getFile(Game.homedir + Game.itemDir + "/" + items.buttons.remove(i).text.replace(" ", "_") + ".tanks");

                    while (f.exists())
                    {
                        f.delete();
                    }

                    items.sortButtons();
                    break;
                }
            }
        }

        itemPage = items.page;
    }

    @Override
    public void draw()
    {
        if (this.drawBehindScreen)
        {
            this.enableMargins = ((Screen)this.itemScreen).enableMargins;
            ((Screen)this.itemScreen).draw();
        }
        else
            this.drawDefaultBackground();

        items.draw();
        quit.draw();
        deleteMode.draw();

        if (deleting)
        {
            for (int i = Math.min(items.page * items.rows * items.columns + items.rows * items.columns, items.buttons.size()) - 1; i >= items.page * items.rows * items.columns; i--)
            {
                if (i >= items.buttons.size() - builtInItemsCount)
                    continue;

                Button b = items.buttons.get(i);
                delete.posX = b.posX + b.sizeX / 2 - b.sizeY / 2;
                delete.posY = b.posY;
                delete.draw();
            }
        }

        if (Level.isDark())
            Drawing.drawing.setColor(255, 255, 255);
        else
            Drawing.drawing.setColor(0, 0, 0);

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 4.5, "Item templates");
    }

    @Override
    public void setupLayoutParameters()
    {

    }

    @Override
    public double getOffsetX()
    {
        if (drawBehindScreen)
            return ((Screen)itemScreen).getOffsetX();
        else
            return super.getOffsetX();
    }

    @Override
    public double getOffsetY()
    {
        if (drawBehindScreen)
            return ((Screen)itemScreen).getOffsetY();
        else
            return super.getOffsetY();
    }

    @Override
    public double getScale()
    {
        if (drawBehindScreen)
            return ((Screen)itemScreen).getScale();
        else
            return super.getScale();
    }

    @Override
    public boolean isOverlayEnabled()
    {
        if (itemScreen instanceof IConditionalOverlayScreen)
            return ((IConditionalOverlayScreen) itemScreen).isOverlayEnabled();

        return itemScreen instanceof ScreenGame || itemScreen instanceof ILevelPreviewScreen || itemScreen instanceof IOverlayScreen;
    }
}
