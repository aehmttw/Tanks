package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.gui.Button;
import tanks.gui.ITrigger;
import tanks.item.Item;
import tanks.item.ItemBullet;
import tanks.item.ItemMine;
import tanks.tankson.FieldPointer;
import tanks.tankson.Pointer;
import tanks.tankson.Property;

import java.io.IOException;
import java.lang.reflect.Field;

public class ScreenEditorItem extends ScreenEditorTanksONable<Item.ItemStack<?>>
{
    public TabItemProperties itemProperties;
    public ScreenEditorTanksONable<?> objectEditorScreen = null;
    public boolean showLoadFromTemplate = false;

    public Button itemTabButton;

    public Button load = new Button(this.centerX - this.objXSpace, this.centerY + this.objYSpace * 6.5, this.objWidth, this.objHeight, "Load from template", () ->
            Game.screen = new ScreenAddSavedItem(this, (b) ->
            {
                this.setTarget(b);
                Game.screen = this;
            }, "My", Item.class)
    );
    public Button save = new Button(this.centerX + this.objXSpace, this.centerY + this.objYSpace * 6.5, this.objWidth, this.objHeight, "Save to template", () ->
    {
        Item.ItemStack<?> t = target.get();
        this.writeItemAndConfirm(t, false);
    }
    );

    public ScreenEditorItem(Pointer<Item.ItemStack<?>> itemStack, Screen screen)
    {
        super(itemStack, screen);

        if (this.target.get() == null) return;

        this.title = "Edit %s";

        this.itemTabButton.image = "item.png";
        this.itemTabButton.drawImageShadow = true;
        this.itemTabButton.imageSizeX = 40;
        this.itemTabButton.imageSizeY = 40;
        this.itemTabButton.imageXOffset = -145;

        this.delete.function = () ->
        {
            setTarget(null);
            this.quit.function.run();
        };

        this.deleteText = "Delete item";
        this.showDeleteObj = false;
        this.delete.setText("Delete item");

        this.resetTabs();
    }

    public boolean writeItem(Item.ItemStack<?> t)
    {
        return this.writeItem(t, false);
    }

    public boolean writeItem(Item.ItemStack<?> t, boolean overwrite)
    {
        BaseFile f = Game.game.fileManager.getFile(Game.homedir + Game.itemDir + "/" + t.item.name.replace(" ", "_") + ".tanks");

        if (!f.exists() || overwrite)
        {
            try
            {
                if (!f.exists())
                    f.create();

                f.startWriting();
                f.println(t.toString());
                f.stopWriting();

                return true;
            }
            catch (IOException e)
            {
                Game.exitToCrash(e);
            }
        }

        return false;
    }

    public void writeItemAndConfirm(Item.ItemStack<?> i, boolean overwrite)
    {
        if (this.writeItem(i, overwrite))
            Game.screen = new ScreenItemSavedInfo(this);
        else
            Game.screen = new ScreenItemSaveOverwrite(this, i);
    }

    @Override
    public void resetTabs()
    {
        super.resetTabs();

        try
        {
            if (target.get() == null)
                return;

            this.objName = target.get().item.getClass().getField("item_class_name").get(null) + " item";

            Item.ItemStack<?> is = target.get();
            Item item = is.item;

            if (is instanceof ItemBullet.ItemStackBullet)
            {
                this.objectEditorScreen = new ScreenEditorBullet(new FieldPointer<>(item, item.getClass().getField("bullet"), false), this.prevScreen);
                ((ScreenEditorBullet) this.objectEditorScreen).bulletTypes.posX += 20;
            }
            else if (is instanceof ItemMine.ItemStackMine)
            {
                this.objectEditorScreen = new ScreenEditorMine(new FieldPointer<>(item, item.getClass().getField("mine"), false), this.prevScreen);
                this.objectEditorScreen.forceDisplayTabs = true;
                Button b = this.objectEditorScreen.topLevelButtons.get(0);
                b.posX = this.itemTabButton.posX;
                b.posY = this.itemTabButton.posY - 60;
                b.sizeX = this.itemTabButton.sizeX;
                b.imageXOffset = this.itemTabButton.imageXOffset;
            }
            else
                this.objectEditorScreen = null;

            if (this.objectEditorScreen != null)
            {
                this.objectEditorScreen.currentTab = null;
                this.objectEditorScreen.title += " item";
            }
        }
        catch (Exception e)
        {
            Game.exitToCrash(e);
        }
    }

