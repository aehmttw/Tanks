package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.gui.Button;
import tanks.gui.SavedFilesList;
import tanks.gui.SearchBox;
import tanks.hotbar.item.Item;
import tanks.translation.Translation;

import java.util.ArrayList;

public class ScreenAddSavedItem extends Screen implements IConditionalOverlayScreen
{
    public static int itemPage;

    public SavedFilesList allItems;
    public SavedFilesList items;
    public boolean drawBehindScreen;

    public IItemScreen itemScreen;
    public Button back;

    public boolean deleting = false;

    public boolean removeNow = false;

    public int builtInItemsCount = 0;

    SearchBox search = new SearchBox(this.centerX, this.centerY - this.objYSpace * 4, this.objWidth * 1.25, this.objHeight, "Search", new Runnable()
    {
        @Override
        public void run()
        {
            createNewItemsList();
            items.filter(search.inputText);
            items.sortButtons();
        }
    }, "");

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
                deleteMode.setText("Stop deleting");
            else
                deleteMode.setText("Delete templates");

            for (Button b: allItems.buttons)
                b.enabled = !deleting;
        }
    }
    );

    public Button delete = new Button(0, 0, 32, 32, "x", () -> removeNow = true);

    public ScreenAddSavedItem(IItemScreen itemScreen, Button prev)
    {
        this(itemScreen, prev, Item.class);
    }

    public ScreenAddSavedItem(IItemScreen itemScreen, Button prev, Class<? extends Item> itemClass)
    {
        super(350, 40, 380, 60);

        this.allowClose = false;

        this.music = ((Screen)itemScreen).music;
        this.musicID = ((Screen)itemScreen).musicID;
        this.itemScreen = itemScreen;
        this.back = prev;

        allItems = new SavedFilesList(Game.homedir + Game.itemDir, itemPage, 0, -30,
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

                        if (!itemClass.isAssignableFrom(i.getClass()))
                            b.text = null;

                        int p = i.price;

                        if (p == 0)
                            b.setSubtext("Free!");
                        else if (p == 1)
                            b.setSubtext("1 coin");
                        else
                            b.setSubtext("%d coins", p);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                });

        ArrayList<String> items = Game.game.fileManager.getInternalFileContents("/items/items.tanks");

        for (String s: items)
        {
            Item i = Item.parseItem(null, s);
            i.name = Translation.translate(i.name);

            if (itemClass.isAssignableFrom(i.getClass()))
            {
                builtInItemsCount++;

                Button b = new Button(0, 0, this.allItems.objWidth, this.allItems.objHeight, i.name, () ->
                {
                    Item i1 = Item.parseItem(null, s);
                    i1.name = Translation.translate(i1.name);
                    i1.importProperties();
                    itemScreen.addItem(i1);
                }
                );

                this.allItems.buttons.add(b);

                b.translated = false;

                b.image = i.icon;
                b.imageXOffset = -b.sizeX / 2 + b.sizeY / 2 + 10;
                b.imageSizeX = b.sizeY;
                b.imageSizeY = b.sizeY;

                int p = i.price;

                if (p == 0)
                    b.setSubtext("Free!");
                else if (p == 1)
                    b.setSubtext("1 coin");
                else
                    b.setSubtext("%d coins", p);
            }
        }

        delete.textOffsetY = -2.5;

        delete.textColR = 255;
        delete.textColG = 255;
        delete.textColB = 255;

        delete.unselectedColR = 160;
        delete.unselectedColG = 160;
        delete.unselectedColB = 160;

        delete.selectedColR = 255;
        delete.selectedColG = 0;
        delete.selectedColB = 0;

        this.items = this.allItems.clone();
        this.createNewItemsList();

        delete.fontSize = this.textSize;
    }

    public void createNewItemsList()
    {
        items.buttons.clear();
        items.buttons.addAll(allItems.buttons);
        items.sortButtons();
    }

    @Override
    public void update()
    {
        items.update();
        quit.update();
        search.update();
        deleteMode.update();

        if (deleting)
        {
            for (int i = Math.min(items.page * items.rows * items.columns + items.rows * items.columns, items.buttons.size()) - 1; i >= items.page * items.rows * items.columns; i--)
            {
                Button b = items.buttons.get(i);

                if (allItems.buttons.indexOf(b) >= allItems.buttons.size() - builtInItemsCount)
                    continue;

                delete.posX = b.posX + b.sizeX / 2 - b.sizeY / 2;
                delete.posY = b.posY;
                delete.update();

                if (removeNow)
                {
                    removeNow = false;

                    Button b1 = items.buttons.remove(i);
                    BaseFile f = Game.game.fileManager.getFile(Game.homedir + Game.itemDir + "/" + b1.text.replace(" ", "_") + ".tanks");

                    allItems.buttons.remove(b1);

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
        search.draw();
        deleteMode.draw();

        if (deleting)
        {
            for (int i = Math.min(items.page * items.rows * items.columns + items.rows * items.columns, items.buttons.size()) - 1; i >= items.page * items.rows * items.columns; i--)
            {
                Button b = items.buttons.get(i);

                if (allItems.buttons.indexOf(b) >= allItems.buttons.size() - builtInItemsCount)
                    continue;

                delete.posX = b.posX + b.sizeX / 2 - b.sizeY / 2;
                delete.posY = b.posY;
                delete.update();
                delete.draw();
            }
        }

        if (Level.isDark())
            Drawing.drawing.setColor(255, 255, 255);
        else
            Drawing.drawing.setColor(0, 0, 0);

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 5, "Item templates");
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

    @Override
    public void onAttemptClose()
    {
        ((Screen)this.itemScreen).onAttemptClose();
    }
}
