package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.Consumer;
import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.gui.Button;
import tanks.gui.SavedFilesList;
import tanks.gui.SearchBoxInstant;
import tanks.item.Item;
import tanks.translation.Translation;

import java.util.ArrayList;

public class ScreenAddSavedItem extends Screen implements IBlankBackgroundScreen
{
    public static int itemPage;

    public SavedFilesList allItems;
    public SavedFilesList items;

    public Screen previousScreen;

    public boolean deleting = false;
    public boolean removeNow = false;
    public int builtInItemsCount = 0;

    public String itemName;

    public Consumer<Item.ItemStack<?>> onComplete;

    SearchBoxInstant search = new SearchBoxInstant(this.centerX, this.centerY - this.objYSpace * 4, this.objWidth * 1.25, this.objHeight, "Search", new Runnable()
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
            Game.screen = previousScreen;
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

    public ScreenAddSavedItem(Screen previousScreen, Consumer<Item.ItemStack<?>> onComplete, String itemName)
    {
        this(previousScreen, onComplete, itemName, Item.ItemStack.class);
    }

    public ScreenAddSavedItem(Screen previousScreen, Consumer<Item.ItemStack<?>> onComplete, String itemName, Class itemClass)
    {
        super(350, 40, 380, 60);

        this.itemName = itemName;

        this.onComplete = onComplete;

        this.allowClose = false;

        this.music = previousScreen.music;
        this.musicID = previousScreen.musicID;
        this.previousScreen = previousScreen;

        allItems = new SavedFilesList(Game.homedir + Game.itemDir, itemPage, 0, -30,
                (name, file) ->
                {
                    try
                    {
                        file.startReading();
                        Item.ItemStack<?> i = Item.ItemStack.fromString(null, file.nextLine());
                        file.stopReading();
                        onComplete.accept(i);
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
                        Item.ItemStack<?> i = Item.ItemStack.fromString(null, file.nextLine());
                        file.stopReading();

                        b.itemIcon = i.item.icon;
                        b.imageXOffset = - b.sizeX / 2 + b.sizeY / 2 + 10;
                        b.imageSizeX = b.sizeY;
                        b.imageSizeY = b.sizeY;

                        if (!itemClass.isAssignableFrom(i.getClass()) && !itemClass.isAssignableFrom(i.item.getClass()))
                            b.text = null;
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                });

        ArrayList<String> items = Game.game.fileManager.getInternalFileContents("/items/items.tanks");

        for (String s: items)
        {
            Item.ItemStack<?> i = Item.ItemStack.fromString(null, s);
            i.item.name = Translation.translate(i.item.name);

            if (itemClass.isAssignableFrom(i.getClass()) || itemClass.isAssignableFrom(i.item.getClass()))
            {
                builtInItemsCount++;

                Button b = new Button(0, 0, this.allItems.objWidth, this.allItems.objHeight, i.item.name, () ->
                {
                    Item.ItemStack<?> i1 = Item.ItemStack.fromString(null, s);
                    i1.item.name = Translation.translate(i1.item.name);
                    onComplete.accept(i1);
                }
                );

                this.allItems.buttons.add(b);

                b.setSubtext("Built-in");
                b.translated = false;

                b.itemIcon = i.item.icon;
                b.imageXOffset = -b.sizeX / 2 + b.sizeY / 2 + 10;
                b.imageSizeX = b.sizeY;
                b.imageSizeY = b.sizeY;
            }
        }

        delete.textOffsetY = -2.5;

        delete.textColR = 255;
        delete.textColG = 255;
        delete.textColB = 255;

        delete.bgColR = 160;
        delete.bgColG = 160;
        delete.bgColB = 160;

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
        Drawing.drawing.setLighting(Level.currentLightIntensity, Math.max(Level.currentLightIntensity * 0.75, Level.currentShadowIntensity));
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
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 5, "%s item templates", itemName);
    }

    @Override
    public void onAttemptClose()
    {
        this.previousScreen.onAttemptClose();
    }
}
