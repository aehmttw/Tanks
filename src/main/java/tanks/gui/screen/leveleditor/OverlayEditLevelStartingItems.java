package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.gui.Selector;
import tanks.gui.screen.*;
import tanks.item.Item;
import tanks.registry.RegistryItem;
import tanks.tankson.FieldPointer;
import tanks.tankson.MonitoredArrayListIndexPointer;

import java.util.ArrayList;

public class OverlayEditLevelStartingItems extends ScreenLevelEditorOverlay implements IConditionalOverlayScreen
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

    public OverlayEditLevelStartingItems(Screen previous, ScreenLevelEditor screenLevelEditor)
    {
        super(previous, screenLevelEditor);
        this.load();

        String[] itemNames = new String[Game.registryItem.itemEntries.size()];
        String[] itemImages = new String[Game.registryItem.itemEntries.size()];

        for (int i = 0; i < Game.registryItem.itemEntries.size(); i++)
        {
            RegistryItem.ItemEntry r = Game.registryItem.getEntry(i);
            itemNames[i] = r.name;
            itemImages[i] = r.image;
        }

        itemSelector = new Selector(0, 0, 0, 0, "item type", itemNames, () ->
        {
            Item i = Game.registryItem.getEntry(itemSelector.options[itemSelector.selectedOption]).getItem();
            screenLevelEditor.level.startingItems.add(i.getStack(null));
            ScreenEditorItem s = new ScreenEditorItem(new MonitoredArrayListIndexPointer<>(screenLevelEditor.level.startingItems, screenLevelEditor.level.startingItems.size() - 1, false, this::refreshItems), Game.screen);
            s.onComplete = this::refreshItems;
            Game.screen = s;
        });

        itemSelector.images = itemImages;
        itemSelector.quick = true;
    }

    public void load()
    {
        startingItemsList = new ButtonList(new ArrayList<>(), 0, 0, -30);
        startingItemsList.arrowsEnabled = true;

        startingItemsList.reorderBehavior = (i, j) ->
        {
            screenLevelEditor.level.startingItems.add(j, screenLevelEditor.level.startingItems.remove((int)i));
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

        Drawing.drawing.setColor(screenLevelEditor.fontBrightness, screenLevelEditor.fontBrightness, screenLevelEditor.fontBrightness);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 270, "Starting items");
        this.startingItemsList.draw();
        this.back.draw();
        this.addItem.draw();

        if (this.startingItemsList.reorder)
            this.reorderItems.setText("Stop reordering");
        else
            this.reorderItems.setText("Reorder items");

        this.reorderItems.draw();
    }

    public void refreshItems()
    {
        ButtonList buttons = this.startingItemsList;
        ArrayList<Item.ItemStack<?>> items = screenLevelEditor.level.startingItems;

        buttons.buttons.clear();

        for (int i = 0; i < items.size(); i++)
        {
            int j = i;

            Button b = new Button(0, 0, 350, 40, items.get(i).item.name, () ->
            {
                ScreenEditorItem s = new ScreenEditorItem(new MonitoredArrayListIndexPointer<>(screenLevelEditor.level.startingItems, j, false, this::refreshItems), Game.screen);
                s.onComplete = this::refreshItems;
                Game.screen = s;
            });

            b.image = items.get(j).item.icon;
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
