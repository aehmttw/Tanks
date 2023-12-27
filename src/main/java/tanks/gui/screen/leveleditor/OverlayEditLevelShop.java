package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.gui.Selector;
import tanks.gui.screen.*;
import tanks.hotbar.item.Item;
import tanks.registry.RegistryItem;

import java.util.ArrayList;

public class OverlayEditLevelShop extends ScreenLevelEditorOverlay implements IItemScreen
{
    public ButtonList shopList;
    public Selector itemSelector;

    public Button addItem = new Button(this.centerX + 380, this.centerY + 300, 350, 40, "Add item", new Runnable()
    {
        @Override
        public void run()
        {
            ScreenSelector s = new ScreenSelector(itemSelector, Game.screen);
            s.drawBehindScreen = true;
            s.images = itemSelector.images;
            s.drawImages = true;
            Game.screen = s;
        }
    }
    );

    public Button reorderItems = new Button(this.centerX - 380, this.centerY + 300, 350, 40, "Reorder items", new Runnable()
    {
        @Override
        public void run()
        {
            shopList.reorder = !shopList.reorder;
        }
    }
    );

    public Button back = new Button(this.centerX, this.centerY + 300, 350, 40, "Back", this::escape
    );

    public OverlayEditLevelShop(Screen previous, ScreenLevelEditor screenLevelEditor)
    {
        super(previous, screenLevelEditor);
        this.load();

        String[] itemNames = new String[Game.registryItem.itemEntries.size() + 1];
        String[] itemImages = new String[Game.registryItem.itemEntries.size() + 1];

        for (int i = 0; i < Game.registryItem.itemEntries.size(); i++)
        {
            RegistryItem.ItemEntry r = Game.registryItem.getEntry(i);
            itemNames[i] = r.name;
            itemImages[i] = r.image;
        }

        itemNames[Game.registryItem.itemEntries.size()] = "From template";
        itemImages[Game.registryItem.itemEntries.size()] = "item.png";

        itemSelector = new Selector(0, 0, 0, 0, "item type", itemNames, () ->
        {
            if (itemSelector.selectedOption == itemSelector.options.length - 1)
            {
                ScreenAddSavedItem s = new ScreenAddSavedItem((IItemScreen) Game.screen, this.addItem);
                s.drawBehindScreen = true;
                Game.screen = s;
            }
            else
            {
                Item i = Game.registryItem.getEntry(itemSelector.options[itemSelector.selectedOption]).getItem();
                addItem(i);
            }
        });

        itemSelector.images = itemImages;
        itemSelector.quick = true;
    }

    public void load()
    {
        shopList = new ButtonList(new ArrayList<>(), 0, 0, -30);
        shopList.arrowsEnabled = true;

        shopList.reorderBehavior = (i, j) ->
        {
            screenLevelEditor.level.shop.add(j, screenLevelEditor.level.shop.remove((int)i));
            ScreenLevelEditor.refreshItemButtons(screenLevelEditor.level.shop, shopList, false);
        };

        ScreenLevelEditor.refreshItemButtons(screenLevelEditor.level.shop, shopList, false);
    }

    public void update()
    {
        this.shopList.update();
        this.back.update();
        this.addItem.update();
        this.reorderItems.update();

        super.update();
    }

    public void draw()
    {
        super.draw();

        if (Game.screen != this)
            return;

        Drawing.drawing.setColor(screenLevelEditor.fontBrightness, screenLevelEditor.fontBrightness, screenLevelEditor.fontBrightness);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 270, "Shop items");
        this.shopList.draw();
        this.back.draw();
        this.addItem.draw();

        if (this.shopList.reorder)
            this.reorderItems.setText("Stop reordering");
        else
            this.reorderItems.setText("Reorder items");

        this.reorderItems.draw();
    }

    @Override
    public void addItem(Item i)
    {
        screenLevelEditor.level.shop.add(i);

        ScreenItemEditor s = new ScreenItemEditor(i, this, false, true);
        s.drawBehindScreen = true;
        Game.screen = s;
    }

    @Override
    public void removeItem(Item i)
    {
        screenLevelEditor.level.shop.remove(i);
        ScreenLevelEditor.refreshItemButtons(screenLevelEditor.level.shop, this.shopList, false);
    }

    @Override
    public void refreshItems()
    {
        ScreenLevelEditor.refreshItemButtons(screenLevelEditor.level.shop, this.shopList, false);
    }

}
