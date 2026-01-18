package tanks.gui.screen.leveleditor;

import tanks.Consumer;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.gui.Selector;
import tanks.gui.screen.IConditionalOverlayScreen;
import tanks.gui.screen.Screen;
import tanks.gui.screen.ScreenAddSavedItem;
import tanks.gui.screen.ScreenEditorShopItem;
import tanks.item.Item;
import tanks.item.ItemIcon;
import tanks.registry.RegistryItem;
import tanks.tankson.MonitoredArrayListIndexPointer;

import java.util.ArrayList;

public class OverlayShop extends ScreenLevelEditorOverlay implements IConditionalOverlayScreen
{
    public ButtonList shopList;
    public Selector itemSelector;

    public Button addItem = new Button(this.centerX + 380, this.centerY + 300, 350, 40, "Add item", new Runnable()
    {
        @Override
        public void run()
        {
            itemSelector.setScreen();
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

    public OverlayShop(Screen previous, ScreenLevelEditor screenLevelEditor)
    {
        super(previous, screenLevelEditor);
        this.load();

        String[] itemNames = new String[Game.registryItem.itemEntries.size()];
        ItemIcon[] itemImages = new ItemIcon[Game.registryItem.itemEntries.size()];

        for (int i = 0; i < Game.registryItem.itemEntries.size(); i++)
        {
            RegistryItem.ItemEntry r = Game.registryItem.getEntry(i);
            itemNames[i] = r.name;
            itemImages[i] = r.icon;
        }

        itemSelector = new Selector(0, 0, 0, 0, "item type", itemNames, () ->
        {
            Consumer<Item.ItemStack<?>> addItem = (Item.ItemStack<?> i) ->
            {
                try
                {
                    screenLevelEditor.level.shop.add(new Item.ShopItem(i));
                    ScreenEditorShopItem s = new ScreenEditorShopItem(new MonitoredArrayListIndexPointer<>(Item.ShopItem.class, screenLevelEditor.level.shop, screenLevelEditor.level.shop.size() - 1, false, this::refreshItems), this);
                    s.onComplete = this::refreshItems;
                    Game.screen = s;
                }
                catch (NoSuchFieldException e)
                {
                    e.printStackTrace();
                }
            };

            Game.screen = new ScreenAddSavedItem(this, addItem, Game.formatString(itemSelector.options[itemSelector.selectedOption]), Game.registryItem.getEntry(itemSelector.selectedOption).item);
        });

        itemSelector.itemIcons = itemImages;
        itemSelector.quick = true;
    }

    public void load()
    {
        shopList = new ButtonList(new ArrayList<>(), 0, 0, -30);
        shopList.arrowsEnabled = true;
        shopList.manualDarkMode = true;

        shopList.reorderBehavior = (i, j) ->
        {
            editor.level.shop.add(j, editor.level.shop.remove((int)i));
            this.refreshItems();
        };

        this.refreshItems();
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

        Drawing.drawing.setColor(0, 0, 0, 127);
        Drawing.drawing.drawPopup(this.centerX, this.centerY, 1200, 720);

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 270, "Shop items");

        if (this.shopList.buttons.size() <= 0)
        {
            Drawing.drawing.setInterfaceFontSize(this.textSize);
            Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - 30, "There are no shop items in this level");
            Drawing.drawing.drawInterfaceText(this.centerX, this.centerY + 30, "Add some with the 'Add item' button!");
        }

        this.shopList.draw();
        this.back.draw();
        this.addItem.draw();

        if (this.shopList.reorder)
            this.reorderItems.setText("Stop reordering");
        else
            this.reorderItems.setText("Reorder items");

        this.reorderItems.draw();
    }

    public void addItem(Item.ShopItem i)
    {
        editor.level.shop.add(i);
        this.refreshItems();
    }

    public void removeItem(Item.ShopItem i)
    {
        editor.level.shop.remove(i);
        this.refreshItems();
    }

    public void refreshItems()
    {
        ButtonList buttons = this.shopList;
        ArrayList<Item.ShopItem> items = editor.level.shop;

        buttons.buttons.clear();

        for (int i = 0; i < items.size(); i++)
        {
            int j = i;

            Button b = new Button(0, 0, 350, 40, items.get(i).itemStack.item.name, () ->
            {
                try
                {
                    ScreenEditorShopItem s = new ScreenEditorShopItem(new MonitoredArrayListIndexPointer<>(Item.ShopItem.class, editor.level.shop, j, false, this::refreshItems), Game.screen);
                    s.onComplete = this::refreshItems;
                    Game.screen = s;
                }
                catch (NoSuchFieldException e)
                {
                    Game.exitToCrash(e);
                }
            });

            b.itemIcon = items.get(j).itemStack.item.icon;
            b.imageXOffset = - b.sizeX / 2 + b.sizeY / 2 + 10;
            b.imageSizeX = b.sizeY;
            b.imageSizeY = b.sizeY;

            int p = items.get(i).price;

            if (p == 0)
                b.setSubtext("Free!");
            else if (p == 1)
                b.setSubtext("1 coin");
            else
                b.setSubtext("%d coins", p);

            buttons.buttons.add(b);
        }

        buttons.sortButtons();
    }

    @Override
    public boolean isOverlayEnabled()
    {
        return Game.screen == this;
    }
}
