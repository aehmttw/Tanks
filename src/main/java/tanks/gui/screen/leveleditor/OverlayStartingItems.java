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
import tanks.gui.screen.ScreenEditorItem;
import tanks.item.Item;
import tanks.item.ItemIcon;
import tanks.registry.RegistryItem;
import tanks.tankson.MonitoredArrayListIndexPointer;

import java.util.ArrayList;

public class OverlayStartingItems extends ScreenLevelEditorOverlay implements IConditionalOverlayScreen
{
    public ButtonList startingItemsList;
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
            startingItemsList.reorder = !startingItemsList.reorder;
        }
    }
    );

    public Button back = new Button(this.centerX, this.centerY + 300, 350, 40, "Back", this::escape
    );

    public OverlayStartingItems(Screen previous, ScreenLevelEditor screenLevelEditor)
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
                screenLevelEditor.level.startingItems.add(i);
                ScreenEditorItem s = new ScreenEditorItem(new MonitoredArrayListIndexPointer<>((Class<Item.ItemStack<?>>)(Class<?>) Item.ItemStack.class, screenLevelEditor.level.startingItems, screenLevelEditor.level.startingItems.size() - 1, false, this::refreshItems), this);
                s.onComplete = this::refreshItems;
                Game.screen = s;
            };

            Game.screen = new ScreenAddSavedItem(this, addItem, Game.formatString(itemSelector.options[itemSelector.selectedOption]), Game.registryItem.getEntry(itemSelector.selectedOption).item);
        });

        itemSelector.itemIcons = itemImages;
        itemSelector.quick = true;
    }

    public void load()
    {
        startingItemsList = new ButtonList(new ArrayList<>(), 0, 0, -30);
        startingItemsList.arrowsEnabled = true;
        startingItemsList.manualDarkMode = true;

        startingItemsList.reorderBehavior = (i, j) ->
        {
            editor.level.startingItems.add(j, editor.level.startingItems.remove((int)i));
            this.refreshItems();
        };

        this.refreshItems();
    }

    public void update()
    {
        this.startingItemsList.update();
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
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 270, "Starting items");

        if (this.startingItemsList.buttons.size() <= 0)
        {
            Drawing.drawing.setInterfaceFontSize(this.textSize);
            Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - 30, "There are no starting items in this level");
            Drawing.drawing.drawInterfaceText(this.centerX, this.centerY + 30, "Add some with the 'Add item' button!");
        }

        this.startingItemsList.draw();
        this.back.draw();
        this.addItem.draw();

        for (int i = Math.min((this.startingItemsList.page + 1) * this.startingItemsList.rows * this.startingItemsList.columns, startingItemsList.buttons.size()) - 1; i >= this.startingItemsList.page * this.startingItemsList.rows * this.startingItemsList.columns; i--)
        {
            Button b = this.startingItemsList.buttons.get(i);
            Drawing.drawing.setColor(0, 0, 0);
            Drawing.drawing.setInterfaceFontSize(this.textSize / 2);
            int ss = editor.level.startingItems.get(i).stackSize;

            if (ss > 0)
                Drawing.drawing.drawInterfaceText(b.posX - b.sizeX / 2 + b.sizeY, b.posY + b.sizeY * 0.325, "x" + ss, false);
        }

        if (this.startingItemsList.reorder)
            this.reorderItems.setText("Stop reordering");
        else
            this.reorderItems.setText("Reorder items");

        this.reorderItems.draw();
    }

    public void refreshItems()
    {
        ButtonList buttons = this.startingItemsList;
        ArrayList<Item.ItemStack<?>> items = editor.level.startingItems;

        buttons.buttons.clear();

        for (int i = 0; i < items.size(); i++)
        {
            int j = i;

            Button b = new Button(0, 0, 350, 40, items.get(i).item.name, () ->
            {
                ScreenEditorItem s = new ScreenEditorItem(new MonitoredArrayListIndexPointer<>((Class<Item.ItemStack<?>>)(Class<?>) Item.ItemStack.class, editor.level.startingItems, j, false, this::refreshItems), Game.screen);
                s.onComplete = this::refreshItems;
                Game.screen = s;
            });

            b.itemIcon = items.get(j).item.icon;
            b.imageXOffset = - b.sizeX / 2 + b.sizeY / 2 + 10;
            b.imageSizeX = b.sizeY;
            b.imageSizeY = b.sizeY;

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