    @Override
    public void setupTabs()
    {
        this.itemProperties = new TabItemProperties(this, "Item properties", "");
        this.setTab(this.itemProperties);
        this.itemTabButton = new Button(this.centerX, 175, 350, 40, "Item properties", () ->
        {
            this.setTab(itemProperties);
            this.objectEditorScreen.currentTab = null;
        });
    }

    @Override
    public void draw()
    {
        if (this.objectEditorScreen != null)
        {
            this.objectEditorScreen.draw();

            if (this.objectEditorScreen.currentTab == null)
            {
                Drawing.drawing.setInterfaceFontSize(this.titleSize);

                if (Level.isDark())
                    Drawing.drawing.setColor(255, 255, 255);
                else
                    Drawing.drawing.setColor(0, 0, 0);

                Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 200, this.currentTab.name);
                this.currentTab.draw();
            }

            this.itemTabButton.draw();
        }
        else
            super.draw();

        if (this.showLoadFromTemplate)
            load.draw();
        else
            this.delete.draw();

        this.save.draw();
    }

    @Override
    public void update()
    {
        if (this.objectEditorScreen != null)
        {
            this.objectEditorScreen.onComplete = this.onComplete;
            this.objectEditorScreen.update();

            if (this.objectEditorScreen.currentTab == null)
                this.currentTab.update();

            this.itemTabButton.enabled = this.objectEditorScreen.currentTab != null;
            this.itemTabButton.update();
        }
        else
            super.update();

        if (this.showLoadFromTemplate)
            load.update();
        else
            this.delete.update();

        this.save.update();
    }

    public class TabItemProperties extends Tab
    {
        public TabItemProperties(ScreenEditorTanksONable<Item.ItemStack<?>> screen, String name, String category)
        {
            super(screen, name, category);
        }

        public void addFields()
        {
            try
            {
                this.uiElements.clear();

                Item i = this.screen.target.get().item;

                // Item name, icon, cooldown
                FieldPointer<Item> ip = new FieldPointer<>(screen.target.get(), screen.target.getType().getField("item"));
                for (Field f : i.getClass().getFields())
                {
                    if (f.getDeclaringClass().equals(Item.class))
                    {
                        Property p = f.getAnnotation(Property.class);
                        if (p != null && p.category().equals(this.category))
                            this.uiElements.add(screen.getUIElementForField(new FieldPointer<>(ip.get(), f), p));
                    }
                }

                // Move the cooldown to be after stack size and max stack size
                ITrigger cooldown = this.uiElements.remove(this.uiElements.size() - 1);
                for (Field f : this.screen.fields)
                {
                    Property p = f.getAnnotation(Property.class);
                    if (p != null && p.category().equals(this.category) && !p.id().equals("item"))
                    {
                        this.uiElements.add(screen.getUIElementForField(new FieldPointer<>(screen.target.get(), f), p));
                    }
                }
                this.uiElements.add(cooldown);

                // Other per-item settings
                for (Field f : i.getClass().getFields())
                {
                    if (!f.getDeclaringClass().equals(Item.class))
                    {
                        Property p = f.getAnnotation(Property.class);
                        if (p != null && p.category().equals(this.category))
                        {
                            this.uiElements.add(screen.getUIElementForField(new FieldPointer<>(ip.get(), f), p));
                        }
                    }
                }
            }
            catch (Exception e)
            {
                Game.exitToCrash(e);
            }
        }
    }
}
