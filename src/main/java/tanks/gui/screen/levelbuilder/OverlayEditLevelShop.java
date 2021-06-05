package tanks.gui.screen.levelbuilder;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.gui.Selector;
import tanks.gui.screen.*;
import tanks.hotbar.item.Item;
import tanks.registry.RegistryItem;

import java.util.ArrayList;

public class OverlayEditLevelShop extends ScreenLevelBuilderOverlay implements IItemScreen
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

    public Button back = new Button(this.centerX, this.centerY + 300, 350, 40, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            escape();
        }
    }
    );

    public OverlayEditLevelShop(Screen previous, ScreenLevelBuilder screenLevelBuilder)
    {
        super(previous, screenLevelBuilder);
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
            screenLevelBuilder.level.shop.add(j, screenLevelBuilder.level.shop.remove((int)i));
            ScreenLevelBuilder.refreshItemButtons(screenLevelBuilder.level.shop, shopList, false);
        };

        ScreenLevelBuilder.refreshItemButtons(screenLevelBuilder.level.shop, shopList, false);
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

        Drawing.drawing.setColor(screenLevelBuilder.fontBrightness, screenLevelBuilder.fontBrightness, screenLevelBuilder.fontBrightness);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - 270, "Shop items");
        this.shopList.draw();
        this.back.draw();
        this.addItem.draw();

        if (this.shopList.reorder)
            this.reorderItems.text = "Stop reordering";
        else
            this.reorderItems.text = "Reorder items";

        this.reorderItems.draw();
    }

    @Override
    public void addItem(Item i)
    {
        screenLevelBuilder.level.shop.add(i);

        //TODO change on starting items
        ScreenEditItem s = new ScreenEditItem(i, this, false, true);
        s.drawBehindScreen = true;
        Game.screen = s;
    }

    @Override
    public void removeItem(Item i)
    {
        screenLevelBuilder.level.shop.remove(i);
        //TODO same as above
        ScreenLevelBuilder.refreshItemButtons(screenLevelBuilder.level.shop, this.shopList, false);
    }

    @Override
    public void refreshItems()
    {
        ScreenLevelBuilder.refreshItemButtons(screenLevelBuilder.level.shop, this.shopList, false);
    }

}
